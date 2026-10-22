package fr.perrier.guard.manager.commands;

import fr.perrier.guard.*;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.*;
import org.bukkit.entity.*;

public class ModList {

    @Command(names = CommandsUtils.MOD_LIST, perm = "guard.modlist")
    public static void onModList(Player player) {
        if(!Guard.getDefaultConfig().getBoolean("commands.modlist.active")) {
            Messages.send(player,Messages.COMMANDDISABLE);
            return;
        }
        final StringBuilder modlist = new StringBuilder();

        for(String lore : Messages.COMMANDS_MODLIST_FORMAT.getLore()) {
            if(lore.contains("%target%")) {
                Guard.getInstance().getVanishALL().forEach((staff,vanish) -> {
                    if(Bukkit.getPlayer(staff) != null && Bukkit.getPlayer(staff).hasPermission("guard.vanish"))
                        modlist.append(lore.replace("%target%", Bukkit.getPlayer(staff).getDisplayName()).replace("%value%", (Vanish.vanish.contains(Bukkit.getPlayer(staff)) ? " &a[Vanish]" : " &c[UnVanish]"))).append("\n");
                });
            }else
                modlist.append(lore).append("\n");
        }
        player.sendMessage(ChatUtil.translate(String.valueOf(modlist)));
    }
}
