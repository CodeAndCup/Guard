package fr.perrier.guard.manager.menu.dashboard;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.commands.ChatLock;
import fr.perrier.guard.manager.commands.NoClip;
import fr.perrier.guard.manager.commands.StaffChat;
import fr.perrier.guard.manager.commands.Vanish;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.Menu;
import fr.perrier.guard.menu.buttons.Glass;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.ItemBuilder;
import fr.perrier.guard.utils.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard /guard : 4 tuiles (Joueurs / Reports / Modération / Outils).
 * Contenu 100% piloté par les clés lang menu.dashboard.* (clés garanties au boot par LangMigrator).
 */
public class DashboardMenu extends Menu {

    public static final int SLOT_PLAYERS = 10;
    public static final int SLOT_REPORTS = 12;
    public static final int SLOT_MODERATION = 14;
    public static final int SLOT_TOOLS = 16;

    /** Pur : slot → section lang de la tuile (testé). */
    public static String tileKey(int slot) {
        switch (slot) {
            case SLOT_PLAYERS:
                return "players";
            case SLOT_REPORTS:
                return "reports";
            case SLOT_MODERATION:
                return "moderation";
            case SLOT_TOOLS:
                return "tools";
            default:
                return null;
        }
    }

    @Override
    public String getTitle(Player player) {
        return lang("title", "&2&lGuard");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(SLOT_PLAYERS, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                return tile(p, XMaterial.PLAYER_HEAD, "players");
            }

            @Override
            public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                Button.playNeutral(p);
                new PlayersMenu().openMenu(p);
            }
        });

        buttons.put(SLOT_REPORTS, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                return tile(p, XMaterial.CHEST, "reports");
            }

            @Override
            public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                if (!p.hasPermission("guard.reports.view")) {
                    Messages.send(p, Messages.NOPERM);
                    return;
                }
                // tuile câblée à la tâche 3 (ReportsMenu) — stub temporaire
                Button.playNeutral(p);
                Messages.send(p, Messages.COMING_SOON);
            }
        });

        buttons.put(SLOT_MODERATION, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                return tile(p, XMaterial.IRON_AXE, "moderation");
            }

            @Override
            public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                Button.playNeutral(p);
                new ModerationMenu().openMenu(p);
            }
        });

        buttons.put(SLOT_TOOLS, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                return tile(p, XMaterial.ANVIL, "tools");
            }

            @Override
            public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                Button.playNeutral(p);
                new ToolsMenu().openMenu(p);
            }
        });

        for (int i = 0; i < getSize(); i++) {
            if (!buttons.containsKey(i)) {
                buttons.put(i, new Glass(15));
            }
        }
        return buttons;
    }

    private ItemStack tile(Player p, XMaterial material, String key) {
        ItemBuilder builder = new ItemBuilder(material.parseItem())
                .setName(lang(key + ".name", key));
        for (String lore : langList(key + ".lore")) {
            builder.addLoreLine(lore);
        }
        return builder.toItemStack();
    }

    @Override
    public int getSize() {
        return 27;
    }

    static String lang(String key, String def) {
        return ChatUtil.translate(Guard.getLangConfig().getString("menu.dashboard." + key, def));
    }

    static List<String> langList(String key) {
        List<String> raw = Guard.getLangConfig().getStringList("menu.dashboard." + key);
        List<String> out = new ArrayList<>();
        for (String s : raw) {
            out.add(ChatUtil.translate(s));
        }
        return out;
    }

    /** Sous-menu Modération : les mêmes actions que les commandes existantes (aucune duplication), cachées selon la perm. */
    public static class ModerationMenu extends Menu {

        @Override
        public String getTitle(Player player) {
            return lang("moderation.title", "&2Moderation");
        }

        @Override
        public Map<Integer, Button> getButtons(Player player) {
            Map<Integer, Button> buttons = new HashMap<>();
            put(buttons, player, 10, XMaterial.SPIDER_EYE, "vanish", "guard.vanish", () -> Vanish.onVanish(player));
            put(buttons, player, 12, XMaterial.PAPER, "chatlock", "guard.chatlock", () -> ChatLock.onChatLock(player));
            put(buttons, player, 14, XMaterial.OAK_SIGN, "staffchat", "guard.staffchat", () -> StaffChat.onStaffChat(player));
            put(buttons, player, 16, XMaterial.BARRIER, "noclip", "guard.noclip", () -> NoClip.onNoClip(player));
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

        private void put(Map<Integer, Button> buttons, Player p, int slot, XMaterial material, String key,
                         String perm, Runnable action) {
            if (!p.hasPermission(perm)) {
                return;
            }
            buttons.put(slot, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    ItemBuilder builder = new ItemBuilder(material.parseItem())
                            .setName(lang("moderation." + key + ".name", key));
                    for (String lore : langList("moderation." + key + ".lore")) {
                        builder.addLoreLine(lore);
                    }
                    return builder.toItemStack();
                }

                @Override
                public void clicked(Player player, int i, ClickType clickType, int hotbarButton) {
                    player.closeInventory();
                    action.run();
                }
            });
        }
    }
}
