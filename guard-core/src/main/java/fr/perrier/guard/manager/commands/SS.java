package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.manager.menu.sanctionset.*;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

import java.util.*;

public class SS implements Listener {

    @Command(names = CommandsUtils.SS, perm = "guard.ss")
    public static void onSanctionSet(Player player, @Param(name = "Joueur", baseValue = "§")String targetName) {
        if(!Guard.getDefaultConfig().getBoolean("commands.sanctionset.active")) {
            Messages.send(player,Messages.COMMANDDISABLE);
            return;
        }
        if(targetName.equalsIgnoreCase("§")) {
            new SelectPlayerSS().openMenu(player);
            return;
        }
        Player target = Bukkit.getPlayer(targetName);
        if(target == null) {
            Messages.send(player,Messages.PLAYERNOTFOUND);
            return;
        }
        new MenuSS(null,target).openMenu(player);
    }
}
