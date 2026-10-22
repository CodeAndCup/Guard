package fr.perrier.guard.commands;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.util.*;

public final class CommandMap extends SimpleCommandMap {

    static Map<UUID, String[]> parameters = new HashMap<>();

    public CommandMap(Server server) {
        super(server);
    }
    @Override
    public List<String> tabComplete(CommandSender sender, String cmdLine) {

        ArrayList<String> players = new ArrayList<>();
        for(Player player : Bukkit.getOnlinePlayers())
            players.add(player.getDisplayName());
        return players;
    }

}
