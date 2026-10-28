package fr.perrier.guard.commands.annotations;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.util.*;

public interface ParameterType<T> {
    T transform(CommandSender sender, String source);

    List<String> tabComplete(Player sender, Set<String> flags, String source);
}
