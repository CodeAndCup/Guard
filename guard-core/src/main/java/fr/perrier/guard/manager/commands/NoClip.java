package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;


public class NoClip {

	@Command(names = CommandsUtils.NOCLIP, perm = "guard.noclip")
	public static void onNoClip(Player player) {
		if(!Guard.getDefaultConfig().getBoolean("commands.noclip.active")) {
			Messages.send(player,Messages.COMMANDDISABLE);
			return;
		}
		if (!NoClipManager.noClipPlayerNames.contains(player.getUniqueId())) {
			NoClipManager.noClipPlayerNames.add(player.getUniqueId());
			Messages.send(player, Messages.COMMANDS_NOCLIP_ACTIVATED);
			return;
		}
		if (player.getLocation().getBlock().getType() != Material.AIR || player.getLocation().add(0, 1, 0).getBlock().getType() != Material.AIR)
			return;
		NoClipManager.noClipPlayerNames.remove(player.getUniqueId());
		Messages.send(player, Messages.COMMANDS_NOCLIP_DESACTIVATED);
	}
}
