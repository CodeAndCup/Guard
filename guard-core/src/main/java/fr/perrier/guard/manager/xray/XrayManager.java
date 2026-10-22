package fr.perrier.guard.manager.xray;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class XrayManager implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new XrayPlayer(event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        XrayPlayer.list.get(event.getPlayer()).update(event.getBlock().getType());
    }
}
