package fr.perrier.guard.menu.buttons;


import fr.perrier.guard.menu.*;
import fr.perrier.guard.utils.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class BackButton extends Button {
    private final Menu back;

    public BackButton(final Menu back) {
        this.back = back;
    }

    @Override
    public ItemStack getButtonItem(final Player player) {
        final ItemBuilder item = new ItemBuilder(Material.ARROW).setName(ChatColor.translateAlternateColorCodes('&', "&f&l« &c&lBack"));
        return item.toItemStack();
    }

    @Override
    public void clicked(final Player player, final int i, final ClickType clickType, final int hb) {
        Button.playNeutral(player);
        this.back.openMenu(player);
    }
}
