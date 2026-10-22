package fr.perrier.guard.manager.listener;

import fr.perrier.guard.*;
import fr.perrier.guard.sql.Connection;
import fr.perrier.guard.manager.commands.*;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.update.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.potion.*;

import java.sql.*;
import java.util.*;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player p = e.getPlayer();

        if(p.hasPermission("guard.admin")) {
            new UpdateChecker(Guard.getInstance(), Guard.getSpigotID()).getVersion(version -> {
                if (!Guard.getInstance().getDescription().getVersion().equals(version)) {
                    p.sendMessage(ChatUtil.prefix("&aNew update found ! &fCurrent: &c" + Guard.getInstance().getDescription().getVersion() + " &8 &fNew: &a" + version));
                }
            });
        }

        //TODO FREEZE
        if(p.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            Freeze.freezePlayer(p);
        }

        //TODO HIDE WHEN A PLAYER IS VANISH

        Vanish.vanish.forEach(staff -> {
            if(p.equals(staff)) {
                p.hidePlayer(staff);
            }
        });

        //TODO VANISH

        if(Guard.getDefaultConfig().getBoolean("database.enable")) {

            final UUID uuid = p.getUniqueId();
            final Connection vanishConnection = Guard.getInstance().getDbManager().getConnection();

            Bukkit.getScheduler().runTaskAsynchronously(Guard.getInstance(), () -> {
                try {
                    final java.sql.Connection connection = vanishConnection.getConnection();

                    final PreparedStatement preparedStatement = connection.prepareStatement("SELECT uuid, vanish FROM player_vanish WHERE uuid = ?");

                    preparedStatement.setString(1, uuid.toString());

                    final ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        final Boolean vanish = resultSet.getBoolean("vanish");
                        Guard.getInstance().getVanishALL().put(uuid, vanish);

                        //TODO SET PLAYER AS VANISH IF TRUE
                        if (vanish) {
                            Bukkit.getScheduler().runTask(Guard.getInstance(), () -> {
                                Vanish.initVanish(p);
                            });
                        }
                    } else {
                        createUserValue(connection, uuid);
                        Guard.getInstance().getVanishALL().put(uuid, false);
                    }

                } catch (Exception e1) {
                    e1.printStackTrace();
                    return;
                }
            });
        }else{
            if(!Guard.getInstance().getVanishALL().containsKey(p.getUniqueId())) {
                Guard.getInstance().getVanishALL().put(p.getUniqueId(), false);
            }
        }
    }

    private void createUserValue(java.sql.Connection connection, UUID uuid) {
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO player_vanish VALUES (?,?)");

            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setBoolean(2,false);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


}
