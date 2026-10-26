package fr.perrier.guard.manager.menu.dashboard;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.menu.moderation.Parametres;
import fr.perrier.guard.manager.menu.xray.XrayMenu;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.GlassMenu;
import fr.perrier.guard.menu.buttons.ConversationButton;
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
 * aucune nouvelle logique. Cadre glass via GlassMenu (pattern documenté de la lib).
 */
public class ToolsMenu extends GlassMenu {

    @Override
    public String getTitle(Player player) {
        return ChatUtil.translate(Guard.getLangConfig().getString("menu.tools.title", "&2Outils"));
    }

    @Override
    public int getGlassColor() {
        return 9;
    }

    /** 27 slots : bordure GlassMenu {0,1,7,8,9,17,18,19,25,26}, tuiles centrées en rangée 2 (11/13/15). */
    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public Map<Integer, Button> getAllButtons(Player player) {
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

        // Parametres : menu existant (pas de perm — réglages perso)
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

        return buttons;
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
