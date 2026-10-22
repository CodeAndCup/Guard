package fr.perrier.guard.commands;

import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.commands.annotations.Command;
import fr.perrier.guard.utils.*;
import lombok.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.lang.reflect.Method;
import java.util.*;

@Getter
public final class CommandData {

    final String[] names;
    final String permission;
    final boolean async;
    final List<ParameterData> parameters;
    final Method method;
    final boolean consoleAllowed;

    public CommandData(Command commandAnnotation, List<ParameterData> parameters, Method method, boolean consoleAllowed) {
        this.names = commandAnnotation.names().getNames();
        this.permission = commandAnnotation.perm();
        this.async = commandAnnotation.async();
        this.parameters = parameters;
        this.method = method;
        this.consoleAllowed = consoleAllowed;
    }

    public String getName() {
        return (names[0]);
    }

    public boolean canAccess(CommandSender sender) {
        // Console can do anything.
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        return player.hasPermission(getPermission());
    }

    public String getUsageString() {
        return (getUsageString(getName()));
    }

    public String getUsageString(String aliasUsed) {
        StringBuilder stringBuilder = new StringBuilder();

        for (ParameterData paramHelp : getParameters()) {
            boolean needed = paramHelp.getDefaultValue().isEmpty();
            stringBuilder.append(needed ? "<" : "[").append(paramHelp.getName());
            stringBuilder.append(needed ? ">" : "]").append(" ");
        }

        return ("/" + aliasUsed.toLowerCase() + " " + stringBuilder.toString().trim().toLowerCase());
    }

    public void execute(CommandSender sender, String[] params) {
        List<Object> transformedParameters = new ArrayList<>();

        transformedParameters.add(sender);

        for (int parameterIndex = 0; parameterIndex < getParameters().size(); parameterIndex++) {
            ParameterData parameter = getParameters().get(parameterIndex);
            String passedParameter = (parameterIndex < params.length ? params[parameterIndex] : parameter.getDefaultValue()).trim();

            if (parameterIndex >= params.length && parameter.getDefaultValue().isEmpty()) {
                String usage = getUsageString();
                sender.sendMessage(ChatUtil.prefix("&cPlease use " + usage));
                return;
            }

            if (parameter.isWildcard() && !passedParameter.trim().equals(parameter.getDefaultValue().trim())) {
                passedParameter = toString(params, parameterIndex);
            }

            Object result = CommandHandler.transformParameter(sender, passedParameter, parameter.getParameterClass());

            if (result == null) {
                return;
            }

            transformedParameters.add(result);

            if (parameter.isWildcard()) {
                break;
            }
        }

        try {
            method.invoke(null, transformedParameters.toArray());
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error occurred while trying to run this command, please contact support.");
            e.printStackTrace();
        }

    }

    public static String toString(String[] args, int start) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int arg = start; arg < args.length; arg++) {
            stringBuilder.append(args[arg]).append(" ");
        }

        return (stringBuilder.toString().trim());
    }

}