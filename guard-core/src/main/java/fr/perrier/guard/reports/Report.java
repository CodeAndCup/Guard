package fr.perrier.guard.reports;

import lombok.Getter;
import lombok.Setter;

/**
 * Un signalement.
 * Deux axes : {@code status} = cycle de vie ({@link #STATUS_OPEN} → {@link #STATUS_IN_PROGRESS}
 * → {@link #STATUS_CLOSED}, ou {@link #STATUS_DELETED}) et {@code outcome} = verdict
 * ({@link #OUTCOME_ACCEPTED} / {@link #OUTCOME_REJECTED}), rempli à la clôture.
 * Les positions sauvegardées ({@code location*}) sont le snapshot du joueur signalé au moment
 * du rapport (récupérable même s'il se déplace ensuite).
 */
@Getter
@Setter
public class Report {

    /** Champs d'origine ; les champs v2 (verdict, responsable, positions, uuid) se posent via setters. */
    public Report(String id, String reporter, String reported, String reason,
                  String serverName, long createdAt, String status, String resolvedBy) {
        this.id = id;
        this.reporter = reporter;
        this.reported = reported;
        this.reason = reason;
        this.serverName = serverName;
        this.createdAt = createdAt;
        this.status = status;
        this.resolvedBy = resolvedBy;
    }

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String STATUS_DELETED = "DELETED";

    public static final String OUTCOME_ACCEPTED = "ACCEPTED";
    public static final String OUTCOME_REJECTED = "REJECTED";

    private String id;
    private String reporter;
    private String reporterUuid;
    private String reported;
    private String reportedUuid;
    private String reason;
    private String serverName;
    private long createdAt;
    private String status;
    private String resolvedBy;

    /* ---- v2 ---- */
    private String outcome;        // ACCEPTED / REJECTED / null (pas encore de verdict)
    private String handler;        // pseudo du modo responsable, null si personne
    private String handlerUuid;
    private long resolvedAt;       // epoch ms de clôture, 0 = jamais

    private String locationWorld;
    private double locationX;
    private double locationY;
    private double locationZ;
    private float locationYaw;
    private float locationPitch;
}
