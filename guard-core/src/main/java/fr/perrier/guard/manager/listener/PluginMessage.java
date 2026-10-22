package fr.perrier.guard.manager.listener;

import fr.perrier.guard.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class PluginMessage implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        String clientBrand = new String(message, StandardCharsets.UTF_8);

        Bukkit.getOnlinePlayers().forEach(staff -> {
            if(staff.hasPermission("guard.lookup")) {
                staff.sendMessage(ChatUtil.prefix("&b" + player.getName() + "&fjoined with &7: &3" + clientBrand));
            }
        });
    }
}
