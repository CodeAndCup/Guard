package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.commands.CommandsUtils;
import lombok.*;
import org.bukkit.entity.*;

public class ChatLock {

    @Getter
    public static boolean chatlock = false;

    @Command(names = CommandsUtils.CHATLOCK, perm = "guard.chatlock")
    public static void onChatLock(Player player) {
        if(!Guard.getDefaultConfig().getBoolean("commands.chatlock.active")) {
            Messages.send(player,Messages.COMMANDDISABLE);
            return;
        }
        chatlock = !chatlock;
        Messages.send(player,(chatlock ? Messages.COMMANDS_CHATLOCK_ACTIVATED : Messages.COMMANDS_CHATLOCK_DESACTIVATED));
    }
}
