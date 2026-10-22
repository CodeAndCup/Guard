package fr.perrier.guard.manager.menu.xray;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.manager.xray.XrayPlayer;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.pagination.PaginatedMenu;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.ItemBuilder;
import fr.perrier.guard.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class XrayMenu extends PaginatedMenu {
    @Override
    public String getPrePaginatedTitle(Player p0) {
        return Messages.MENU_XRAY_TITLE.getMessage();
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player p0) {
        HashMap<Integer, Button> buttons = new HashMap<Integer, Button>();

        for(Player player : Bukkit.getOnlinePlayers()) {
            buttons.put(buttons.size(), new XrayPlayerMenu(player));
        }

        return buttons;
    }

    @RequiredArgsConstructor
    public static class XrayPlayerMenu extends Button {
        private final Player player;

        @Override
        public ItemStack getButtonItem(Player p0) {

            ItemBuilder itemBuilder = new ItemBuilder(XMaterial.PLAYER_HEAD.parseItem())
                    .setSkullOwner(player.getDisplayName())
                    .setName(Messages.MENU_XRAY_PLAYER_NAME.getMessage().replace("%player%", player.getName()));
            for(String lore : Messages.MENU_XRAY_PLAYER_LORE.getLore())
                itemBuilder.addLoreLine(
                        lore.replace("%diamond%",Integer.toString(XrayPlayer.list.get(player).getDiamond()))
                                .replace("%pourcent_diamond%",(XrayPlayer.list.get(player).getDiamond() != 0 ? Float.toString(((float) XrayPlayer.list.get(player).getDiamond() /XrayPlayer.list.get(player).getStone())*100) : "0"))
                                .replace("%emerald%",Integer.toString(XrayPlayer.list.get(player).getEmerald()))
                                .replace("%pourcent_emerald%",(XrayPlayer.list.get(player).getEmerald() != 0 ? Float.toString(((float) XrayPlayer.list.get(player).getEmerald() /XrayPlayer.list.get(player).getStone())*100) : "0"))
                                .replace("%gold%",Integer.toString(XrayPlayer.list.get(player).getGold()))
                                .replace("%pourcent_gold%",(XrayPlayer.list.get(player).getGold() != 0 ? Float.toString(((float) XrayPlayer.list.get(player).getGold() /XrayPlayer.list.get(player).getStone())*100) : "0"))
                                .replace("%iron%",Integer.toString(XrayPlayer.list.get(player).getIron()))
                                .replace("%pourcent_iron%",(XrayPlayer.list.get(player).getIron() != 0 ? Float.toString(((float) XrayPlayer.list.get(player).getIron() /XrayPlayer.list.get(player).getStone())*100) : "0"))
                                .replace("%redstone%",Integer.toString(XrayPlayer.list.get(player).getRestone()))
                                .replace("%pourcent_redstone%",(XrayPlayer.list.get(player).getRestone() != 0 ? Float.toString(((float) XrayPlayer.list.get(player).getRestone() /XrayPlayer.list.get(player).getStone())*100) : "0"))
                                .replace("%lapis%",Integer.toString(XrayPlayer.list.get(player).getLapis()))
                                .replace("%pourcent_lapis%",(XrayPlayer.list.get(player).getLapis() != 0 ? Float.toString(((float) XrayPlayer.list.get(player).getLapis() /XrayPlayer.list.get(player).getStone())*100) : "0"))
                                .replace("%coal%",Integer.toString(XrayPlayer.list.get(player).getCoal()))
                                .replace("%pourcent_coal%",(XrayPlayer.list.get(player).getCoal() != 0 ?Float.toString(((float) XrayPlayer.list.get(player).getCoal() /XrayPlayer.list.get(player).getStone())*100) : "0"))
                                .replace("%stone%",Integer.toString(XrayPlayer.list.get(player).getStone()))
                );
            return itemBuilder.toItemStack();
        }

        @Override
        public void clicked(Player p0, int slot, ClickType clickType, int hotbarButton) {
            p0.teleport(player.getLocation());
            p0.sendMessage(ChatUtil.prefix(Messages.COMMANDS_TELEPORT_CONFIRMATION.getMessage().replace("%target%",player.getName())));
        }
    }
}
