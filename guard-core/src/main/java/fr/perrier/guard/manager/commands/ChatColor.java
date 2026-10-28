package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.entity.*;

import java.util.*;

public class ChatColor {

    public static ArrayList<Player> chatcholor = new ArrayList<Player>();

    @Command(names = CommandsUtils.CHATCOLOR , perm = "guard.chatcolor")
    public static void onChatColor(Player player) {
        if(!Guard.getDefaultConfig().getBoolean("commands.chatcolor.active")) {
            Messages.send(player,Messages.COMMANDDISABLE);
            return;
        }
        if(chatcholor.contains(player)) {
            Messages.send(player,Messages.COMMANDS_CHATCOLOR_DESACTIVATED);
            chatcholor.remove(player);
        }else{
            Messages.send(player,Messages.COMMANDS_CHATCOLOR_ACTIVATED);
            chatcholor.add(player);
        }
    }
}
