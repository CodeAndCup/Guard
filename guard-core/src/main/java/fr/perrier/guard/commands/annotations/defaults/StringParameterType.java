package fr.perrier.guard.commands.annotations.defaults;

import fr.perrier.guard.commands.annotations.*;
import org.apache.commons.lang3.*;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.util.*;

public class StringParameterType implements ParameterType<String> {

    /** Flag de completion : pseudo des joueurs connectes (voir @Param(tabCompleteFlags)). */
    public static final String FLAG_PLAYERS = "players";

    public String transform(CommandSender sender, String source) {
        return source;
    }

    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        // les params String restent muets par defaut, SAUF flag "players" : c'est ce qui donne
        // l'autocompletion des pseudos sur /ss, /cps, /freeze... (le type Player ferait un
        // transform strict, incompatible avec les baseValue sentinelles de ces commandes)
        if (flags == null || !flags.contains(FLAG_PLAYERS)) {
            return (new ArrayList<>());
        }
        List<String> completions = new ArrayList<>();
        String prefix = source == null ? "" : source;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (StringUtils.startsWithIgnoreCase(player.getName(), prefix) && sender.canSee(player)) {
                completions.add(player.getName());
            }
        }
        return completions;
    }
}
