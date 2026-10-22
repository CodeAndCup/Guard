package fr.perrier.guard.manager.commands;

import fr.perrier.guard.*;
import fr.perrier.guard.commands.annotations.*;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.*;
import org.bukkit.entity.*;

import java.util.*;

public class Cps {

    public static HashMap<UUID, Double> testedG = new HashMap<UUID, Double>();
    public static HashMap<UUID, Double> testedD = new HashMap<UUID,Double>();
    public static HashMap<UUID, Double> saveG = new HashMap<UUID,Double>();
    public static HashMap<UUID, Double> saveD = new HashMap<UUID,Double>();

    @Command(names = CommandsUtils.CPS, perm = "guard.cps")
    public static void onCPS(Player player, @Param(name = "target")String starget, @Param(name = "check", baseValue = "0")String scheck) {
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
        testedG.put(target.getUniqueId(),0.0);
        testedD.put(target.getUniqueId(),0.0);
        saveG.put(target.getUniqueId(),0.0);
        saveD.put(target.getUniqueId(),0.0);
        int check = Integer.parseInt(scheck);
        if(check==0) check = Guard.getDefaultConfig().getInt("cps_check");
        for(int i=1; i < check; i++) {
            Bukkit.getScheduler().runTaskLater(Guard.getInstance(), () -> {
                player.sendMessage(Messages.prefix() + ChatUtil.translate(Messages.COMMANDS_CPS_FORMAT.getMessage().replace("%target%",target.getName()).replace("%left%",String.valueOf(testedG.get(target.getUniqueId()))).replace("%right%",String.valueOf(testedD.get(target.getUniqueId())))));
                saveG.replace(target.getUniqueId(), testedG.get(target.getUniqueId()) + saveG.get(target.getUniqueId()));
                saveD.replace(target.getUniqueId(), testedD.get(target.getUniqueId()) + saveD.get(target.getUniqueId()));
                testedG.replace(target.getUniqueId(),0.0);
                testedD.replace(target.getUniqueId(),0.0);
                },i*20L);
        }
        Integer finalCheck = check;
        Bukkit.getScheduler().runTaskLater(Guard.getInstance(), () -> {
            player.sendMessage(Messages.prefix() + ChatUtil.translate(Messages.COMMANDS_CPS_END.getMessage().replace("%target%",target.getName()).replace("%left%",String.valueOf((saveG.get(target.getUniqueId())/ finalCheck))).replace("%right%",String.valueOf((saveD.get(target.getUniqueId())/ finalCheck)))));
            testedG.remove(target.getUniqueId());
            testedD.remove(target.getUniqueId());
            saveG.remove(target.getUniqueId());
            saveD.remove(target.getUniqueId());
        },check*20L);
    }
}
