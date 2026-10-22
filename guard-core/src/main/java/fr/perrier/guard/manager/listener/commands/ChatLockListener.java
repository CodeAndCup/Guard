package fr.perrier.guard.manager.listener.commands;

import fr.perrier.guard.manager.commands.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public class ChatLockListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if(ChatLock.chatlock)
            event.setCancelled(true);
    }
}
