package fr.perrier.guard.commands.annotations.defaults;

import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.utils.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.util.*;

public class DoubleParameterType implements ParameterType<Double> {

    public Double transform(CommandSender sender, String source) {
        if (source.toLowerCase().contains("e")) {
            sender.sendMessage(ChatUtil.prefix(ChatColor.RED + source + " is not valid."));
            return (null);
        }

        try {
            double parsed = Double.parseDouble(source);

            if (Double.isNaN(parsed) || !Double.isFinite(parsed)) {
                sender.sendMessage(ChatUtil.prefix("&cThis number is not valid"));
                return (null);
            }

            return (parsed);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatUtil.prefix("&cThis number is not valid"));
            return (null);
        }
    }

    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        return (new ArrayList<>());
    }

}