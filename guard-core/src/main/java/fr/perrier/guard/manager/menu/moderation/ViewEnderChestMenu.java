package fr.perrier.guard.manager.menu.moderation;

import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.Menu;
import fr.perrier.guard.menu.buttons.DisplayButton;
import fr.perrier.guard.menu.buttons.Glass;
import fr.perrier.guard.utils.ItemBuilder;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ViewEnderChestMenu extends Menu {
    private final Player target;

    @Override
    public String getTitle(Player paramPlayer) {
        return "&5EnderChest &f" + target.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player paramPlayer) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (int i = 0; i < 9; i++) {
            buttons.put(i, new Glass());
        }
        for (int i = 36; i < 54; i++) {
            buttons.put(i, new Glass());
        }

        Inventory inv = target.getEnderChest();

        int i = 9;
        for (ItemStack content : inv.getContents()) {
            if (content == null) {
                buttons.put(i, new DisplayButton(new ItemStack(Material.AIR)));
            } else {
                buttons.put(i, new DisplayButton(content));
            }
            i++;
        }

        return buttons;
    }
}
