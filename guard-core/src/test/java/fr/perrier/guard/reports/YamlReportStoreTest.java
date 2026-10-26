package fr.perrier.guard.reports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/** Contrat du store YAML (fallback mono-serveur) : submit/listAll/resolve/setStatus/setHandler. */
public class YamlReportStoreTest {

    @TempDir
    Path temp;

    @Test
    public void submitAssignsUuidIdAndPersists() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        Report r = new Report(null, "Alex", "Steve", "hacks", "lobby", System.currentTimeMillis(), Report.STATUS_OPEN, null);
        Report saved = store.submit(r).join();
        assertEquals(r, saved);
        assertNotNull(saved.getId());
        UUID.fromString(saved.getId()); // jette si invalide
        List<Report> open = store.listAll().join();
        assertEquals(1, open.size());
        assertEquals(saved.getId(), open.get(0).getId());
        assertEquals("Alex", open.get(0).getReporter());
        assertEquals(Report.STATUS_OPEN, open.get(0).getStatus());
    }

    @Test
    public void resolveRemovesFromOpen() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        Report saved = store.submit(new Report(null, "A", "S", "r", "lobby", System.currentTimeMillis(), Report.STATUS_OPEN, null)).join();
        store.resolve(saved.getId(), "Modo", Report.OUTCOME_ACCEPTED).join();
        // plus rien en attente : le seul rapport est passé CLOSED
        List<Report> all = store.listAll().join();
        assertEquals(1, all.size());
        assertEquals(Report.STATUS_CLOSED, all.get(0).getStatus());
    }

    @Test
    public void fileSurvivesStoreReopen() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        String first = store.submit(new Report(null, "Alex", "Steve", "hacks", "lobby", System.currentTimeMillis(), Report.STATUS_OPEN, null)).join().getId();
        List<Report> open = new YamlReportStore(temp.resolve("reports.yml").toFile()).listAll().join();
        assertEquals(1, open.size());
        assertEquals(first, open.get(0).getId());
    }

    @Test
    public void idsAreUniqueAcrossReopen() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        String first = store.submit(new Report(null, "A", "S", "r", "lobby", 0, Report.STATUS_OPEN, null)).join().getId();
        String second = new YamlReportStore(temp.resolve("reports.yml").toFile())
                .submit(new Report(null, "B", "T", "r2", "lobby", 0, Report.STATUS_OPEN, null)).join().getId();
        assertNotEquals(first, second);
    }

    @Test
    public void unknownUuidResolveFailsLoudly() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        CompletableFuture<Void> f = store.resolve(UUID.randomUUID().toString(), "Modo", Report.OUTCOME_REJECTED);
        assertThrows(java.util.concurrent.CompletionException.class, f::join);
    }

    @Test
    public void clearClosedTagsOnlyOldClosedReports() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        Report old = store.submit(new Report(null, "A", "S", "r", "lobby", 0, Report.STATUS_OPEN, null)).join();
        store.resolve(old.getId(), "Modo", Report.OUTCOME_ACCEPTED).join();
        store.setStatus(old.getId(), Report.STATUS_CLOSED, "Modo").join();
        // résolu il y a 0 jour → rétention 1 jour : rien à nettoyer ; rétention 0 : tout
        assertEquals(0, (int) store.clearClosed(1).join());
        List<Report> stillThere = store.listAll().join();
        assertEquals(1, stillThere.size());
    }
}
