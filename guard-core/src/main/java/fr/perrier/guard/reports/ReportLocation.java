package fr.perrier.guard.reports;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Position sauvegardée d'un signalement — extraite du modèle pour rester testable sans serveur.
 * Le tp se refuse net si le monde n'est pas chargé (pas de tp dans le vide, pas de NPE).
 */
public final class ReportLocation {

    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    private ReportLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /** null si le rapport n'a pas de snapshot exploitable. */
    public static ReportLocation of(Report report) {
        if (report == null || report.getLocationWorld() == null) {
            return null;
        }
        return new ReportLocation(report.getLocationWorld(), report.getLocationX(), report.getLocationY(),
                report.getLocationZ(), report.getLocationYaw(), report.getLocationPitch());
    }

    /** Le monde sauvegardé est-il bien celui fourni ? (comparaison par nom, testable) */
    public boolean matches(String worldName) {
        return world != null && world.equals(worldName);
    }

    /* valeurs pures (lisibles sans serveur) */
    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }
    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
    public String world() { return world; }

    /** Bukkit.Location prête à téléporter, ou null si le monde n'est pas/plus chargé. */
    public Location toLocation(String worldName) {
        if (!matches(worldName)) {
            return null;
        }
        org.bukkit.World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null) {
            return null;
        }
        return new Location(bukkitWorld, x, y, z, yaw, pitch);
    }
}
