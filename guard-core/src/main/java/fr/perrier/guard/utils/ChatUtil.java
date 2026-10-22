package fr.perrier.guard.utils;

import net.md_5.bungee.api.*;

public class ChatUtil {

    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    public static String prefix(String message) {
        return Messages.prefix() + ChatColor.translateAlternateColorCodes('&',message);
    }

}
