package fr.perrier.guard.reports;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contrat des filtres/tris de la file staff — pur (des Report + des String).
 * C'est la logique que la GUI applique au snapshot de ReportCache.
 */
public class ReportFiltersTest {

    private static Report r(String id, String reporter, String reported, String reason,
                            long createdAt, String status, String handler) {
        Report rep = new Report(id, reporter, reported, reason, "lobby", createdAt, status, null);
        rep.setHandler(handler);
        rep.setHandlerUuid("uuid-" + handler); // le filtre "mes rapports" compare l'UUID
        return rep;
    }

    private static List<Report> sample() {
        return new ArrayList<>(Arrays.asList(
                r("1", "Alex", "Steve", "forcefield", 3_000L, Report.STATUS_IN_PROGRESS, "Modo1"),
                r("2", "Bob", "Alice", "spam", 2_000L, Report.STATUS_OPEN, null),
                r("3", "Cid", "Steve", "grief", 1_000L, Report.STATUS_CLOSED, "Modo2"),
                r("4", "Dan", "Alice", "toxic", 500L, Report.STATUS_DELETED, null)));
    }

    @Test
    public void filterKeepsOnlyItsStatus() {
        List<Report> open = ReportFilters.apply(sample(), Report.STATUS_OPEN, "NEWEST", null, null);
        assertEquals(1, open.size());
        assertEquals("2", open.get(0).getId());

        List<Report> inProgress = ReportFilters.apply(sample(), Report.STATUS_IN_PROGRESS, "NEWEST", null, null);
        assertEquals(1, inProgress.size());
        assertEquals("1", inProgress.get(0).getId());

        List<Report> closed = ReportFilters.apply(sample(), Report.STATUS_CLOSED, "NEWEST", null, null);
        assertEquals(1, closed.size());
        assertEquals("3", closed.get(0).getId());

        List<Report> deleted = ReportFilters.apply(sample(), Report.STATUS_DELETED, "NEWEST", null, null);
        assertEquals(1, deleted.size());
        assertEquals("4", deleted.get(0).getId());
    }

    /** Un rapport supprimé ne doit JAMAIS remonter dans la file ouverte. */
    @Test
    public void deletedLeaksIntoNoFilter() {
        List<Report> open = ReportFilters.apply(sample(), Report.STATUS_OPEN, "NEWEST", null, null);
        for (Report rep : open) {
            assertTrue(!Report.STATUS_DELETED.equals(rep.getStatus()), "un DELETED s'est glissé dans OPEN");
        }
    }

    @Test
    public void sortNewestAndOldest() {
        List<Report> newest = ReportFilters.apply(sample(), Report.STATUS_OPEN, "NEWEST", null, null);
        assertEquals("2", newest.get(0).getId()); // seul OPEN

        List<Report> all = ReportFilters.apply(sample(), null, "NEWEST", null, null); // pas de filtre = tout
        assertEquals(Arrays.asList("1", "2", "3", "4"), ids(all));

        List<Report> oldest = ReportFilters.apply(sample(), null, "OLDEST", null, null);
        assertEquals(Arrays.asList("4", "3", "2", "1"), ids(oldest));
    }

    @Test
    public void onlyMineKeepsHandlerReports() {
        // onlyMine compare l'UUID du responsable (le staff connecté), pas son pseudo
        List<Report> mine = ReportFilters.apply(sample(), null, "NEWEST", "uuid-Modo1", null);
        assertEquals(1, mine.size());
        assertEquals("1", mine.get(0).getId());
    }

    @Test
    public void queryMatchesReporterReportedReasonAndHandler() {
        assertEquals(1, ReportFilters.apply(sample(), null, "NEWEST", null, "force").size());
        assertEquals(1, ReportFilters.apply(sample(), null, "NEWEST", null, "grief").size());
        // "ste" matche le signalé STEVE dans 2 rapports
        assertEquals(2, ReportFilters.apply(sample(), null, "NEWEST", null, "ste").size());
        // "modo2" matche le responsable du rapport 3
        assertEquals(1, ReportFilters.apply(sample(), null, "NEWEST", null, "modo2").size());
        // requête inconnue -> vide
        assertTrue(ReportFilters.apply(sample(), null, "NEWEST", null, "zzz").isEmpty());
    }

    @Test
    public void filterAndQueryCombine() {
        // rapport 1 : IN_PROGRESS avec "ste" dans le signalé
        assertEquals(1, ReportFilters.apply(sample(), Report.STATUS_IN_PROGRESS, "NEWEST", null, "ste").size());
        // rapport 3 est CLOSED : filtrer OPEN + "ste" ne doit rien renvoyer
        assertTrue(ReportFilters.apply(sample(), Report.STATUS_OPEN, "NEWEST", null, "ste").isEmpty());
    }

    @Test
    public void sortOpenFirstGroupsByStatusThenDate() {
        List<Report> sorted = ReportFilters.apply(sample(), null, ReportFilters.SORT_OPEN_FIRST, null, null);
        // 2 (OPEN) puis 1 (IN_PROGRESS) puis 3 (CLOSED) puis 4 (DELETED)
        assertEquals(Arrays.asList("2", "1", "3", "4"), ids(sorted));
    }

    private static List<String> ids(List<Report> reports) {
        List<String> out = new ArrayList<>();
        for (Report rep : reports) {
            out.add(rep.getId());
        }
        return out;
    }
}
