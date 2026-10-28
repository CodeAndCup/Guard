package fr.perrier.guard.commands.annotations.defaults;


import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.utils.*;
import org.apache.commons.lang.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.util.*;
import java.util.stream.*;

public class BooleanParameterType implements ParameterType<Boolean> {

    static final Map<String, Boolean> MAP = new HashMap<>();

    static {
        MAP.put("true", true);
        MAP.put("on", true);
        MAP.put("oui", true);

        MAP.put("false", false);
        MAP.put("off", false);
        MAP.put("non", false);
    }

    public Boolean transform(CommandSender sender, String source) {
        if (!MAP.containsKey(source.toLowerCase())) {
            sender.sendMessage(ChatUtil.translate("&cPlease enter 'true' or 'false'"));
            return (null);
        }

        return MAP.get(source.toLowerCase());
    }

    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        return (MAP.keySet().stream().filter(string -> StringUtils.startsWithIgnoreCase(string, source)).collect(Collectors.toList()));
    }

}