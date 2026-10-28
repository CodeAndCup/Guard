package fr.perrier.guard;

import fr.perrier.guard.automod.chat.AutoMute;
import fr.perrier.guard.compat.CompatHolder;
import fr.perrier.guard.manager.listener.CloseInventory;
import fr.perrier.guard.manager.listener.PlayerJoin;
import fr.perrier.guard.manager.listener.PlayerLeave;
import fr.perrier.guard.manager.listener.PluginMessage;
import fr.perrier.guard.network.Codec;
import fr.perrier.guard.network.MessengerHolder;
import fr.perrier.guard.reports.ReportCache;
import fr.perrier.guard.reports.ReportFlash;
import fr.perrier.guard.reports.ReportNotifications;
import fr.perrier.guard.manager.listener.commands.*;
import fr.perrier.guard.manager.listener.hotbar.*;
import fr.perrier.guard.manager.xray.XrayManager;
import fr.perrier.guard.sql.*;
import fr.perrier.guard.utils.lang.LangMigrator;
import fr.perrier.guard.commands.*;
import fr.perrier.guard.manager.commands.*;
import fr.perrier.guard.manager.commands.ChatColor;
import fr.perrier.guard.manager.cps.CpsService;
import fr.perrier.guard.menu.*;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.item.*;
import fr.perrier.guard.utils.update.*;
import lombok.*;
import org.bukkit.*;
import org.bukkit.configuration.file.*;
import org.bukkit.plugin.java.*;
import org.bstats.bukkit.Metrics;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Guard extends JavaPlugin {

    @Getter
    public static Guard instance;
    @Getter
    private DatabaseManager dbManager;
    @Getter
    private Map<UUID,Boolean> VanishALL;
    @Getter
    private static MenuAPI menuAPI;
    @Getter
    private static CommandHandler commandHandler;

    @Getter@Setter
    private static File langConfigFile;
    @Getter@Setter
    private static FileConfiguration langConfig;

    @Getter@Setter
    private static File defaultConfigFile;
    @Getter@Setter
    private static FileConfiguration defaultConfig;

    @Getter
    private static final int spigotID = 116879;
    @Getter
    private static final int bstatsID = 21932;

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        instance = Guard.this;

        defaultConfig = getConfig();
        defaultConfig.options().copyDefaults(true);
        this.saveDefaultConfig();
        defaultConfigFile = new File(getDataFolder(), "config.yml");

        createLanguageConfig();

        CompatHolder.init(getServer().getBukkitVersion(), System.getProperty("java.version"));
        MessengerHolder.init(this);
        // abonnements réseau (dispatch main thread, cf. Messenger)
        MessengerHolder.get().onMessage(Codec.TYPE_STAFFCHAT, StaffChat.networkHandler());
        MessengerHolder.get().onMessage(Codec.TYPE_NOTIFY, ReportFlash.networkHandler());
        MessengerHolder.get().onMessage(Codec.TYPE_VANISH, VanishSync.networkHandler());

        new Metrics(this,bstatsID);

        new UpdateChecker(this, spigotID).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getLogger().info( "----------------------------------------------");
                getLogger().info( "| ");
                getLogger().info( "| Guard is up to date !");
                getLogger().info( "| ");
                getLogger().info( "| Version: " + version);
                getLogger().info( "----------------------------------------------");
            } else {
                getLogger().info( "----------------------------------------------");
                getLogger().info( "| ");
                getLogger().info( "| Guard new update available !");
                getLogger().info( "| ");
                getLogger().info( "| Your version: " + getDescription().getVersion());
                getLogger().info( "| New version: " + version);
                getLogger().info( "----------------------------------------------");
            }
        });


        if(Guard.getDefaultConfig().getBoolean("database.enable")) {
            dbManager = DatabaseManager.create();
        }

        VanishALL = new ConcurrentHashMap<>();

        // cache des reports (les menus le lisent en synchrone : plus d'affichage intermittent)
        ReportCache.startPeriodicRefresh();
        ReportNotifications.init(); // toggle staff (/reports on|off), fichier local
        CpsService.start(); // scan CPS h24 (roll main thread + analyse async)

        menuAPI = new MenuAPI(instance);
        commandHandler = new CommandHandler(this);
        // types de paramètres (transform + tab completion) : sans ce hook, les commandes
        // à paramètre non-String NPE et aucune complétion de paramètre ne fonctionne
        commandHandler.hook();

        loadCommands();
        loadListener();

        new NoClipManager().everyTick();

        if (Guard.getDefaultConfig().getBoolean("anticheat_encryption.enabled")) {
            EncryptionAC.setProtocolManager(EncryptionAC.resolveProtocolManager());
            if (EncryptionAC.getProtocolManager() != null) {
                new EncryptionAC().registerPacketListener();
                getLogger().info("Guard: AntiCheat Encryption (packet obfuscation) active.");
            } else {
                getLogger().warning("Guard: anticheat_encryption.enabled=true but ProtocolLib is not installed - disabled.");
            }
        }

        Bukkit.getMessenger().registerIncomingPluginChannel(instance, "minecraft:brand", new PluginMessage());
    }

    public void loadCommands() {
        commandHandler.registerCommands(ChatColor.class);
        commandHandler.registerCommands(ChatLock.class);
        commandHandler.registerCommands(Cps.class);
        commandHandler.registerCommands(Freeze.class);
        commandHandler.registerCommands(Help.class);
        commandHandler.registerCommands(InventoryView.class);
        commandHandler.registerCommands(ModList.class);
        commandHandler.registerCommands(NoClip.class);
        commandHandler.registerCommands(SanctionSet.class);
        commandHandler.registerCommands(Teleport.class);
        commandHandler.registerCommands(TeleportHere.class);
        commandHandler.registerCommands(Vanish.class);
        commandHandler.registerCommands(Xray.class);
        commandHandler.registerCommands(LookUp.class);
        commandHandler.registerCommands(StaffChat.class);
        commandHandler.registerCommands(Report.class);
        commandHandler.registerCommands(Reports.class);
    }

    public void loadListener() {
        getServer().getPluginManager().registerEvents(new CloseInventory(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoin(),this);
        getServer().getPluginManager().registerEvents(new PlayerLeave(),this);

        getServer().getPluginManager().registerEvents(new CpsListener(),this);
        getServer().getPluginManager().registerEvents(new ChatColorListener(),this);
        getServer().getPluginManager().registerEvents(new ChatLockListener(),this);
        getServer().getPluginManager().registerEvents(new StaffChatListener(),this);
        getServer().getPluginManager().registerEvents(new XrayManager(),this);

        getServer().getPluginManager().registerEvents(new ItemList(),this);
        getServer().getPluginManager().registerEvents(new CancelEvent(),this);

        getServer().getPluginManager().registerEvents(new CustomItemListener(this),this);

        getServer().getPluginManager().registerEvents(new AutoMute(),this);
    }

    @Override
    public void onDisable() {

        Bukkit.getOnlinePlayers().forEach(player -> {
            if(Guard.getInstance().getVanishALL().get(player.getUniqueId())) {
                Vanish.removeVanish(player);
            }
        });

        if(Guard.getDefaultConfig().getBoolean("database.enable"))
            this.dbManager.close();

        if(MessengerHolder.get() != null)
            MessengerHolder.get().close();
    }

    public void createLanguageConfig() {
        setLangConfigFile(new File(getDataFolder(), "message_" + getDefaultConfig().getString("language") + ".yml"));
        if (!getLangConfigFile().exists()) {
            getLangConfigFile().getParentFile().mkdirs();
            saveResource("message_" + getDefaultConfig().getString("language") + ".yml", false);
        }
        setLangConfig(YamlConfiguration.loadConfiguration(getLangConfigFile()));
        // migration : les clés nouvelles des mises à jour n'existent pas dans un fichier lang ancien installé
        int added = LangMigrator.fill(getLangConfigFile(), langDefaultsFromJar());
        if (added > 0) {
            getLogger().info("Guard: lang +" + added + " clés ajoutées (" + getDefaultConfig().getString("language") + ")");
            setLangConfig(YamlConfiguration.loadConfiguration(getLangConfigFile()));
        }
    }

    /** Defaults du jar (resource embarquée) aplatis en clés pointées → valeurs BRUTES (String/List). */
    private java.util.Map<String, Object> langDefaultsFromJar() {
        java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
        java.io.InputStreamReader reader = new java.io.InputStreamReader(
                getResource("message_" + getDefaultConfig().getString("language") + ".yml"), java.nio.charset.StandardCharsets.UTF_8);
        YamlConfiguration jar = YamlConfiguration.loadConfiguration(reader);
        for (String key : jar.getKeys(true)) {
            if (!(jar.get(key) instanceof org.bukkit.configuration.ConfigurationSection)) {
                map.put(key, jar.get(key));
            }
        }
        return map;
    }
}
