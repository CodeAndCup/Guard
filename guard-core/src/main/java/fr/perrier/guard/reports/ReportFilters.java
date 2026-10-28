package fr.perrier.guard.reports;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Filtres et tris de la file des signalements — 100 % pur (des Report + des String).
 * La GUI applique ceci au snapshot synchrone de {@link ReportCache}.
 */
public final class ReportFilters {

    public static final String SORT_NEWEST = "NEWEST";
    public static final String SORT_OLDEST = "OLDEST";
    /** OPEN d'abord, puis IN_PROGRESS, puis le reste — au sein de chaque groupe, du plus récent au plus ancien. */
    public static final String SORT_OPEN_FIRST = "OPEN_FIRST";

    private ReportFilters() {
    }

    /**
     * @param filter   statut à garder, ou null pour tout
     * @param sort     {@link #SORT_NEWEST} / {@link #SORT_OLDEST} / {@link #SORT_OPEN_FIRST} (null = NEWEST)
     * @param onlyMineUuid  si non-null : ne garder que les rapports dont ce staff est responsable
     * @param query    si non-null : sous-chaîne (insensible à la casse) cherchée dans
     *                 signalant, signalé, raison ou responsable
     */
    public static List<Report> apply(List<Report> all, String filter, String sort,
                                     String onlyMineUuid, String query) {
        String q = query == null ? null : query.toLowerCase(java.util.Locale.ROOT);
        List<Report> out = new ArrayList<>();
        for (Report report : all) {
            if (report == null) {
                continue;
            }
            if (filter != null && !filter.equals(report.getStatus())) {
                continue;
            }
            if (onlyMineUuid != null && !onlyMineUuid.equals(report.getHandlerUuid())) {
                continue;
            }
            if (q != null && !contains(report, q)) {
                continue;
            }
            out.add(report);
        }
        Comparator<Report> byDate = Comparator.comparingLong(Report::getCreatedAt).reversed();
        if (SORT_OLDEST.equalsIgnoreCase(sort)) {
            byDate = Comparator.comparingLong(Report::getCreatedAt);
        } else if (SORT_OPEN_FIRST.equalsIgnoreCase(sort)) {
            // statut D'ABORD, date seulement pour départager dans un même groupe
            byDate = Comparator.comparingInt(ReportFilters::openRank)
                    .thenComparing(Comparator.comparingLong(Report::getCreatedAt).reversed());
        }
        out.sort(byDate);
        return out;
    }

    private static int openRank(Report report) {
        if (Report.STATUS_OPEN.equals(report.getStatus())) {
            return 0;
        }
        if (Report.STATUS_IN_PROGRESS.equals(report.getStatus())) {
            return 1;
        }
        return 2;
    }

    private static boolean contains(Report report, String q) {
        return hay(report.getId(), q) || hay(report.getReporter(), q) || hay(report.getReporterUuid(), q)
                || hay(report.getReported(), q) || hay(report.getReportedUuid(), q)
                || hay(report.getReason(), q) || hay(report.getHandler(), q);
    }

    private static boolean hay(String value, String q) {
        return value != null && value.toLowerCase(java.util.Locale.ROOT).contains(q);
    }
}
