package fr.perrier.guard.reports;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Contrat du garde-fou anti-abus : pur, aucun serveur (des String + une liste de rapports).
 * La garde renvoie une CLE de lang de refus (ou null si autorisé) — la traduction est
 * de la responsabilité de l'appelant, comme pour toutes les autres clés du plugin.
 */
public class ReportGuardTest {

    private static final String ME = "uuid-moi";
    private static final String TARGET = "uuid-cible";
    private static final long NOW = 10_000L;

    private static Report report(String reporterUuid, String reportedUuid, long createdAt, String status) {
        Report r = new Report("id", reporterUuid, reportedUuid, "raison", "lobby", createdAt, status, null);
        r.setReporterUuid(reporterUuid);
        r.setReportedUuid(reportedUuid);
        return r;
    }

    private static ReportGuard.GuardSettings settings(int cooldown, int maxOpen,
                                                      int targetCooldown, boolean requireOnline) {
        return new ReportGuard.GuardSettings(cooldown, maxOpen, targetCooldown, requireOnline);
    }

    @Test
    public void selfReportRefused() {
        assertEquals("reports.refus.self", ReportGuard.check("Moi", "Moi", ME, ME, true, false,
                NOW, new ArrayList<>(), settings(30, 5, 300, true)));
    }

    @Test
    public void immuneTargetRefused() {
        assertEquals("reports.refus.immune", ReportGuard.check("Moi", "Cible", ME, TARGET, true, true,
                NOW, new ArrayList<>(), settings(30, 5, 300, true)));
    }

    @Test
    public void offlineTargetRefusedOnlyWhenRequired() {
        assertEquals("reports.refus.offline", ReportGuard.check("Moi", "Cible", ME, TARGET, false, false,
                NOW, new ArrayList<>(), settings(30, 5, 300, true)));
        assertNull(ReportGuard.check("Moi", "Cible", ME, TARGET, false, false,
                NOW, new ArrayList<>(), settings(30, 5, 300, false)));
    }

    @Test
    public void openReportsLimitRefusedButClosedDontCount() {
        List<Report> full = new ArrayList<>(Arrays.asList(
                report(ME, "autre1", 1_000L, Report.STATUS_OPEN),
                report(ME, "autre2", 1_001L, Report.STATUS_IN_PROGRESS)));
        assertEquals("reports.refus.limit", ReportGuard.check("Moi", "Cible", ME, TARGET, true, false,
                NOW, full, settings(30, 2, 300, true)));

        List<Report> closed = new ArrayList<>(Arrays.asList(
                report(ME, "autre1", NOW - 31_000L, Report.STATUS_CLOSED),
                report(ME, "autre2", NOW - 31_000L, Report.STATUS_DELETED)));
        assertNull(ReportGuard.check("Moi", "Cible", ME, TARGET, true, false,
                NOW, closed, settings(30, 2, 300, true)));
    }

    @Test
    public void reporterCooldownRefusedOnlyWithinWindow() {
        assertEquals("reports.refus.cooldown", ReportGuard.check("Moi", "Cible", ME, TARGET, true, false,
                NOW, new ArrayList<>(Arrays.asList(report(ME, "autre1", NOW - 5_000L, Report.STATUS_CLOSED))),
                settings(30, 5, 300, true)));
        assertNull(ReportGuard.check("Moi", "Cible", ME, TARGET, true, false,
                NOW, new ArrayList<>(Arrays.asList(report(ME, "autre1", NOW - 31_000L, Report.STATUS_CLOSED))),
                settings(30, 5, 300, true)));
    }

    @Test
    public void targetCooldownRefusedWithinWindow() {
        assertEquals("reports.refus.target-cooldown", ReportGuard.check("Moi", "Cible", ME, TARGET, true, false,
                NOW, new ArrayList<>(Arrays.asList(report("uuid-autre", TARGET, NOW - 1_000L, Report.STATUS_CLOSED))),
                settings(30, 5, 300, true)));
        assertNull(ReportGuard.check("Moi", "Cible", ME, TARGET, true, false,
                NOW, new ArrayList<>(Arrays.asList(report("uuid-autre", TARGET, NOW - 301_000L, Report.STATUS_CLOSED))),
                settings(30, 5, 300, true)));
    }

    @Test
    public void firstRefusalWins() {
        // self + immunisé + tout : c'est "self" qui sort (ordre le plus utile à afficher)
        assertEquals("reports.refus.self", ReportGuard.check("Moi", "Moi", ME, ME, false, true,
                NOW, new ArrayList<>(Arrays.asList(report(ME, TARGET, NOW, Report.STATUS_OPEN))),
                settings(30, 0, 300, true)));
    }

    @Test
    public void negativeMaxOpenMeansUnlimited() {
        List<Report> many = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            many.add(report(ME, "autre" + i, 1_000L, Report.STATUS_OPEN));
        }
        assertNull(ReportGuard.check("Moi", "Cible", ME, TARGET, true, false,
                NOW, many, settings(0, -1, 300, true)));
    }

    @Test
    public void deletedReportsDontCountAsOpen() {
        List<Report> deleted = new ArrayList<>(Arrays.asList(
                report(ME, "autre1", 1_000L, Report.STATUS_DELETED),
                report(ME, "autre2", 1_001L, Report.STATUS_DELETED)));
        assertNull(ReportGuard.check("Moi", "Cible", ME, TARGET, true, false,
                NOW, deleted, settings(0, 2, 300, true)));
    }

    @Test
    public void cleanCaseAllowed() {
        assertNull(ReportGuard.check("Moi", "Cible", ME, TARGET, true, false,
                NOW, new ArrayList<>(), settings(30, 5, 300, true)));
    }
}
