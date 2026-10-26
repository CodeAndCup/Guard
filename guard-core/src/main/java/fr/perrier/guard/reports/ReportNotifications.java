package fr.perrier.guard.reports;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Toggle de notifications par staff (/reports on|off).
 *
 * Appel du builder : état persisté dans un fichier LOCAL (works sans base de données).
 * Conséquence assumée — sur un réseau multi-serveurs, l'état est par serveur
 * (un modo coupé sur « survie » reste abonné sur « pvp »). Un stockage partagé en base
 * reste possible plus tard sans changer l'interface.
 *
 * Modèle : un défaut global ({@code reports.notify-default}) + les EXCEPTIONS explicites
 * par joueur. Le fichier ne stocke que les exceptions.
 */
public final class ReportNotifications {

    private static boolean defaultEnabled = true;
    private static final Map<UUID, Boolean> OVERRIDE = new HashMap<>();
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
        defaultEnabled = true;
        OVERRIDE.clear();
        load();
    }

    /** @param hasNotifyPerm true = le staff N'A PAS la permission de se désabonner (reçoit quoi qu'il arrive). */
    public static boolean wantsNotifications(UUID player, boolean hasNotifyPerm) {
        if (hasNotifyPerm) {
            return true;
        }
        Boolean override = OVERRIDE.get(player);
        return override == null ? defaultEnabled : override;
    }

    public static void setEnabled(UUID player, boolean enabled) {
        OVERRIDE.put(player, enabled);
        save();
    }

    /** Bascule l'état d'un joueur et renvoie le nouvel état. */
    public static boolean toggle(Player player) {
        boolean nowEnabled = !wantsNotifications(player.getUniqueId(), false);
        setEnabled(player.getUniqueId(), nowEnabled);
        return nowEnabled;
    }

    private static void load() {
        OVERRIDE.clear();
        // défaut : fichier s'il existe, sinon config, sinon true
        defaultEnabled = configDefault();
        if (file == null || !file.exists()) {
            return;
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        if (yml.contains("default-enabled")) {
            defaultEnabled = yml.getBoolean("default-enabled");
        }
        for (String key : yml.getStringList("disabled")) {
            try {
                OVERRIDE.put(UUID.fromString(key), Boolean.FALSE);
            } catch (IllegalArgumentException ignored) {
            }
        }
        for (String key : yml.getStringList("enabled")) {
            try {
                OVERRIDE.put(UUID.fromString(key), Boolean.TRUE);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private static void save() {
        if (file == null) {
            return;
        }
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("default-enabled", defaultEnabled);
        List<String> disabled = new ArrayList<>();
        List<String> enabled = new ArrayList<>();
        for (Map.Entry<UUID, Boolean> entry : OVERRIDE.entrySet()) {
            (entry.getValue() ? enabled : disabled).add(entry.getKey().toString());
        }
        yml.set("disabled", disabled);
        yml.set("enabled", enabled);
        try {
            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** config reports.notify-default ; tolerant au test sans serveur (config absente → true). */
    private static boolean configDefault() {
        try {
            org.bukkit.configuration.file.FileConfiguration cfg = fr.perrier.guard.Guard.getDefaultConfig();
            return cfg == null || cfg.getBoolean("reports.notify-default", true);
        } catch (RuntimeException | NoClassDefFoundError e) {
            return true;
        }
    }
}
