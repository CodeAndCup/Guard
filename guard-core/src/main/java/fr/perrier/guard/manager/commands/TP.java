package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.*;
import org.bukkit.entity.*;

public class TP {

    @Command(names = CommandsUtils.TP, perm = "guard.tp")
    public static void onTP(Player player, @Param(name = "Player")String starget) {
        if(!Guard.getDefaultConfig().getBoolean("commands.teleport.active")) {
            Messages.send(player,Messages.COMMANDDISABLE);
            return;
        }
        Player target = Bukkit.getPlayer(starget);
        if(target == null) {
            Messages.send(player,Messages.PLAYERNOTFOUND);
            return;
        }
        if(target.equals(player)) {
            player.sendMessage(ChatUtil.translate("&eHerobrine joined the game"));
            return;
        }
        player.teleport(target.getLocation());
        player.sendMessage(Messages.prefix() + ChatUtil.translate(Messages.COMMANDS_TELEPORT_CONFIRMATION.getMessage().replace("%target%",target.getName())));
    }
}
