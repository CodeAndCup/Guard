package fr.perrier.guard.menu.buttons;


import com.cryptomorin.xseries.*;
import fr.perrier.guard.menu.*;
import fr.perrier.guard.utils.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class Glass extends Button {

    private int data;

    public Glass() {
        data = 7;
    }

    public Glass(int data) {
        this.data = data;
    }

    @Override
    public ItemStack getButtonItem(final Player player) {
        final ItemBuilder item = new ItemBuilder(XMaterial.WHITE_STAINED_GLASS_PANE.parseMaterial()).setDurability(data).setName(" ");
        return item.toItemStack();
    }

    @Override
    public void clicked(final Player player, final int i, final ClickType clickType, final int hb) {
    }
}
