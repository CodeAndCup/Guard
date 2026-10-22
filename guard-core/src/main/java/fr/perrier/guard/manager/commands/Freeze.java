package fr.perrier.guard.manager.commands;

import fr.perrier.guard.*;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.manager.menu.moderation.FreezeMenu;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.potion.*;

import java.util.*;

public class Freeze {

    @Command(names = CommandsUtils.FREEZE, perm = "guard.freeze")
    public static void onFreeze(Player player, @Param(name = "target")String starget) {
        if(!Guard.getDefaultConfig().getBoolean("commands.freeze.active")) {
            Messages.send(player,Messages.COMMANDDISABLE);
            return;
        }
        Player target = Bukkit.getPlayer(starget);
        if(target == null) {
            Messages.send(player,Messages.PLAYERNOTFOUND);
            return;
        }
        if(target.equals(player)) {
            player.sendMessage(Messages.prefix() + ChatUtil.translate("&cWhat do you do today c: ?"));
            return;
        }
        if(!target.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            freezePlayer(target);
            player.sendMessage(String.format("%s%s", Messages.prefix(), ChatUtil.translate(Messages.COMMANDS_FREEZE_ACTIVATED.getMessage().replace("%target%", target.getName()))));
        }else{
            unfreezePlayer(target);
            player.sendMessage(Messages.prefix() + ChatUtil.translate(Messages.COMMANDS_FREEZE_DESACTIVATED.getMessage().replace("%target%",target.getName())));
        }
    }

    public static void freezePlayer(Player target) {
        new FreezeMenu().openMenu(target);
        target.setWalkSpeed(0);
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10000, 255), true);
    }

    public static void unfreezePlayer(Player target) {
        target.setWalkSpeed(0.2f);
        target.removePotionEffect(PotionEffectType.BLINDNESS);
        target.closeInventory();
    }
}
