package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.commands.annotations.Command;
import fr.perrier.guard.network.Codec;
import fr.perrier.guard.network.GuardFrame;
import fr.perrier.guard.network.MessengerHolder;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.Messages;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Staff chat : toggle par commande et raccourci "@message".
 * Émission sur le canal (TYPE_STAFFCHAT) + affichage local ; réception via MessengerHolder (wiring au boot, tâche 6).
 */
public class StaffChat {

    public static final String AT_PREFIX = "@";

    private static final Map<UUID, Boolean> toggles = new ConcurrentHashMap<>();

    @Command(names = CommandsUtils.STAFFCHAT, perm = "guard.staffchat")
    public static void onStaffChat(Player player) {
        if(!Guard.getDefaultConfig().getBoolean("commands.staffchat.active")) {
            Messages.send(player, Messages.COMMANDDISABLE);
            return;
        }
        boolean now = !toggles.getOrDefault(player.getUniqueId(), false);
        toggles.put(player.getUniqueId(), now);
        player.sendMessage(ChatUtil.translate(Guard.getLangConfig().getString(now
                ? "commands.staffchat.enabled" : "commands.staffchat.disabled",
                now ? "&aStaff chat activé" : "&cStaff chat désactivé")));
    }

    public static boolean isToggled(UUID uuid) {
        return toggles.getOrDefault(uuid, false);
    }

    /** Pur : le message commence par le prefix "@" (et contient du texte après). */
    public static boolean isStaffChatMessage(String raw) {
        return raw != null && raw.startsWith(AT_PREFIX) && raw.length() > AT_PREFIX.length();
    }

    /** Pur : retire le leading "@" et l'espace qui suit éventuellement. */
    public static String stripPrefix(String raw) {
        if (!isStaffChatMessage(raw)) {
            return raw;
        }
        String s = raw.substring(AT_PREFIX.length());
        return s.startsWith(" ") ? s.substring(1) : s;
    }

    /** Pur : traduit les couleurs du template lang SEUL (le message du staff reste brut, pas de réinterprétation de ses codes). */
    public static String format(FileConfiguration lang, String sender, String message, String serverName) {
        String template = lang.getString("commands.staffchat.format",
                "&8[{server_name}] &b{staff} &f: {message}");
        return ChatUtil.translate(template)
                .replace("{server_name}", serverName)
                .replace("{staff}", sender)
                .replace("{message}", message);
    }

    /** Émission : broadcast canal + affichage local immédiat (l'émetteur peut aussi s'entendre via le canal, pas de dédup). */
    public static void emit(Player sender, String message) {
        MessengerHolder.get().broadcast(Codec.TYPE_STAFFCHAT, sender.getName() + "\u0000" + message);
        show(serverName(), sender.getName(), message);
    }

    /** Affichage aux staff du serveur (utilisé en local et comme handler réseau). */
    public static void show(String serverName, String senderName, String message) {
        String rendered = format(Guard.getLangConfig(), senderName, message, serverName);
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("guard.staffchat")) {
                staff.sendMessage(rendered);
            }
        }
    }

    /** Handler réseau (wiring boot T6) : payload "sender\u0000message". */
    public static Consumer<GuardFrame> networkHandler() {
        return frame -> {
            String[] parts = frame.getPayload().split("\u0000", 2);
            if (parts.length == 2) {
                show(serverName(), parts[0], parts[1]);
            }
        };
    }

    /** Nom du serveur affiché dans les messages (le nom Bungee n'est pas exposé à Bukkit). */
    private static String serverName() {
        return Guard.getDefaultConfig().getString("network.server-name", "Serveur");
    }
}
