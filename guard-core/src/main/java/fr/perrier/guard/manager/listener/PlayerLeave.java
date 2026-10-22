package fr.perrier.guard.manager.listener;

import fr.perrier.guard.*;
import fr.perrier.guard.manager.commands.*;
import fr.perrier.guard.utils.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.potion.*;

import java.util.Objects;


public class PlayerLeave implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent e) {

        Player p = e.getPlayer();

        //TODO FREEZE
        if(p.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            for(Player staff : Bukkit.getOnlinePlayers()) {
                if(staff.hasPermission("guard.freeze")) {
                    Messages.send(staff,Messages.COMMANDS_FREEZE_DISCONNECT);
                    if(Guard.getDefaultConfig().getBoolean("freeze_ban")) {
                        String sanction = Guard.getDefaultConfig().getString("sanction.ban");
                        if(sanction == null) throw new RuntimeException("Sanction Command Null");
                        sanction = sanction.replace("%target%", p.getName());
                        sanction = sanction.replace("%duration%", Objects.requireNonNull(Guard.getDefaultConfig().getString("freeze_ban.duration")));
                        sanction = sanction.replace("%reason%", Objects.requireNonNull(Guard.getDefaultConfig().getString("freeze_ban.reason")));

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),sanction);
                    }
                }
            }
        }


        //TODO VANISH
        if(Guard.getInstance().getVanishALL().get(p.getUniqueId())) {
            Vanish.removeVanish(p);
        }
    }


}
