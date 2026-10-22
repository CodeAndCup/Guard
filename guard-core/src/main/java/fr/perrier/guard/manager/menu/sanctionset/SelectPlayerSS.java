package fr.perrier.guard.manager.menu.sanctionset;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.pagination.PaginatedMenu;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.ItemBuilder;
import fr.perrier.guard.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SelectPlayerSS extends PaginatedMenu {
    @Override
    public String getPrePaginatedTitle(Player player) {
        return Messages.MENU_SANCTIONSET_SELECTOR_TITLE.getMessage();
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (Player target : player.getServer().getOnlinePlayers()) {
            buttons.put(buttons.size(), new PlayerButton(target));
        }
        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                return new ItemBuilder(Material.AIR).toItemStack();
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
                player.closeInventory();
                player.sendMessage(ChatUtil.prefix("&f#WeAreNotNazi"));
            }
        });
        return buttons;
    }

    @RequiredArgsConstructor
    private static class PlayerButton extends Button {
        private final Player target;

        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(XMaterial.PLAYER_HEAD.parseMaterial(),1,(byte) SkullType.PLAYER.ordinal())
                .setName("&f" + target.getName())
                .setLore(
                    Messages.MENU_SANCTIONSET_SELECTOR_PLAYER_LORE.getLore()
                    )
                .setSkullOwner(target.getName())
                .toItemStack();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            new MenuSS(new SelectPlayerSS(),target).openMenu(player);
        }
    }
}
