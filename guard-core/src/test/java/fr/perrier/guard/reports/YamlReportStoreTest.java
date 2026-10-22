package fr.perrier.guard.reports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Contrat du store YAML (fallback mono-serveur) : submit/listOpen/resolve + persistance réelle du fichier. */
public class YamlReportStoreTest {

    @TempDir
    Path temp;

    @Test
    public void submitAssignsIdAndPersists() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        Report r = new Report(0, "Alex", "Steve", "hacks", "lobby", System.currentTimeMillis(), Report.STATUS_OPEN, null);
        Report saved = store.submit(r).join();
        assertNotEquals(0, saved.getId());
        List<Report> open = store.listOpen().join();
        assertEquals(1, open.size());
        assertEquals(saved.getId(), open.get(0).getId());
        assertEquals("Alex", open.get(0).getReporter());
        assertEquals(Report.STATUS_OPEN, open.get(0).getStatus());
    }

    @Test
    public void resolveRemovesFromOpen() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        Report saved = store.submit(new Report(0, "A", "S", "r", "lobby", System.currentTimeMillis(), Report.STATUS_OPEN, null)).join();
        store.resolve(saved.getId(), "Modo", Report.STATUS_ACCEPTED).join();
        assertEquals(0, store.listOpen().join().size());
    }

    @Test
    public void fileSurvivesStoreReopen() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        int first = store.submit(new Report(0, "Alex", "Steve", "hacks", "lobby", System.currentTimeMillis(), Report.STATUS_OPEN, null)).join().getId();
        List<Report> open = new YamlReportStore(temp.resolve("reports.yml").toFile()).listOpen().join();
        assertEquals(1, open.size());
        assertEquals(first, open.get(0).getId());
        assertTrue(open.get(0).getId() > 0);
    }

    @Test
    public void idsKeepIncrementingAcrossReopen() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        int first = store.submit(new Report(0, "A", "S", "r", "lobby", 0, Report.STATUS_OPEN, null)).join().getId();
        int second = new YamlReportStore(temp.resolve("reports.yml").toFile())
                .submit(new Report(0, "B", "T", "r2", "lobby", 0, Report.STATUS_OPEN, null)).join().getId();
        assertTrue(second > first);
    }

    @Test
    public void resolvedSurvivesReopen() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        int id = store.submit(new Report(0, "A", "S", "r", "lobby", 0, Report.STATUS_OPEN, null)).join().getId();
        new YamlReportStore(temp.resolve("reports.yml").toFile()).resolve(id, "Modo", Report.STATUS_REJECTED).join();
        CompletableFuture<List<Report>> f = new YamlReportStore(temp.resolve("reports.yml").toFile()).listOpen();
        assertEquals(0, f.join().size());
    }
}
