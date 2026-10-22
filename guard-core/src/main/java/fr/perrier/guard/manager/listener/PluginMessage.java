package fr.perrier.guard.manager.listener;

import fr.perrier.guard.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PluginMessage implements PluginMessageListener {

    /** Dernier brand client connu par joueur ( recyclé à la déconnexion par PlayerLeave). */
    private static final Map<UUID, String> BRANDS = new ConcurrentHashMap<>();

    /** Pur : brand affiché à l'onglet Info du profil, "?" tant qu'aucun message recu. */
    public static String brandOf(Player player) {
        String brand = BRANDS.get(player.getUniqueId());
        return brand == null ? "?" : brand;
    }

    public static void forget(Player player) {
        BRANDS.remove(player.getUniqueId());
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        String clientBrand = new String(message, StandardCharsets.UTF_8);
        BRANDS.put(player.getUniqueId(), clientBrand);

        Bukkit.getOnlinePlayers().forEach(staff -> {
            if(staff.hasPermission("guard.lookup")) {
                staff.sendMessage(ChatUtil.prefix("&b" + player.getName() + "&fjoined with &7: &3" + clientBrand));
            }
        });
    }
}
