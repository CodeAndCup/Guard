package fr.perrier.guard.menu.buttons;


import fr.perrier.guard.menu.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class EasyButton extends Button {

    final ItemStack itemStack;
    final Consumer<Player> consumer;

    public EasyButton(ItemStack itemStack, Consumer<Player> player) {
        this.itemStack = itemStack;
        this.consumer = player;
    }

    @Override
    public ItemStack getButtonItem(Player p0) {
        return itemStack;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
        this.consumer.accept(player);
    }
}
