package fr.perrier.guard.manager.listener;

import fr.perrier.guard.*;
import fr.perrier.guard.manager.commands.*;
import fr.perrier.guard.sql.DatabaseManager;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.update.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.potion.*;

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

        if(DatabaseManager.isEnabled()) {

            final UUID uuid = p.getUniqueId();
            // état optimiste immédiat : onVanish peut être appelé avant la réponse SQL (sinon unboxing null)
            if(!Guard.getInstance().getVanishALL().containsKey(uuid)) {
                Guard.getInstance().getVanishALL().put(uuid, false);
            }

            Guard.getInstance().getDbManager().getVanish(uuid).thenAccept(vanish -> {
                Guard.getInstance().getVanishALL().put(uuid, vanish);
                if (vanish) {
                    Bukkit.getScheduler().runTask(Guard.getInstance(), () -> {
                        Vanish.initVanish(p);
                    });
                }
            });
        }else{
            if(!Guard.getInstance().getVanishALL().containsKey(p.getUniqueId())) {
                Guard.getInstance().getVanishALL().put(p.getUniqueId(), false);
            }
        }
    }

}
