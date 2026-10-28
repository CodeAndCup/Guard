package fr.perrier.guard.manager.listener;

import fr.perrier.guard.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.potion.*;

public class CloseInventory implements Listener {


    //TODO FREEZE
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        Inventory inv = e.getInventory();
        if(p.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            Bukkit.getScheduler().runTaskLater(Guard.getInstance(), () -> p.openInventory(inv), 1L);

        }
    }
}
