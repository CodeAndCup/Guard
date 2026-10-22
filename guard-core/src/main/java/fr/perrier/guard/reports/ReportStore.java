package fr.perrier.guard.reports;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Persistance des reports. Toutes les méthodes sont asynchrones ; la DB/YAML est source de vérité. */
public interface ReportStore {

    /** Persiste et remplit l'id (appelant passe id=0). */
    CompletableFuture<Report> submit(Report report);

    /** Reports ouverts, du plus récent au plus ancien. */
    CompletableFuture<List<Report>> listOpen();

    /** outcome = Report.STATUS_ACCEPTED ou STATUS_REJECTED. */
    CompletableFuture<Void> resolve(int id, String by, String outcome);
}
