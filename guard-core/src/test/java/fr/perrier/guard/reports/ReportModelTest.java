package fr.perrier.guard.reports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Contrat du modèle v2 : statuts de cycle de vie + verdict, responsable, positions. */
public class ReportModelTest {

    @TempDir
    Path temp;

    /** Tous les nouveaux champs survivent à un aller-retour par le store YAML. */
    @Test
    public void newFieldsRoundTripThroughStore() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        Report r = new Report(null, "Alex", "Steve", "forcefield", "lobby",
                1_700_000_000_000L, Report.STATUS_OPEN, null);
        r.setOutcome(Report.OUTCOME_ACCEPTED);
        r.setHandler("Modo");
        r.setHandlerUuid("uuid-modo");
        r.setResolvedAt(1_700_000_100_000L);
        r.setLocationWorld("world");
        r.setLocationX(12.5);
        r.setLocationY(64.0);
        r.setLocationZ(-3.25);
        r.setLocationYaw(90.0F);
        r.setLocationPitch(-15.0F);

        Report saved = store.submit(r).join();
        List<Report> all = store.listAll().join();
        assertEquals(1, all.size());
        Report back = all.get(0);

        assertEquals(saved.getId(), back.getId());
        assertEquals(Report.OUTCOME_ACCEPTED, back.getOutcome());
        assertEquals("Modo", back.getHandler());
        assertEquals("uuid-modo", back.getHandlerUuid());
        assertEquals(1_700_000_100_000L, back.getResolvedAt());
        assertEquals("world", back.getLocationWorld());
        assertEquals(12.5, back.getLocationX(), 0.0001);
        assertEquals(64.0, back.getLocationY(), 0.0001);
        assertEquals(-3.25, back.getLocationZ(), 0.0001);
        assertEquals(90.0F, back.getLocationYaw(), 0.0001F);
        assertEquals(-15.0F, back.getLocationPitch(), 0.0001F);
    }

    /** Accepter/rejeter produit désormais CLOSED + verdict (les anciens statuts n'existent plus). */
    @Test
    public void resolveWritesClosedPlusOutcome() {
        YamlReportStore store = new YamlReportStore(temp.resolve("reports.yml").toFile());
        Report saved = store.submit(new Report(null, "A", "S", "r", "lobby", 0,
                Report.STATUS_OPEN, null)).join();

        store.resolve(saved.getId(), "Modo", Report.OUTCOME_ACCEPTED).join();

        Report back = store.listAll().join().get(0);
        assertEquals(Report.STATUS_CLOSED, back.getStatus());
        assertEquals(Report.OUTCOME_ACCEPTED, back.getOutcome());
        assertEquals("Modo", back.getResolvedBy());
        assertTrue(back.getResolvedAt() > 0);
    }

    /** Les quatre statuts de cycle de vie existent et sont distincts. */
    @Test
    public void lifecycleStatusesExist() {
        assertEquals("OPEN", Report.STATUS_OPEN);
        assertEquals("IN_PROGRESS", Report.STATUS_IN_PROGRESS);
        assertEquals("CLOSED", Report.STATUS_CLOSED);
        assertEquals("DELETED", Report.STATUS_DELETED);
        assertEquals(4, java.util.stream.Stream.of(Report.STATUS_OPEN, Report.STATUS_IN_PROGRESS,
                Report.STATUS_CLOSED, Report.STATUS_DELETED).distinct().count());
    }
}
