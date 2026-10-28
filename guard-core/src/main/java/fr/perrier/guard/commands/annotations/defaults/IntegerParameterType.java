package fr.perrier.guard.commands.annotations.defaults;

import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.utils.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.util.*;

public class IntegerParameterType implements ParameterType<Integer> {

    public Integer transform(CommandSender sender, String source) {
        try {
            return (Integer.parseInt(source));
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatUtil.prefix(ChatColor.RED + source + " is not a valid number."));
            return (null);
        }
    }

    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        return (new ArrayList<>());
    }

}