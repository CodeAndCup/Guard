package fr.perrier.guard.menu.pagination;


import com.cryptomorin.xseries.*;
import fr.perrier.guard.menu.*;
import fr.perrier.guard.utils.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class GlassFill extends Button {
    private final PaginatedMenu menu;

    public GlassFill(final PaginatedMenu menu) {
        this.menu = menu;
    }

    @Override
    public ItemStack getButtonItem(final Player player) {
        final ItemBuilder item = new ItemBuilder(XMaterial.WHITE_STAINED_GLASS_PANE.parseMaterial());
        item.setDurability(7);
        item.setName(" ");
        return item.toItemStack();
    }

    @Override
    public void clicked(final Player player, final int i, final ClickType clickType, final int hb) {
    }
}
