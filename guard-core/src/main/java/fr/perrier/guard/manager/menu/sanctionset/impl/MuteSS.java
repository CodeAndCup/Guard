package fr.perrier.guard.manager.menu.sanctionset.impl;

import com.cryptomorin.xseries.*;
import fr.perrier.guard.*;
import fr.perrier.guard.manager.commands.*;
import fr.perrier.guard.manager.menu.sanctionset.*;
import fr.perrier.guard.menu.*;
import fr.perrier.guard.menu.pagination.*;
import fr.perrier.guard.utils.*;
import lombok.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;

import java.util.*;

@RequiredArgsConstructor
public class MuteSS extends PaginatedMenu {

    private final Player target;

    @Override
    public String getPrePaginatedTitle(Player player) {
        return Messages.MENU_SANCTIONSET_MUTE_TITLE.getMessage().replace("%target%", target.getName());
    }

    @Override
    public int getGlassColor() { return 14;}

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for(SanctionSS values : SanctionSS.values()) {
            if(!values.getType().equalsIgnoreCase("Mute")) continue;
            buttons.put(buttons.size(),new MuteSanction(values));
        }

        return buttons;
    }

    @Override
    public Menu backButton() {
        return new MenuSS(null,target);
    }

    @RequiredArgsConstructor
    private class MuteSanction extends Button {
        private final SanctionSS value;

        @Override
        public boolean shouldUpdate(Player player, int slot, ClickType clickType) {
            return true;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return getSanctionItem(value);
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {

            player.closeInventory();

            try {
                new ConfirmationMenu(() -> {

                    String sanction = Guard.getDefaultConfig().getString("sanction.mute");
                    if(sanction == null) throw new RuntimeException("Sanction Command Null");
                    sanction = sanction.replace("%target%", target.getName());
                    sanction = sanction.replace("%duration%", value.getDuration().replace("Perm",Objects.requireNonNull(Guard.getDefaultConfig().getString("sanction.duration_permanant"))));
                    sanction = sanction.replace("%reason%",value.getReason());

                    player.performCommand(sanction);
                    player.closeInventory();
                }, getSanctionItem(value), new MuteSS(target)).openMenu(player);
            }catch (Exception e1) {
                player.sendMessage(ChatUtil.translate(Messages.prefix() + "&4ERR0R please contact support with this ID: [SS-MUTE~L.78]"));
                e1.printStackTrace();
            }
        }
    }

    public static ItemStack getSanctionItem(SanctionSS value) {
        ItemBuilder itemBuilder;
        try {
            itemBuilder = new ItemBuilder(value.getItem());
            itemBuilder.setName("&f" + value.getReason());

            for (String lore : Messages.MENU_SANCTIONSET_MUTE_LORE.getLore()) {
                itemBuilder.addLoreLine(lore.replace("%type%", value.getType()).replace("%reason%", value.getReason()).replace("%duration%", value.getDuration()));
            }
        }catch (Exception e1) {
             itemBuilder = new ItemBuilder(XMaterial.BARRIER.parseItem());
             itemBuilder.setName("&4&lERR0R");
             itemBuilder.setLore(" ","&cPlease look logs and contact support.");
        }
        return itemBuilder.toItemStack();
    }
}
