package fr.perrier.guard;

import fr.perrier.guard.automod.chat.AutoMute;
import fr.perrier.guard.compat.CompatHolder;
import fr.perrier.guard.manager.listener.CloseInventory;
import fr.perrier.guard.manager.listener.PlayerJoin;
import fr.perrier.guard.manager.listener.PlayerLeave;
import fr.perrier.guard.manager.listener.PluginMessage;
import fr.perrier.guard.network.MessengerHolder;
import fr.perrier.guard.manager.listener.commands.*;
import fr.perrier.guard.manager.listener.hotbar.*;
import fr.perrier.guard.manager.xray.XrayManager;
import fr.perrier.guard.sql.*;
import fr.perrier.guard.commands.*;
import fr.perrier.guard.manager.commands.*;
import fr.perrier.guard.manager.commands.ChatColor;
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

        menuAPI = new MenuAPI(instance);
        commandHandler = new CommandHandler(this);

        loadCommands();
        loadListener();

        new NoClipManager().everyTick();

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
        commandHandler.registerCommands(SS.class);
        commandHandler.registerCommands(TP.class);
        commandHandler.registerCommands(TPHere.class);
        commandHandler.registerCommands(Vanish.class);
        commandHandler.registerCommands(Xray.class);
        commandHandler.registerCommands(LookUp.class);
        commandHandler.registerCommands(StaffChat.class);
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
    }
}
