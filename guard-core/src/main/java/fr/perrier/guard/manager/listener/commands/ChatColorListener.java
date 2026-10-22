package fr.perrier.guard.manager.listener.commands;

import fr.perrier.guard.manager.commands.*;
import fr.perrier.guard.utils.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public class ChatColorListener implements Listener {

    @EventHandler
    public void AsyncChatEvent(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        Player p = e.getPlayer();
        if(ChatColor.chatcholor.contains(p)) {
            message = message.replace(message, ChatUtil.translate(Messages.COMMANDS_CHATCOLOR_COLOR.getMessage()) + message);
            e.setMessage(message);
        }
    }
}
