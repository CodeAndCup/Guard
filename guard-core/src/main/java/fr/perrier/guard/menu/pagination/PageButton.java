package fr.perrier.guard.menu.pagination;


import fr.perrier.guard.*;
import fr.perrier.guard.menu.*;
import fr.perrier.guard.utils.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PageButton extends Button {
    private final int mod;
    private final PaginatedMenu menu;

    public PageButton(final int mod, final PaginatedMenu menu) {
        this.mod = mod;
        this.menu = menu;
    }

    @Override
    public ItemStack getButtonItem(final Player player) {
        // flèches VANILLA (ARROW existe depuis 1.8) : plus aucune dépendance aux textures
        // custom (authlib/PropertySet) — elles provoquaient des NoSuchMethodError/NoSuchMethodException
        // sur chaque version de Paper qui changeait son API profile.
        if (!this.hasNext(player)) {
            return new ItemBuilder(Material.AIR).toItemStack();
        }
        return new ItemBuilder(Material.ARROW)
                .setName(ChatUtil.translate(this.mod > 0
                        ? "#2EE6D6&lPage suivante &8&l»"
                        : "&8&l« #2EE6D6&lPage précédente"))
                .toItemStack();
    }

    @Override
    public void clicked(final Player player, final int i, final ClickType clickType, final int hb) {
        if (this.hasNext(player)) {
            this.menu.modPage(player, this.mod);
            Button.playNeutral(player);
            InventoryUpdate.updateInventory(Guard.getInstance(), player, menu.getTitle(player));
        }
    }

    private boolean hasNext(final Player player) {
        final int pg = this.menu.getPage() + this.mod;
        return pg > 0 && this.menu.getPages(player) >= pg;
    }
}
