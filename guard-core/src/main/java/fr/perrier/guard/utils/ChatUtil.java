package fr.perrier.guard.utils;

import fr.perrier.guard.compat.CompatHolder;
import net.md_5.bungee.api.*;

public class ChatUtil {

    public static String translate(String message) {
        return CompatHolder.get().colorize(message);
    }
    public static String prefix(String message) {
        return Messages.prefix() + ChatColor.translateAlternateColorCodes('&',message);
    }

}
