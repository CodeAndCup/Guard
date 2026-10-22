package fr.perrier.guard.manager.commands;

import com.lunarclient.bukkitapi.*;
import fr.perrier.guard.*;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.manager.items.*;
import fr.perrier.guard.manager.listener.hotbar.*;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

import java.sql.*;
import java.util.*;

public class Vanish {

    public static ArrayList<Player> vanish = new ArrayList<Player>();
    public static Map<UUID, ItemStack[]> items = new HashMap<UUID, ItemStack[]>();
    public static ArrayList<UUID> godmode = new ArrayList<>();


    @Command(names = CommandsUtils.VANISH, perm = "guard.vanish")
    public static void onVanish(Player player) {
        if(!Guard.getDefaultConfig().getBoolean("commands.vanish.active")) {
            Messages.send(player,Messages.COMMANDDISABLE);
            return;
        }
        UUID uuid = player.getUniqueId();
        if(Guard.getInstance().getVanishALL().get(uuid)) {
            Messages.send(player,Messages.COMMANDS_VANISH_DESACTIVATED);
            Guard.getInstance().getVanishALL().put(uuid,false);

            if(Guard.getDefaultConfig().getBoolean("database.enable")) {
                final fr.perrier.guard.sql.Connection vanishConnection = Guard.getInstance().getDbManager().getConnection();
                try {
                    final Connection connection = vanishConnection.getConnection();
                    final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE player_vanish SET vanish=false WHERE uuid=?");
                    preparedStatement.setString(1, uuid.toString());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (Guard.getInstance().getServer().getPluginManager().getPlugin("LunarClient-API") != null) {
                if (LunarClientAPI.getInstance().isRunningLunarClient(player)) {
                    LunarClientAPI.getInstance().disableAllStaffModules(player);
                }
            }
            removeVanish(player);
        }else{

            Messages.send(player,Messages.COMMANDS_VANISH_ACTIVATED);
            Guard.getInstance().getVanishALL().put(uuid,true);
            if(Guard.getDefaultConfig().getBoolean("database.enable")) {

                final fr.perrier.guard.sql.Connection vanishConnection = Guard.getInstance().getDbManager().getConnection();
                try {
                    final Connection connection = vanishConnection.getConnection();
                    final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE player_vanish SET vanish=true WHERE uuid=?");
                    preparedStatement.setString(1, uuid.toString());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (Guard.getInstance().getServer().getPluginManager().getPlugin("LunarClient-API") != null) {
                if (LunarClientAPI.getInstance().isRunningLunarClient(player)) {
                    player.sendMessage(ChatUtil.prefix("&fLunarClient Staff &aActivé&f."));
                    LunarClientAPI.getInstance().giveAllStaffModules(player);
                }
            }

            initVanish(player);

        }
    }

    public static void initVanish(Player p) {
        Inventory inv = p.getInventory();

        items.put(p.getUniqueId(), inv.getContents());
        inv.clear();
        Hide(p);
        VanishHotbar.page1(p);

        p.setAllowFlight(true);
        p.setFlying(true);

        godmode.add(p.getUniqueId());

    }

    public static void removeVanish(Player p) {
        for(Player allp : Bukkit.getOnlinePlayers()) {
            allp.showPlayer(p);
        }

        Inventory inv = p.getInventory();
        inv.clear();
        if(items.get(p.getUniqueId()) != null) {
            inv.setContents(items.get(p.getUniqueId()));
            items.remove(p.getUniqueId());
        }
        p.updateInventory();

        if(p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
            p.setAllowFlight(false);
            p.setFlying(false);
        }
        p.setFlySpeed(0.1f);

        godmode.remove(p.getUniqueId());
        vanish.remove(p);
    }

    public static void Hide(Player p) {
        for(Player allp : Bukkit.getOnlinePlayers()) {
            if(vanish.contains(allp)) {
                p.showPlayer(allp);
                allp.showPlayer(p);
                continue;
            }
            if(allp!=p) {
                allp.hidePlayer(p);
            }
        }
        p.getInventory().setItem(4, ModerationItems.vanish_on.toItemStack());
        vanish.add(p);
    }

    public static void Show(Player p) {
        for(Player allp : Bukkit.getOnlinePlayers()) {
            allp.showPlayer(p);
            if(vanish.contains(allp) && allp!=p) {
                p.hidePlayer(allp);
            }
        }
        p.getInventory().setItem(4, ModerationItems.vanish_off.toItemStack());
        vanish.remove(p);
    }
}
