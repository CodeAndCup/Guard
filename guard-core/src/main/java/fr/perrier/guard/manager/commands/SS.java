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
    public static void onSanctionSet(Player player, @Param(name = "Joueur", baseValue = "§", tabCompleteFlags = "players")String targetName) {
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
        new SanctionScreen(null, target).openMenu(player);
    }

    /** Commande console de la config (sanction.<cat>) exécutée par le staff [%target%/%duration%/%reason%].
     *  Point unique utilisé par SanctionScreen (fusion des anciens sous-menus SS) et les hotbars. */
    public static void runSanction(Player staff, String category, Player target, String duration, String reason) {
        String sanction = Guard.getDefaultConfig().getString("sanction." + category.toLowerCase());
        if (sanction == null) {
            throw new IllegalStateException("sanction." + category.toLowerCase() + " manquant dans la config");
        }
        sanction = sanction.replace("%target%", target.getName());
        sanction = sanction.replace("%duration%", duration.replace("Perm",
                Objects.requireNonNull(Guard.getDefaultConfig().getString("sanction.duration_permanant"))));
        sanction = sanction.replace("%reason%", reason);
        staff.performCommand(sanction);
        staff.closeInventory();
    }
}
