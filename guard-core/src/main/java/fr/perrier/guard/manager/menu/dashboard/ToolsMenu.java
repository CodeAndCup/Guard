package fr.perrier.guard.manager.menu.dashboard;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.menu.moderation.Parametres;
import fr.perrier.guard.manager.menu.xray.XrayMenu;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.Menu;
import fr.perrier.guard.menu.buttons.ConversationButton;
import fr.perrier.guard.menu.buttons.Glass;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.ItemBuilder;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Tuile Outils : X-Ray / LookUp / Parametres — wiring pur vers les menus et la commande existants,
 * aucune nouvelle logique. LookUp sans cible = saisie du pseudo par conversation, puis la commande existante.
 */
public class ToolsMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return ChatUtil.translate(Guard.getLangConfig().getString("menu.tools.title", "&2Outils"));
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        // X-Ray : le menu existant, caché si pas de perm
        if (player.hasPermission("guard.xray")) {
            buttons.put(11, new Button() {
                @Override
                public ItemStack getButtonItem(Player p) {
                    return tile(XMaterial.DIAMOND_ORE, "xray");
                }

                @Override
                public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                    Button.playNeutral(p);
                    new XrayMenu().openMenu(p);
                }
            });
        }
        // LookUp : la commande existante, cible saisie par conversation
        if (player.hasPermission("guard.lookup")) {
            ItemStack item = tile(XMaterial.PAPER, "lookup");
            buttons.put(13, new ConversationButton<String>(item, "lookup",
                    ChatUtil.translate(Guard.getLangConfig().getString("menu.tools.lookup.prompt", "&fNom du joueur &b?")),
                    (cmdName, pair) -> {
                        ConversationContext cc = pair.getLeft();
                        Player staff = (Player) cc.getForWhom();
                        staff.performCommand(cmdName + " " + pair.getRight().trim());
                    }));
        }
        // Parametres : menu existant (no perm — réglages perso)
        buttons.put(15, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                return tile(XMaterial.FEATHER, "parameters");
            }

            @Override
            public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                Button.playNeutral(p);
                new Parametres().openMenu(p);
            }
        });

        for (int i = 0; i < getSize(); i++) {
            if (!buttons.containsKey(i)) {
                buttons.put(i, new Glass(15));
            }
        }
        return buttons;
    }

    @Override
    public int getSize() {
        return 27;
    }

    private ItemStack tile(XMaterial material, String key) {
        ItemBuilder item = new ItemBuilder(material.parseItem())
                .setName(ChatUtil.translate(Guard.getLangConfig().getString("menu.tools." + key + ".name", key)));
        for (String line : Guard.getLangConfig().getStringList("menu.tools." + key + ".lore")) {
            item.addLoreLine(ChatUtil.translate(line));
        }
        return item.toItemStack();
    }
}
