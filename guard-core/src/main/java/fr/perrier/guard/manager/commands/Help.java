package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;

import java.io.File;

public class Help {

    @Command(names = CommandsUtils.HELP, perm = "guard.help")
    public static void onGuardHelp(Player player, @Param(name = "option", baseValue = "none") String option) {
        if (option.isEmpty() || option.equalsIgnoreCase("none")) {
            new fr.perrier.guard.manager.menu.dashboard.DashboardMenu().openMenu(player);
        } else if (option.equalsIgnoreCase("help")) {
            String help = "&8&l&m------------------------------\n" +
                    "#0E6F7D&l          Guard\n" +
                    "&a \n" +
                    "&f- #0E6F7D/hguard #17A1B2[reload]\n" +
                    "&f- #0E6F7D/chatcolor\n" +
                    "&f- #0E6F7D/cps #17A1B2<player>\n" +
                    "&f- #0E6F7D/freeze #17A1B2<player>\n" +
                    "&f- #0E6F7D/ss #17A1B2<player>\n" +
                    "&f- #0E6F7D/tp #17A1B2<player>\n" +
                    "&f- #0E6F7D/tphere #17A1B2<player>\n" +
                    "&f- #0E6F7D/vanish\n" +
                    "&f- #0E6F7D/noclip\n" +
                    "&f- #0E6F7D/invsee #17A1B2<player> [inv/ec]\n" +
                    "&f- #0E6F7D/modlist\n" +
                    "&f- #0E6F7D/chatlock\n" +
                    "&f- #0E6F7D/xray\n" +
                    "&f- #0E6F7D/lookup #17A1B2<player>\n" +
                    "&2 \n" +
                    "#0E6F7DVersion: #17A1B2" + Guard.getInstance().getDescription().getVersion() + " \n" +
                    "#0E6F7DDiscord Support: #17A1B2discord.cupcode.fr \n" +
                    "&8&l&m------------------------------";
            player.sendMessage(ChatUtil.translate(help));
        } else if (option.equalsIgnoreCase("reload")) {

            Guard.setDefaultConfig(YamlConfiguration.loadConfiguration(Guard.getDefaultConfigFile()));

            Guard.getInstance().createLanguageConfig();
            Messages.refreshAll();
            CommandsUtils.refreshAll();
            Guard.getCommandHandler().reloadCommands();

            Messages.send(player,Messages.RELOADED);
        }
    }
}
