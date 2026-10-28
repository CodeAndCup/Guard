package fr.perrier.guard.manager.menu.moderation;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.GlassMenu;
import fr.perrier.guard.utils.ItemBuilder;
import fr.perrier.guard.utils.Messages;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class FreezeMenu extends GlassMenu {

    @Override
    public int getGlassColor() {
        return 0;
    }

    @Override
    public Map<Integer, Button> getAllButtons(Player player) {
        HashMap<Integer, Button> buttons = new HashMap<Integer, Button>();
        buttons.put(13, new Button() {
            @Override
            public ItemStack getButtonItem(Player p0) {
                return new ItemBuilder(XMaterial.ICE.parseMaterial())
                        .setName(Messages.MENU_FREEZE_TITLE.getMessage())
                        .setLore(Messages.MENU_FREEZE_LORE.getLore())
                        .toItemStack();
            }
        });
        return buttons;
    }

    @Override
    public String getTitle(Player paramPlayer) {
        return Messages.MENU_FREEZE_TITLE.getMessage();
    }
}
