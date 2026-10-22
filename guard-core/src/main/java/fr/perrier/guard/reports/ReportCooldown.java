package fr.perrier.guard.reports;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Cooldown /report : pur, horloge passée en paramètre pour être testable. */
public final class ReportCooldown {

    private static final Map<UUID, Long> lastReport = new ConcurrentHashMap<>();

    private ReportCooldown() {
    }

    /** Millisecondes restantes (0 = libre). */
    public static long cooldownLeft(UUID uuid, long nowMillis, long cooldownMillis) {
        Long last = lastReport.get(uuid);
        if (last == null) {
            return 0;
        }
        long left = cooldownMillis - (nowMillis - last);
        return left > 0 ? left : 0;
    }

    public static void remember(UUID uuid, long nowMillis) {
        lastReport.put(uuid, nowMillis);
    }
}
