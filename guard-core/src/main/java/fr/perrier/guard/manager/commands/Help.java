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
        if(option.isEmpty() || option.equalsIgnoreCase("none") || option.equalsIgnoreCase("help")) {
            String help = "&8&l&m------------------------------\n" +
                    "&3&l          Guard\n" +
                    "&a \n" +
                    "&f- &3/hguard &b[reload]\n" +
                    "&f- &3/chatcolor\n" +
                    "&f- &3/cps &b<player>\n" +
                    "&f- &3/freeze &b<player>\n" +
                    "&f- &3/ss &b<player>\n" +
                    "&f- &3/tp &b<player>\n" +
                    "&f- &3/tphere &b<player>\n" +
                    "&f- &3/vanish\n" +
                    "&f- &3/noclip\n" +
                    "&f- &3/invsee &b<player> [inv/ec]\n" +
                    "&f- &3/modlist\n" +
                    "&f- &3/chatlock\n" +
                    "&f- &3/xray\n" +
                    "&f- &3/lookup &b<player>\n" +
                    "&2 \n" +
                    "&3Version: &b" + Guard.getInstance().getDescription().getVersion() + " \n" +
                    "&3Discord Support: &bdiscord.cupcode.fr \n" +
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
