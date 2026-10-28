package fr.perrier.guard.reports;

import java.util.List;

import static fr.perrier.guard.reports.Report.STATUS_CLOSED;
import static fr.perrier.guard.reports.Report.STATUS_IN_PROGRESS;
import static fr.perrier.guard.reports.Report.STATUS_OPEN;

/**
 * Garde-fou anti-abus de {@code /report} — 100 % pur : des String, une liste de rapports,
 * une horloge injectée. Aucun accès à Bukkit ni à la config (l'appelant fournit les seuils
 * et traduit la clé de refus).
 *
 * Renvoie une clé de lang de refus ({@code reports.refus.*}) ou {@code null} si autorisé.
 * L'ordre des contrôles est du plus important au moins important : le premier refus gagne.
 */
public final class ReportGuard {

    private ReportGuard() {
    }

    /** Seuils lus par l'appelant depuis {@code config.yml}. */
    public static final class GuardSettings {
        public final int cooldownSeconds;
        public final int maxOpenPerReporter;
        public final int targetCooldownSeconds;
        public final boolean requireOnlineTarget;

        public GuardSettings(int cooldownSeconds, int maxOpenPerReporter,
                             int targetCooldownSeconds, boolean requireOnlineTarget) {
            this.cooldownSeconds = cooldownSeconds;
            this.maxOpenPerReporter = maxOpenPerReporter;
            this.targetCooldownSeconds = targetCooldownSeconds;
            this.requireOnlineTarget = requireOnlineTarget;
        }
    }

    public static String check(String reporterName, String targetName,
                               String reporterUuid, String targetUuid,
                               boolean targetOnline, boolean targetImmune,
                               long nowMillis, List<Report> context, GuardSettings s) {
        // 1. se signaler soi-même
        if (reporterName != null && reporterName.equalsIgnoreCase(targetName)) {
            return "reports.refus.self";
        }
        // 2. cible immunisée (permission guard.reports.immune, évaluée par l'appelant)
        if (targetImmune) {
            return "reports.refus.immune";
        }
        // 3. cible en ligne obligatoire
        if (s.requireOnlineTarget && !targetOnline) {
            return "reports.refus.offline";
        }
        // 4. plafond de rapports ouverts par signalant
        int openCount = 0;
        long reporterLast = 0L;
        long targetLast = 0L;
        for (Report report : context) {
            if (report == null) {
                continue;
            }
            String reporterId = report.getReporterUuid() != null ? report.getReporterUuid() : report.getReporter();
            String reportedId = report.getReportedUuid() != null ? report.getReportedUuid() : report.getReported();
            boolean live = STATUS_OPEN.equals(report.getStatus()) || STATUS_IN_PROGRESS.equals(report.getStatus());
            if (reporterId != null && reporterId.equals(reporterUuid)) {
                reporterLast = Math.max(reporterLast, report.getCreatedAt());
                if (live) {
                    openCount++;
                }
            }
            if (reportedId != null && reportedId.equals(targetUuid)) {
                targetLast = Math.max(targetLast, report.getCreatedAt());
            }
        }
        // -1 (ou négatif) = illimité, cf. config.yml max-open-per-reporter
        if (s.maxOpenPerReporter >= 0 && openCount >= s.maxOpenPerReporter) {
            return "reports.refus.limit";
        }
        // 5. cooldown du signalant
        if (s.cooldownSeconds > 0 && reporterLast > 0
                && within(nowMillis, reporterLast, s.cooldownSeconds * 1000L)) {
            return "reports.refus.cooldown";
        }
        // 6. cooldown de la cible (elle vient d'être signalée par quelqu'un)
        if (s.targetCooldownSeconds > 0 && targetLast > 0
                && within(nowMillis, targetLast, s.targetCooldownSeconds * 1000L)) {
            return "reports.refus.target-cooldown";
        }
        return null;
    }

    private static boolean within(long now, long last, long windowMillis) {
        return now - last >= 0 && now - last < windowMillis;
    }
}
