package fr.perrier.guard.reports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le cache garde TOUS les statuts (la file staff doit filtrer, pas le stockage).
 * Pas de serveur Bukkit ici : on teste uniquement l'état après un cycle complet.
 */
public class ReportCacheTest {

    @TempDir
    Path temp;

    /** Après submit + resolve, un rechargement from-scratch voit les deux statuts. */
    @Test
    public void storeKeepsEveryStatus() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        store.submit(new Report(null, "A", "S", "r", "lobby", 1_000L, Report.STATUS_OPEN, null)).join();
        Report second = store.submit(new Report(null, "B", "T", "r2", "lobby", 2_000L,
                Report.STATUS_OPEN, null)).join();
        store.resolve(second.getId(), "Modo", Report.OUTCOME_REJECTED).join();
        store.submit(new Report(null, "C", "U", "r3", "lobby", 3_000L,
                Report.STATUS_IN_PROGRESS, null)).join();

        List<Report> all = store.listAll().join();
        assertEquals(3, all.size());
        // triés du plus récent au plus ancien (comportement attendu par la GUI)
        assertEquals(3_000L, all.get(0).getCreatedAt());
        assertEquals(2_000L, all.get(1).getCreatedAt());
        assertEquals(1_000L, all.get(2).getCreatedAt());
        assertEquals(Report.STATUS_CLOSED, all.get(1).getStatus());
        assertEquals(Report.OUTCOME_REJECTED, all.get(1).getOutcome());
    }

    /** setHandler / setStatus changent l'état sans toucher au reste. */
    @Test
    public void handlerAndStatusUpdates() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        Report r = store.submit(new Report(null, "A", "S", "r", "lobby", 1_000L,
                Report.STATUS_OPEN, null)).join();

        store.setHandler(r.getId(), "Modo", UUID.randomUUID().toString()).join();
        store.setStatus(r.getId(), Report.STATUS_IN_PROGRESS, "Modo").join();

        Report back = store.listAll().join().get(0);
        assertEquals("Modo", back.getHandler());
        assertEquals(Report.STATUS_IN_PROGRESS, back.getStatus());
        assertEquals("Modo", back.getResolvedBy());
        assertNull(back.getOutcome()); // pas encore de verdict
    }

    /** Un id inconnu échoue bruyamment (jamais un silence). */
    @Test
    public void unknownIdFailsLoudly() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        CompletableFuture<Void> f = store.resolve(UUID.randomUUID().toString(), "Modo", Report.OUTCOME_ACCEPTED);
        assertThrows(java.util.concurrent.CompletionException.class, f::join);
    }
}
