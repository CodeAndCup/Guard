package fr.perrier.guard.reports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Un signalement. Status : OPEN / ACCEPTED / REJECTED. */
@Getter
@AllArgsConstructor
public class Report {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_REJECTED = "REJECTED";

    @Setter
    private String id;
    private String reporter;
    private String reported;
    private String reason;
    private String serverName;
    private long createdAt;
    private String status;
    private String resolvedBy;
}
