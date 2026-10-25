package fr.perrier.guard.reports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Contrat du store YAML (fallback mono-serveur) : submit/listOpen/resolve + persistance réelle du fichier. */
public class YamlReportStoreTest {

    @TempDir
    Path temp;

    @Test
    public void submitAssignsUuidIdAndPersists() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        Report r = new Report(null, "Alex", "Steve", "hacks", "lobby", System.currentTimeMillis(), Report.STATUS_OPEN, null);
        Report saved = store.submit(r).join();
        // id = UUID valide, assigné par le store
        assertEquals(r, saved);
        assertNotNull(saved.getId());
        UUID.fromString(saved.getId()); // jette si invalide
        List<Report> open = store.listOpen().join();
        assertEquals(1, open.size());
        assertEquals(saved.getId(), open.get(0).getId());
        assertEquals("Alex", open.get(0).getReporter());
        assertEquals(Report.STATUS_OPEN, open.get(0).getStatus());
    }

    @Test
    public void resolveRemovesFromOpen() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        Report saved = store.submit(new Report(null, "A", "S", "r", "lobby", System.currentTimeMillis(), Report.STATUS_OPEN, null)).join();
        store.resolve(saved.getId(), "Modo", Report.STATUS_ACCEPTED).join();
        assertEquals(0, store.listOpen().join().size());
    }

    @Test
    public void fileSurvivesStoreReopen() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        String first = store.submit(new Report(null, "Alex", "Steve", "hacks", "lobby", System.currentTimeMillis(), Report.STATUS_OPEN, null)).join().getId();
        List<Report> open = new YamlReportStore(temp.resolve("reports.yml").toFile()).listOpen().join();
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
        CompletableFuture<Void> f = store.resolve(UUID.randomUUID().toString(), "Modo", Report.STATUS_REJECTED);
        assertThrows(java.util.concurrent.CompletionException.class, f::join);
    }
}
