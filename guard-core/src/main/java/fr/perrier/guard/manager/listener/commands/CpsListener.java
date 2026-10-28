package fr.perrier.guard.manager.listener.commands;

import fr.perrier.guard.manager.cps.CpsService;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;

/** Comptage CPS toujours actif (scan h24 CpsService) — plus conditionné à une commande. */
public class CpsListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Action a = e.getAction();
        Player p = e.getPlayer();

        if (a.equals(Action.LEFT_CLICK_AIR) || a.equals(Action.LEFT_CLICK_BLOCK)) {
            CpsService.tracker(p).leftClick();
        } else if (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK)) {
            // sans bloc en main l'event se déclenche 2 fois : poids 0.5 (comportement historique)
            CpsService.tracker(p).rightClick(e.isBlockInHand() ? 1.0 : 0.5);
        }
    }
}
