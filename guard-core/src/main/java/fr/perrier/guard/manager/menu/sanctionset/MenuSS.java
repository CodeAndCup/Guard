package fr.perrier.guard.manager.menu.sanctionset;

import fr.perrier.guard.manager.menu.sanctionset.impl.*;
import fr.perrier.guard.menu.*;
import fr.perrier.guard.menu.buttons.*;
import fr.perrier.guard.utils.*;
import lombok.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class MenuSS extends GlassMenu {

    private final Menu oldMenu;
    private final Player target;

    @Override
    public String getTitle(Player player) {
        return Messages.MENU_SANCTIONSET_MAIN_TITLE.getMessage().replace("%target%", target.getName());
    }

    @Override
    public int getGlassColor() { return 14;}

    @Override
    public Map<Integer, Button> getAllButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(12, new Mute());
        buttons.put(13, new Warn());
        buttons.put(14, new Ban());
        buttons.put(21, new DisplayButton(new ItemStack(Material.AIR)));

        return buttons;
    }

    private class Mute extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder itemBuilder = new ItemBuilder(Material.BOOK);
            itemBuilder.setName(Messages.MENU_SANCTIONSET_MAIN_MUTETITLE.getMessage());

            for(String lore: Messages.MENU_SANCTIONSET_MAIN_MUTELORE.getLore()) {
                itemBuilder.addLoreLine(lore);
            }
            return itemBuilder.toItemStack();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            new MuteSS(target).openMenu(player);
        }
    }

    private class Warn extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder itemBuilder = new ItemBuilder(Material.MAP);
            itemBuilder.setName(Messages.MENU_SANCTIONSET_MAIN_WARNTITLE.getMessage());

            for(String lore : Messages.MENU_SANCTIONSET_MAIN_WARNLORE.getLore()) {
                itemBuilder.addLoreLine(lore);
            }

            return itemBuilder.toItemStack();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            new WarnSS(target).openMenu(player);
        }
    }

    private class Ban extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder itemBuilder = new ItemBuilder(Material.ANVIL);
            itemBuilder.setName(Messages.MENU_SANCTIONSET_MAIN_BANTITLE.getMessage());

            for(String lore : Messages.MENU_SANCTIONSET_MAIN_BANLORE.getLore()) {
                itemBuilder.addLoreLine(lore);
            }
            return itemBuilder.toItemStack();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {

            if(player.hasPermission("guard.ss_ban")) {
                new BanSS(target).openMenu(player);
            }else{
                player.closeInventory();
                player.sendMessage(Messages.NOPERM.getMessage());
            }
        }
    }

}
