package fr.perrier.guard.manager.listener.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.commands.StaffChat;
import fr.perrier.guard.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/** Chemin chat legacy (fonctionne aussi sur Paper). Intercepte toggle + "@message" avant les autres listeners. */
public class StaffChatListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        boolean useStaff = StaffChat.isToggled(player.getUniqueId())
                || StaffChat.isStaffChatMessage(event.getMessage());
        if (!useStaff || !player.hasPermission("guard.staffchat")) {
            return;
        }
        if (!Guard.getDefaultConfig().getBoolean("commands.staffchat.active")) {
            if (StaffChat.isStaffChatMessage(event.getMessage())) {
                // pas de blocage silencieux d'un "@" hors staffchat actif : laisser passer en chat normal
                return;
            }
            Messages.send(player, Messages.COMMANDDISABLE);
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true); // ne jamais sortir dans le chat public
        final String content = StaffChat.stripPrefix(event.getMessage()).trim();
        if (content.isEmpty()) {
            return;
        }
        // l'event est async ; l'émission (canal + affichage) repose sur le main thread
        Bukkit.getScheduler().runTask(Guard.getInstance(), () -> StaffChat.emit(player, content));
    }
}
