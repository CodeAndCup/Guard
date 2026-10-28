package fr.perrier.guard.commands.annotations.defaults;

import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.utils.*;
import org.apache.commons.lang.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.util.*;

public class PlayerParameterType implements ParameterType<Player> {

    public Player transform(CommandSender sender, String source) {
        if (sender instanceof Player && (source.equalsIgnoreCase("self") || source.equals(""))) {
            return ((Player) sender);
        }
        if (!(sender instanceof Player) && (source.equalsIgnoreCase("self") || source.equals(""))) {
            sender.sendMessage(ChatUtil.prefix("&cDo you have a 2nd you ?"));
            return (null);
        }

        Player player = Bukkit.getPlayer(source);

        if (player == null) {
            sender.sendMessage(ChatUtil.prefix(Messages.PLAYERNOTFOUND.getMessage()));
            return (null);
        }

        return (player);
    }

    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        List<String> completions = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (StringUtils.startsWithIgnoreCase(player.getName(), source) && (sender.canSee(player))) {
                completions.add(player.getName());
            }
        }

        return (completions);
    }

}