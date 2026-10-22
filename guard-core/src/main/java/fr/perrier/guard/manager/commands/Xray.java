package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.commands.annotations.Command;
import fr.perrier.guard.manager.menu.xray.XrayMenu;
import fr.perrier.guard.utils.Messages;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.entity.Player;

public class Xray {

    @Command(names = CommandsUtils.XRAY, perm = "guard.xray")
    public static void onXray(Player player) {
        if(!Guard.getDefaultConfig().getBoolean("commands.xray.active")) {
            Messages.send(player,Messages.COMMANDDISABLE);
            return;
        }
        new XrayMenu().openMenu(player);
    }
}
