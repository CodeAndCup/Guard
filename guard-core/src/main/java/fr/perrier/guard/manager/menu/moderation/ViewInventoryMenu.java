package fr.perrier.guard.manager.menu.moderation;

import fr.perrier.guard.menu.*;
import fr.perrier.guard.menu.buttons.*;
import fr.perrier.guard.utils.*;
import lombok.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

import java.util.*;

@RequiredArgsConstructor
public class ViewInventoryMenu extends Menu {
    private final Player target;

    @Override
    public String getTitle(Player paramPlayer) {
        return "&3Inventory &f" + target.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player paramPlayer) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (int i = 0; i < 18; i++) {
            buttons.put(i, new Glass());
        }

        PlayerInventory inv = target.getInventory();

        buttons.put(0, new DisplayButton(inv.getHelmet()));
        buttons.put(1, new DisplayButton(inv.getChestplate()));
        buttons.put(2, new DisplayButton(inv.getLeggings()));
        buttons.put(3, new DisplayButton(inv.getBoots()));

        buttons.put(5, new DisplayButton(new ItemBuilder(Material.COOKED_BEEF).setName("&f» &6Saturation: &f" + (int) target.getSaturation()).setLore("&f» &6Food: &f" + target.getFoodLevel()).toItemStack()));
        buttons.put(6, new DisplayButton(new ItemBuilder(Material.GOLDEN_APPLE).setName("&f» &cHealth: &f" + (int) target.getHealth() + "&r&c ❤").toItemStack()));

        int i = 18;
        for (ItemStack content : inv.getContents()) {
            if(i>=6*9) break;
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
