package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.manager.menu.moderation.ViewEnderChestMenu;
import fr.perrier.guard.manager.menu.moderation.ViewInventoryMenu;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.*;
import org.bukkit.entity.*;

public class InventoryView {


    @Command(names = CommandsUtils.INVENTORY_VIEW, perm = "guard.inventory")
    public static void onInventoryView(Player player, @Param(name = "Player")String starget, @Param(name = "type", baseValue = "inv")String type) {
        if(!Guard.getDefaultConfig().getBoolean("commands.inventory.active")) {
            Messages.send(player,Messages.COMMANDDISABLE);
            return;
        }
        Player target = Bukkit.getPlayer(starget);
        if(target == null) {
            Messages.send(player,Messages.PLAYERNOTFOUND);
            return;
        }
        if(target.equals(player)) {
            player.sendMessage(ChatUtil.prefix("&cHeeiiin ?!"));
            return;
        }
        if(type.equals("inv") || type.equals("inventory")) {
            new ViewInventoryMenu(target).openMenu(player); 
        } else if (type.equalsIgnoreCase("enderchest") || type.equalsIgnoreCase("ec")) {
            new ViewEnderChestMenu(target).openMenu(player);
        }else {
            player.sendMessage(ChatUtil.prefix("&c/invsee <Player> [inv/ec]"));
        }

    }
}
