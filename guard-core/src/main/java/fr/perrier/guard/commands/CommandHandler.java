package fr.perrier.guard.commands;

import com.google.common.base.*;
import com.google.common.collect.*;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.commands.annotations.Command;
import fr.perrier.guard.commands.annotations.defaults.*;
import fr.perrier.guard.manager.commands.*;
import fr.perrier.guard.utils.*;
import lombok.*;
import org.bukkit.*;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.*;
import org.bukkit.plugin.java.*;
import org.bukkit.scheduler.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

public class CommandHandler implements Listener {
    private final JavaPlugin plugin;
    @Getter
    public static final List<CommandData> commands = new ArrayList<>();
    static final Map<Class<?>, ParameterType<?>> parameterTypes = new HashMap<>();
    static boolean initiated = false;

    // commandes racines déjà enregistrées dans la command map Bukkit (dédoublonnage)
    private final Map<String, List<CommandData>> registeredRootCommands = new HashMap<>();
    private final Set<String> registeredRootCommandNames = new HashSet<>();

    public CommandHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        instance = this;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Register a custom parameter adapter.
     */
    public static void registerParameterType(Class<?> transforms, ParameterType<?> parameterType) {
        parameterTypes.put(transforms, parameterType);
    }

    /**
     * Registers a single class with the command handler.
     */
    public void registerCommands(Class<?> registeredClass) {

        for (Method method : registeredClass.getMethods()) {
            if (method.getAnnotation(Command.class) != null) {
                registerMethod(method);
            }
        }
    }

    /**
     * Registers a single method with the command handler.
     */
    private static void registerMethod(Method method) {
        Command commandAnnotation = method.getAnnotation(Command.class);
        List<ParameterData> parameterData = new ArrayList<>();

        for (int parameterIndex = 1; parameterIndex < method.getParameterTypes().length; parameterIndex++) {
            Param paramAnnotation = null;

            for (Annotation annotation : method.getParameterAnnotations()[parameterIndex]) {
                if (annotation instanceof Param) {
                    paramAnnotation = (Param) annotation;
                    break;
                }
            }

            if (paramAnnotation != null) {
                parameterData.add(new ParameterData(paramAnnotation, method.getParameterTypes()[parameterIndex]));
            } else {
                return;
            }
        }

        CommandData commandData = new CommandData(commandAnnotation, parameterData, method,
                method.getParameterTypes()[0].isAssignableFrom(Player.class));

        commands.add(commandData);
        commands.sort((o1, o2) -> (o2.getName().length() - o1.getName().length()));

        // enregistrement aussi dans la command map Bukkit : sans ça, AUCUNE complétion
        // possible (le serveur ne connaît pas les commandes) — pattern CupCodeAPI
        if (instance != null) {
            instance.registerWithBukkit(commandData);
        }
    }

    private static CommandHandler instance;

