package fr.perrier.guard.manager.listener.commands;

import fr.perrier.guard.manager.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;

public class CpsListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Action a = e.getAction();
        Player p = e.getPlayer();

        if (a.equals(Action.LEFT_CLICK_AIR) || a.equals(Action.LEFT_CLICK_BLOCK)) {
            if (Cps.testedG.containsKey(p.getUniqueId()))
                Cps.testedG.replace(p.getUniqueId(), Cps.testedG.get(p.getUniqueId()) + 1.0);
        }
        if (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK)) {
            if (Cps.testedD.containsKey(e.getPlayer().getUniqueId()))
                Cps.testedD.replace(p.getUniqueId(), Cps.testedD.get(p.getUniqueId()) + (e.isBlockInHand() ? 1.0 : 0.5)); // This is cause if it's not a block is in player hand the event is launched 2 times
        }
    }
}
