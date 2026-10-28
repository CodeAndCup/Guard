package fr.perrier.guard.automod.chat;

import fr.perrier.guard.Guard;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.List;

public class AutoMute implements Listener {

    HashMap<Player,Integer> warning = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String msg = e.getMessage();

        if(p.hasPermission("guard.automod.mute.bypass")) return;

        List<String> words = Guard.getDefaultConfig().getStringList("automod.banned_word");

        for(String check : msg.replaceAll("\\p{Punct}", "").split("\\s+")) {
            for(String checking : words) {
                if(check.toLowerCase().equalsIgnoreCase(checking)) {
                    e.setCancelled(true);
                    e.setMessage("");

                    for(String line : Messages.AUTOMOD_CHAT_WARN.getLore())
                        p.sendMessage(ChatUtil.translate(line));

                    Bukkit.getOnlinePlayers().forEach(players -> {
                        if(players.hasPermission("guard.automod")) {
                            players.sendMessage(Messages.prefix() + ChatUtil.translate(Messages.AUTOMOD_CHAT_ALERT.getMessage().replace("%target%", p.getName()).replace("%message%", msg)));
                        }
                    });
                    if(Guard.getDefaultConfig().getInt("automod.number_warn")==-1) return;
                    if(warning.containsKey(p)) {
                        if(warning.get(p) > Guard.getDefaultConfig().getInt("automod.number_warn")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),Guard.getDefaultConfig().getString("sanction.mute").replace("%target%", p.getName()).replace("%duration%","3h").replace("%reason%", "&8[&3GUARD &f» &bAutoMute&8] &f: &c" + checking));
                            warning.remove(p);
                        }
                        warning.replace(p,warning.get(p)+1);
                    }else{
                        warning.put(p,1);
                    }
                    return;
                }
            }
        }
    }
}
