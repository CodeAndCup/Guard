package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.network.Codec;
import fr.perrier.guard.network.GuardFrame;
import fr.perrier.guard.network.MessengerHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Sync vanish inter-serveurs : chaque toggle broadcast TYPE_VANISH "uuid\u0000state" ;
 * les autres instances appliquent l'état au joueur si présent (sur le main thread, contrat Messenger).
 */
public class VanishSync {

    public static void broadcast(UUID uuid, boolean vanish) {
        MessengerHolder.get().broadcast(Codec.TYPE_VANISH, uuid.toString() + "\u0000" + vanish);
    }

    public static java.util.function.Consumer<GuardFrame> networkHandler() {
        return frame -> {
            String[] parts = frame.getPayload().split("\u0000", 2);
            if (parts.length != 2) {
                return;
            }
            UUID uuid;
            try {
                uuid = UUID.fromString(parts[0]);
            } catch (IllegalArgumentException e) {
                return;
            }
            boolean vanish = Boolean.parseBoolean(parts[1]);
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                return; // absent de ce serveur : le prochain join relira la DB
            }
            Guard.getInstance().getVanishALL().put(uuid, vanish);
            if (vanish && !Vanish.vanish.contains(p)) {
                Vanish.initVanish(p);
            } else if (!vanish && Vanish.vanish.contains(p)) {
                Vanish.removeVanish(p);
            }
        };
    }
}
