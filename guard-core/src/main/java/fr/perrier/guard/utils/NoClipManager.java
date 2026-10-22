package fr.perrier.guard.utils;

import java.util.ArrayList;
import java.util.UUID;

import fr.perrier.guard.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class NoClipManager {

	
	public static ArrayList<UUID> noClipPlayerNames = new ArrayList<UUID>();
	
	public void everyTick() {
        Bukkit.getScheduler().runTaskTimer(Guard.getInstance(), this::checkForBlocks, 0L, 1L);
    }

    @SuppressWarnings("unused")
	private void checkForBlocks() {
        for (UUID uuid : noClipPlayerNames) {
            boolean noClip;
            Player p = Bukkit.getPlayer(uuid);
            if (p == null || !p.isOnline()) continue;
            if (/*Guard.getInstance().getVanishALL().get(p.getUniqueId()) &&*/ !p.getGameMode().equals(GameMode.SPECTATOR)) {
                boolean bl = noClip = p.getLocation().add(0.0, -0.1, 0.0).getBlock().getType() != Material.AIR && p.isSneaking() || this.isNoClip(p);
                if (!noClip) continue;
                p.setGameMode(GameMode.SPECTATOR);
                continue;
            }
            if (!p.getGameMode().equals(GameMode.SPECTATOR)) continue;
            boolean bl = noClip = p.getLocation().add(0.0, -0.1, 0.0).getBlock().getType() != Material.AIR || this.isNoClip(p);
            if (noClip) continue;
            p.setGameMode(GameMode.SURVIVAL);
            p.setAllowFlight(true);
            p.setFlying(true);
        }
    }

    private boolean isNoClip(Player p) {
        boolean noClip = false;
        if (p.getLocation().add(0.4, 0.0, 0.0).getBlock().getType() != Material.AIR) {
            noClip = true;
        } else if (p.getLocation().add(-0.4, 0.0, 0.0).getBlock().getType() != Material.AIR) {
            noClip = true;
        } else if (p.getLocation().add(0.0, 0.0, 0.4).getBlock().getType() != Material.AIR) {
            noClip = true;
        } else if (p.getLocation().add(0.0, 0.0, -0.4).getBlock().getType() != Material.AIR) {
            noClip = true;
        } else if (p.getLocation().add(0.4, 1.0, 0.0).getBlock().getType() != Material.AIR) {
            noClip = true;
        } else if (p.getLocation().add(-0.4, 1.0, 0.0).getBlock().getType() != Material.AIR) {
            noClip = true;
        } else if (p.getLocation().add(0.0, 1.0, 0.4).getBlock().getType() != Material.AIR) {
            noClip = true;
        } else if (p.getLocation().add(0.0, 1.0, -0.4).getBlock().getType() != Material.AIR) {
            noClip = true;
        } else if (p.getLocation().add(0.0, 1.9, 0.0).getBlock().getType() != Material.AIR) {
            noClip = true;
        }
        return noClip;
    }
}
