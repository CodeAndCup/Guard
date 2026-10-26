package fr.perrier.guard.reports;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Persistance des rapports (source de vérité). Toutes les méthodes sont asynchrones ;
 * la DB/YAML est lue en ASYNC puis exposée en synchrone via {@link ReportCache}.
 */
public interface ReportStore {

    /** Persiste (l'id UUID est assigné ici, appelant passe id=null) et remplit le modèle. */
    CompletableFuture<Report> submit(Report report);

    /** Tous les rapports, du plus récent au plus ancien (filtrage côté appelant). */
    CompletableFuture<List<Report>> listAll();

    /** Clôture avec verdict : status=CLOSED, outcome=ACCEPTED|REJECTED. */
    CompletableFuture<Void> resolve(String id, String by, String outcome);

    /** Change le statut (OPEN / IN_PROGRESS / CLOSED / DELETED) ; efface le verdict si non CLOSED. */
    CompletableFuture<Void> setStatus(String id, String status, String by);

    /** Assigne (ou retire avec name=null) le modo responsable. */
    CompletableFuture<Void> setHandler(String id, String name, String uuid);

    /** Supprime les rapports CLOSED au-delà de la rétention ; retourne le nombre traités. */
    CompletableFuture<Integer> clearClosed(int retentionDays);
}
