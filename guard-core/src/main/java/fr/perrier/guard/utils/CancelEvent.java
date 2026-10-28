package fr.perrier.guard.utils;

import fr.perrier.guard.*;
import fr.perrier.guard.manager.commands.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;

public class CancelEvent implements Listener {

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if(Guard.getInstance().getVanishALL().get(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if(Guard.getInstance().getVanishALL().get(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if(Guard.getInstance().getVanishALL().get(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if(Guard.getInstance().getVanishALL().get(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        try {
            if (Guard.getInstance().getVanishALL().get(e.getEntity().getUniqueId())) {
                if (Vanish.godmode.contains(e.getEntity().getUniqueId())) {
                    e.setCancelled(true);
                } else {
                    if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                        e.setCancelled(true);
                    }
                    if (e.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.FALL) {
                        e.setCancelled(true);
                    }
                }
            }
        }catch (Exception e1) {
            return;
        }
    }

    @EventHandler
    public void onCmdExecute(PlayerCommandPreprocessEvent e) {
        Player p = (Player) e.getPlayer();
        String cmd = "";
        if (e.getMessage().indexOf(" ") >= 0) {
            cmd = e.getMessage().substring(1, e.getMessage().indexOf(" "));
        } else {
            cmd = e.getMessage().substring(1, e.getMessage().length());
        }
        if(cmd.contains("reload") || cmd.contains("rl")) {
            p.sendMessage(Messages.prefix() + ChatUtil.translate("&4WARNING &3&lGuard &4doesn't support /reload, please do /restart or /stop.\n&4Some bugs can appends with a reload."));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(Guard.getInstance().getVanishALL().get(e.getWhoClicked().getUniqueId()))
            e.setCancelled(true);
    }
}
