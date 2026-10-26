package fr.perrier.guard.reports;

import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.commands.Freeze;
import fr.perrier.guard.network.Codec;
import fr.perrier.guard.network.GuardFrame;
import fr.perrier.guard.network.MessengerHolder;
import fr.perrier.guard.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * Flash inter-serveurs du canal TYPE_NOTIFY : payload "kind\u0000server\u0000text".
 * Kinds : REPORT (alerte staff reports), XRAY (alerte staff xray), FREEZE / UNFREEZE (réplication du gel).
 * Wiring au boot (tâche 6) ; émission statique pour émetteurs locaux.
 */
public class ReportFlash {

    public static final String KIND_REPORT = "REPORT";
    public static final String KIND_XRAY = "XRAY";
    public static final String KIND_FREEZE = "FREEZE";
    public static final String KIND_UNFREEZE = "UNFREEZE";

    /** Émission : display local PUIS flash vers tous les serveurs. Pourquoi l'affichage local :
     * le Forward BungeeCord n'est jamais délivré au serveur émetteur (pas de loopback) — sans
     * affichage local, les mods du serveur source (et le mode mono-serveur) ne verraient jamais l'alerte. */
    public static void broadcast(String kind, String text) {
        text = text.replace("\u0000", " "); // un \u0000 dans le texte casserait le parse du payload (3 parts) sur chaque serveur
        String payload = kind + "\u0000" + Guard.getDefaultConfig().getString("network.server-name", "Serveur")
                + "\u0000" + text;
        if (KIND_REPORT.equals(kind) || KIND_XRAY.equals(kind)) {
            alert(KIND_REPORT.equals(kind) ? "guard.reports.view" : "guard.xray",
                    KIND_REPORT.equals(kind) ? "commands.report.flash" : "commands.report.xray-flash", text);
        }
        // pour FREEZE/UNFREEZE : pas d'affichage local (le staff local qui a gelé est déjà informé par Freeze.onFreeze)
        MessengerHolder.get().broadcast(Codec.TYPE_NOTIFY, payload);
    }

    /** Pur : payload → {kind, server, text} ; null si malformé (drop, jamais d'exception). */
    public static String[] parse(String payload) {
        String[] parts = payload.split("\u0000");
        return parts.length == 3 ? parts : null;
    }

    public static Consumer<GuardFrame> networkHandler() {
        return frame -> {
            String[] parts = parse(frame.getPayload());
            if (parts == null) {
                return;
            }
            String kind = parts[0];
            String text = parts[2];
            switch (kind) {
                case KIND_REPORT:
                    alert("guard.reports.view", "commands.report.flash", text);
                    break;
                case KIND_XRAY:
                    alert("guard.xray", "commands.report.xray-flash", text);
                    break;
                case KIND_FREEZE:
                    applyFreeze(text, true);
                    break;
                case KIND_UNFREEZE:
                    applyFreeze(text, false);
                    break;
                default:
                    // kind inconnu d'une version plus récente : drop silencieux
            }
        };
    }

    private static void alert(String perm, String langKey, String text) {
        String rendered = ChatUtil.translate(Guard.getLangConfig().getString(langKey,
                "&b[Guard] &f" + text).replace("%text%", text));
        for (Player staff : Bukkit.getOnlinePlayers()) {
            // perm OU (pas la perm notify et désabonné) → pas de flash : le toggle réseau s'applique
            // aussi à la réception, pas seulement aux événements locaux
            if (staff.hasPermission(perm) && wantsNotifications(staff)) {
                staff.sendMessage(rendered);
            }
        }
    }

    /** Vrai si ce staff veut recevoir les alertes (désinscription via /reports off).
     *  SANS la permission guard.reports.notify : reçoit quoi qu'il arrive (anti-accident). */
    private static boolean wantsNotifications(Player staff) {
        return ReportNotifications.wantsNotifications(staff.getUniqueId(),
                !staff.hasPermission("guard.reports.notify"));
    }

    private static void applyFreeze(String targetName, boolean freeze) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            return;
        }
        if (freeze) {
            Freeze.freezePlayer(target);
            alert("guard.freeze", "commands.report.cross-freeze", targetName);
        } else {
            Freeze.unfreezePlayer(target);
        }
    }
}