    /**
     * Enregistre chaque racine de commande (premier mot) dans la command map Bukkit.
     */
    private void registerWithBukkit(CommandData commandData) {
        try {
            SimpleCommandMap commandMap = getCommandMap();

            for (String commandName : commandData.getNames()) {
                String rootCommand = commandName.split(" ")[0].toLowerCase();

                registeredRootCommands.computeIfAbsent(rootCommand, k -> new ArrayList<>()).add(commandData);

                if (!registeredRootCommandNames.contains(rootCommand) && !isCommandRegistered(commandMap, rootCommand)) {
                    registerRootCommand(rootCommand, commandMap);
                    registeredRootCommandNames.add(rootCommand);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register command: " + commandData.getName());
            e.printStackTrace();
        }
    }

    /**
     * La commande est-elle déjà dans la map (plugin.yml ou autre plugin) ?
     */
    private boolean isCommandRegistered(SimpleCommandMap commandMap, String commandName) {
        try {
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            return knownCommands.containsKey(commandName.toLowerCase());
        } catch (Exception e) {
            try {
                return commandMap.getCommand(commandName) != null;
            } catch (Exception e2) {
                plugin.getLogger().warning("Could not check if command '" + commandName + "' is registered. Proceeding with registration.");
                return false;
            }
        }
    }

    /**
     * Crée la PluginCommand racine avec exécuteur + compléteur.
     * L'exécution réelle passe par PlayerCommandPreprocessEvent (evalCommand + cancel) :
     * la commande enregistrée ne sert donc qu'à la complétion et au secours console.
     */
    private void registerRootCommand(String rootCommand, SimpleCommandMap commandMap) throws Exception {
        Constructor<org.bukkit.command.PluginCommand> constructor =
                org.bukkit.command.PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
        constructor.setAccessible(true);

        org.bukkit.command.PluginCommand pluginCommand = constructor.newInstance(rootCommand, plugin);
        pluginCommand.setDescription("Guard command: " + rootCommand);
        pluginCommand.setUsage("/" + rootCommand + " <args>");

        MultiCommandExecutor executor = new MultiCommandExecutor(rootCommand);
        pluginCommand.setExecutor(executor);
        pluginCommand.setTabCompleter(executor);

        commandMap.register(plugin.getName().toLowerCase(), pluginCommand);
    }

    /**
     * Exécuteur + compléteur : sous-commandes multi-mots puis paramètres (ParameterType.tabComplete).
     */
    private class MultiCommandExecutor implements CommandExecutor, TabCompleter {
        private final String rootCommand;

        public MultiCommandExecutor(String rootCommand) {
            this.rootCommand = rootCommand;
        }

        @Override
        public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
            String fullCommand = label;
            if (args.length > 0) {
                fullCommand += " " + String.join(" ", args);
            }
            if (evalCommand(sender, fullCommand) == null) {
                List<CommandData> available = registeredRootCommands.get(rootCommand.toLowerCase());
                if (available != null) {
                    for (CommandData cmd : available) {
                        if (cmd.canAccess(sender)) {
                            sender.sendMessage(ChatColor.GRAY + "Usage: " + cmd.getUsageString());
                        }
                    }
                }
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
            if (!(sender instanceof Player)) {
                return new ArrayList<>();
            }
            Player player = (Player) sender;
            List<String> completions = new ArrayList<>();

            List<CommandData> availableCommands = registeredRootCommands.get(rootCommand.toLowerCase());
            if (availableCommands == null) {
                return completions;
            }

            // 1) suggestion de sous-commande pendant la frappe ("/root su" → "sub")
            String earlyLastWord = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
            String earlyCommandSoFar = alias.toLowerCase();
            if (args.length > 1) {
                earlyCommandSoFar += " " + String.join(" ", Arrays.copyOf(args, args.length - 1)).toLowerCase();
            }
            List<String> early = new ArrayList<>();
            for (CommandData cmd : availableCommands) {
                if (!cmd.canAccess(sender)) {
                    continue;
                }
                for (String cmdName : cmd.getNames()) {
                    String cmdLower = cmdName.toLowerCase();
                    if (cmdLower.startsWith(earlyCommandSoFar)) {
                        String remaining = cmdLower.substring(earlyCommandSoFar.length());
                        if (remaining.startsWith(" ")) {
                            remaining = remaining.substring(1);
                        }
                        if (!remaining.isEmpty()) {
                            String nextWord = remaining.split(" ")[0];
                            if (earlyLastWord.isEmpty() || nextWord.startsWith(earlyLastWord)) {
                                if (!early.contains(nextWord)) {
                                    early.add(nextWord);
                                }
                            }
                        }
                    }
                }
            }
            if (!early.isEmpty()) {
                return early;
            }

            // 2) match exact (les mots déjà tapés) → complétion du paramètre courant
            //    NB : pas de garde "commande racine + args" — c'est EXACTEMENT le cas "/ss Ste"
            //    que la complétion de paramètres doit servir.
            boolean trailingEmpty = args.length > 0 && args[args.length - 1].trim().isEmpty();
            for (CommandData cmd : availableCommands) {
                if (!cmd.canAccess(sender)) {
                    continue;
                }
                String matchedName = matchedName(cmd.getNames(), alias, args);
                if (matchedName == null) {
                    continue;
                }
                String[] cmdParts = matchedName.toLowerCase().split(" ");
                int parameterIndex = parameterIndexFor(cmdParts, args);
                if (parameterIndex > 0 && parameterIndex <= cmd.getParameters().size()) {
                    ParameterData paramData = cmd.getParameters().get(parameterIndex - 1);
                    String currentParam = args.length > 0 ? args[args.length - 1] : "";
                    List<String> paramCompletions = tabCompleteParameter(player, currentParam,
                            paramData.getParameterClass(), paramData.getTabCompleteFlags());
                    if (paramCompletions != null && !paramCompletions.isEmpty()) {
                        return paramCompletions;
                    }
                    return completions; // plus de paramètres à compléter
                }
            }

            // 3) repli : complétion des sous-commandes
            String partialLastWord = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
            String commandSoFar = alias.toLowerCase();
            if (args.length > 1) {
                commandSoFar += " " + String.join(" ", Arrays.copyOf(args, args.length - 1)).toLowerCase();
            }
            for (CommandData cmd : availableCommands) {
                if (!cmd.canAccess(sender)) {
                    continue;
                }
                for (String cmdName : cmd.getNames()) {
                    String cmdLower = cmdName.toLowerCase();
                    if (cmdLower.startsWith(commandSoFar)) {
                        String remaining = cmdLower.substring(commandSoFar.length());
                        if (remaining.startsWith(" ")) {
                            remaining = remaining.substring(1);
                        }
                        if (!remaining.isEmpty()) {
                            String nextWord = remaining.split(" ")[0];
                            if (partialLastWord.isEmpty() || nextWord.startsWith(partialLastWord)) {
                                if (!completions.contains(nextWord)) {
                                    completions.add(nextWord);
                                }
                            }
                        }
                    }
                }
            }
            return completions;
        }
    }

    /**
     * Pur : index (1-based) du paramètre en cours de frappe pour une commande à N mots.
     * Testé — même formule que CupCodeAPI (source de vérité du mainteneur).
     */
    static int parameterIndexFor(String[] cmdParts, String[] args) {
        return args.length - (cmdParts.length - 1);
    }

    /**
     * Pur (testé) : nom de la commande dont les mots tapés correspondent aux args, null si aucun.
     * Espace final toléré ("/ss " attend un paramètre), racine seule + args = match (ex "/ss Ste").
     */
    static String matchedName(String[] names, String alias, String[] args) {
        boolean trailingEmpty = args.length > 0 && args[args.length - 1].trim().isEmpty();
        for (String cmdName : names) {
            String[] cmdParts = cmdName.toLowerCase().split(" ");
            boolean isMatch = args.length >= cmdParts.length - 1;
            if (isMatch) {
                for (int i = 1; i < cmdParts.length; i++) {
                    if (i == cmdParts.length - 1 && trailingEmpty) {
                        continue; // espace final : match toléré
                    }
                    String argVal = args[i - 1];
                    if (argVal == null || argVal.trim().isEmpty() || !cmdParts[i].equals(argVal.toLowerCase())) {
                        isMatch = false;
                        break;
                    }
                }
            }
            if (isMatch && cmdParts[0].equalsIgnoreCase(alias)) {
                return cmdName;
            }
        }
        return null;
    }

    public void reloadCommands() {
        commands.clear();
        registeredRootCommands.clear();
        registeredRootCommandNames.clear();
        registerCommands(fr.perrier.guard.manager.commands.ChatColor.class);
        registerCommands(ChatLock.class);
        registerCommands(Cps.class);
        registerCommands(Freeze.class);
        registerCommands(Help.class);
        registerCommands(InventoryView.class);
        registerCommands(ModList.class);
        registerCommands(NoClip.class);
        registerCommands(SanctionSet.class);
        registerCommands(Teleport.class);
        registerCommands(TeleportHere.class);
        registerCommands(Vanish.class);
        registerCommands(Xray.class);
        registerCommands(LookUp.class);
        registerCommands(StaffChat.class);
        registerCommands(Report.class);
        registerCommands(fr.perrier.guard.manager.commands.Reports.class);
    }

    /**
     * Process a command (permission checks, argument validation, etc.)
     */
    public CommandData evalCommand(final CommandSender sender, String command) {
        String[] args = new String[]{};
        CommandData found = null;

        CommandLoop:
        for (CommandData commandData : commands) {
            for (String alias : commandData.getNames()) {
                String messageString = command.toLowerCase() + " "; // Add a space.
                String aliasString = alias.toLowerCase() + " "; // Add a space.

                if (messageString.startsWith(aliasString)) {
                    found = commandData;

                    if (messageString.length() > aliasString.length()) {
                        if (found.getParameters().isEmpty()) {
                            continue;
                        }
                    }

                    if (command.length() > alias.length() + 1) {
                        args = (command.substring(alias.length() + 1)).split(" ");
                    }

                    break CommandLoop;
                }
            }
        }

        if (found == null) {
            return (null);
        }

        if (!(sender instanceof Player) && !found.isConsoleAllowed()) {
            sender.sendMessage(ChatColor.RED + "This command does not support execution from the console.");
            return (found);
        }

        if (!found.canAccess(sender)) {
            sender.sendMessage(ChatUtil.translate(Messages.NOPERM.getMessage()));
            return (found);
        }

        if (found.isAsync()) {
            final CommandData foundClone = found;
            final String[] argsClone = args;

            new BukkitRunnable() {
                public void run() {
                    foundClone.execute(sender, argsClone);
                }
            }.runTaskAsynchronously(plugin);
        } else {
            found.execute(sender, args);
        }

        return (found);
    }

    /**
     * Transforms a parameter.
     */
    static Object transformParameter(CommandSender sender, String parameter, Class<?> transformTo) {
        if (transformTo.equals(String.class)) {
            return (parameter);
        }
        return (parameterTypes.get(transformTo).transform(sender, parameter));
    }

    /**
     * Tab completes a parameter.
     */
    static List<String> tabCompleteParameter(Player sender, String parameter, Class<?> transformTo, String[] tabCompleteFlags) {
        if (!parameterTypes.containsKey(transformTo)) {
            return (new ArrayList<>());
        }

        return (parameterTypes.get(transformTo).tabComplete(sender, ImmutableSet.copyOf(tabCompleteFlags), parameter));
    }

    /**
     * Initiates the command handler : enregistre les types de paramètres (transform + tabComplete).
     * Appelé une fois au boot (Guard.onEnable) — SANS lui, toute commande à paramètre
     * non-String (/ss <player>, /freeze <player>...) NPE dans transformParameter, et
     * aucune complétion de paramètre ne fonctionne.
     */
    public void hook() {
        Preconditions.checkState(!initiated);
        initiated = true;
        instance = this;

        registerParameterType(boolean.class, new BooleanParameterType());
        registerParameterType(float.class, new FloatParameterType());
        registerParameterType(double.class, new DoubleParameterType());
        registerParameterType(int.class, new IntegerParameterType());
        registerParameterType(OfflinePlayer.class, new OfflinePlayerParameterType());
        registerParameterType(Player.class, new PlayerParameterType());
        registerParameterType(String.class, new StringParameterType());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().substring(1);

        if (evalCommand(event.getPlayer(), command) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onConsoleCommand(ServerCommandEvent event) {
        if (evalCommand(event.getSender(), event.getCommand()) != null) {
            event.setCancelled(true);
        }
    }

    /**
     * La command map du serveur, SANS deviner le nom du paquet CraftBukkit
     * (org.bukkit.craftbukkit.&lt;version&gt; est mort sur Paper mojmap : ClassNotFoundException).
     * On lit la classe RÉELLE du serveur — pattern CupCodeAPI (référence mainteneur).
     */
    public static SimpleCommandMap getCommandMap() {
        return readCommandMap(Bukkit.getServer());
    }

    /**
     * Pur (testé) : champ privé "commandMap" d'abord (chemin 1.8→Paper), repli méthode publique.
     */
    static SimpleCommandMap readCommandMap(Object server) {
        final Class<?> serverClass = server.getClass();
        try {
            final Field field = serverClass.getDeclaredField("commandMap");
            field.setAccessible(true);
            return (SimpleCommandMap) field.get(server);
        } catch (Exception ignored) {
        }
        try {
            final Method method = serverClass.getMethod("getCommandMap");
            return (SimpleCommandMap) method.invoke(server);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Guard: command map inaccessible sur ce serveur (" + serverClass.getName() + ")", e);
        }
    }
}
