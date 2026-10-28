package fr.perrier.guard.menu.buttons;


import com.cryptomorin.xseries.*;
import fr.perrier.guard.menu.*;
import fr.perrier.guard.utils.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class CloseButton extends Button {
    @Override
    public ItemStack getButtonItem(final Player player) {
        final ItemBuilder item = new ItemBuilder(XMaterial.INK_SAC.parseMaterial()).setDurability(1).setName(ChatColor.translateAlternateColorCodes('&', "&cClose"));
        return item.toItemStack();
    }

    @Override
    public void clicked(final Player player, final int i, final ClickType clickType, final int hb) {
        Button.playNeutral(player);
        player.closeInventory();
    }
}
