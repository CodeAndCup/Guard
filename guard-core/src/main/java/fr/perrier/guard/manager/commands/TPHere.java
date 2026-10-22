package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.*;
import org.bukkit.entity.*;

public class TPHere {

    @Command(names = CommandsUtils.TPHERE, perm = "guard.tphere")
    public static void onTPHere(Player player, @Param(name = "Player")String starget) {
        if(!Guard.getDefaultConfig().getBoolean("commands.teleporthere.active")) {
            Messages.send(player,Messages.COMMANDDISABLE);
            return;
        }
        Player target = Bukkit.getPlayer(starget);
        if(target == null) {
            Messages.send(player,Messages.PLAYERNOTFOUND);
            return;
        }
        if(target.equals(player)) {
            player.sendMessage(ChatUtil.prefix("&cOkkk.."));
            return;
        }
        target.teleport(player.getLocation());
        player.sendMessage(ChatUtil.prefix(Messages.COMMANDS_TELEPORTHERE_CONFIRMATION.getMessage().replace("%target%",target.getName())));
    }
}
