package fr.perrier.guard.reports;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Toggle de notifications par staff (/reports on|off).
 *
 * Appel du builder : état persisté dans un fichier LOCAL (works sans base de données).
 * Conséquence assumée — sur un réseau multi-serveurs, l'état est par serveur
 * (un modo coupé sur « survie » reste abonné sur « pvp »). Un stockage partagé en base
 * reste possible plus tard sans changer l'interface.
 */
public final class ReportNotifications {

    private static final Set<UUID> DISABLED = new HashSet<>();
    private static File file;

    private ReportNotifications() {
    }

    /** Fichier par défaut dans le data folder (appelé au boot). */
    public static void init() {
        if (file == null) {
            file = new File(fr.perrier.guard.Guard.getInstance().getDataFolder(), "notifications.yml");
            load();
        }
    }

    /** Tests : pointe la persistance sur un fichier temporaire. */
    public static void resetForTests(File testFile) {
        file = testFile;
        DISABLED.clear();
        load();
    }

    /** @param hasNotifyPerm true = le staff N'A PAS la permission de se désabonner (reçoit quoi qu'il arrive). */
    public static boolean wantsNotifications(UUID player, boolean hasNotifyPerm) {
        return hasNotifyPerm || !DISABLED.contains(player);
    }

    public static void setEnabled(UUID player, boolean enabled) {
        if (enabled) {
            DISABLED.remove(player);
        } else {
            DISABLED.add(player);
        }
        save();
    }

    /** Bascule l'état d'un joueur et renvoie le nouvel état. */
    public static boolean toggle(Player player) {
        boolean nowEnabled = DISABLED.contains(player.getUniqueId());
        setEnabled(player.getUniqueId(), nowEnabled);
        return nowEnabled;
    }

    private static void load() {
        DISABLED.clear();
        if (file == null || !file.exists()) {
            return;
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        for (String key : yml.getStringList("disabled")) {
            try {
                DISABLED.add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private static void save() {
        if (file == null) {
            return;
        }
        YamlConfiguration yml = new YamlConfiguration();
        java.util.List<String> out = new java.util.ArrayList<>();
        for (UUID id : DISABLED) {
            out.add(id.toString());
        }
        yml.set("disabled", out);
        try {
            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
