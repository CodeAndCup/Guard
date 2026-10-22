package fr.perrier.guard.manager.listener.hotbar;

import fr.perrier.guard.*;
import fr.perrier.guard.utils.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

import java.util.*;

public class ItemList implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        ItemStack it = p.getItemInHand();
        FileConfiguration cfg = Guard.getInstance().getLangConfig();

        try {
            if(Objects.requireNonNull(it.getItemMeta()).getDisplayName().equalsIgnoreCase(ChatUtil.translate(cfg.getString("items.ss_book"))))
                p.performCommand("ss " + e.getRightClicked().getName());
            else if(Objects.requireNonNull(it.getItemMeta()).getDisplayName().equalsIgnoreCase(ChatUtil.translate(cfg.getString("items.freeze"))))
                p.performCommand("freeze " + e.getRightClicked().getName());
            else if(Objects.requireNonNull(it.getItemMeta()).getDisplayName().equalsIgnoreCase(ChatUtil.translate(cfg.getString("items.cps"))))
                p.performCommand("cps " + e.getRightClicked().getName());
            else if(Objects.requireNonNull(it.getItemMeta()).getDisplayName().equalsIgnoreCase(ChatUtil.translate(cfg.getString("items.inventory_view"))))
                p.performCommand("invsee " + e.getRightClicked().getName());
        }catch (Exception ignored) {}
    }

}
