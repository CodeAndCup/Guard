package fr.perrier.guard.manager.commands;

import fr.perrier.guard.*;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.manager.cps.CpsService;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.*;
import org.bukkit.entity.*;

public class Cps {

    @Command(names = CommandsUtils.CPS, perm = "guard.cps")
    public static void onCPS(Player player, @Param(name = "target", tabCompleteFlags = "players")String starget, @Param(name = "check", baseValue = "0")String scheck) {
        if(!Guard.getDefaultConfig().getBoolean("commands.cps.active")) {
            Messages.send(player,Messages.COMMANDDISABLE);
            return;
        }
        Player target = Bukkit.getPlayer(starget);
        if(target == null) {
            Messages.send(player,Messages.PLAYERNOTFOUND);
            return;
        }
        player.sendMessage(Messages.prefix() + ChatUtil.translate(Messages.COMMANDS_CPS_RUNNING.getMessage().replace("%target%",target.getName())));
        int check = Integer.parseInt(scheck);
        if(check == 0) check = Guard.getDefaultConfig().getInt("cps_check");

        // le relevé s'accroche au flux de comptage permanent : 1 relevé/seconde pendant
        // `check` secondes, sans tâche dédiée — le désabonnement est automatique
        final int total = check;
        final double[] sum = {0.0, 0.0};
        final int[] runs = {0};
        CpsService.observe(target, total, sample -> {
            if (!player.isOnline()) {
                return; // le staff est parti : le relevé tourne encore pour le target, sans affichage
            }
            sum[0] += sample[0];
            sum[1] += sample[1];
            runs[0]++;
            if (runs[0] < total) {
                player.sendMessage(Messages.prefix() + ChatUtil.translate(Messages.COMMANDS_CPS_FORMAT.getMessage()
                        .replace("%target%", target.getName())
                        .replace("%left%", String.format("%.1f", sample[0]))
                        .replace("%right%", String.format("%.1f", sample[1]))));
            } else {
                player.sendMessage(Messages.prefix() + ChatUtil.translate(Messages.COMMANDS_CPS_END.getMessage()
                        .replace("%target%", target.getName())
                        .replace("%left%", String.format("%.1f", sum[0] / total))
                        .replace("%right%", String.format("%.1f", sum[1] / total))));
            }
        });
    }
}
