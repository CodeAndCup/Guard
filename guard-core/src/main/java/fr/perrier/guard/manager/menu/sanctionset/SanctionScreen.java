package fr.perrier.guard.manager.menu.sanctionset;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.commands.SanctionSet;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.Menu;
import fr.perrier.guard.menu.buttons.BackButton;
import fr.perrier.guard.menu.buttons.ConversationButton;
import fr.perrier.guard.menu.buttons.Glass;
import fr.perrier.guard.menu.pagination.ConfirmationMenu;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.ItemBuilder;
import fr.perrier.guard.utils.Messages;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sanctions en 1 écran : filtres (Mute/Warn/Ban selon les clés lang + perms), raisons itérées
 * depuis sanction.<cat>.<raison>.name/.duration, durée perso ConversationButton, ConfirmationMenu →
 * execute la commande console de la config via SS.runSanction.
 */
public class SanctionScreen extends Menu {

    private static final List<String> ALL_CATEGORIES = Arrays.asList("Mute", "Warn", "Ban");
    /** Slots de bordure d'un inventaire 45 (meme set que GlassMenu) : filtres/raisons/back les evitant. */
    private static final java.util.Set<Integer> BORDER_45 =
            new java.util.LinkedHashSet<>(Arrays.asList(0, 1, 7, 8, 9, 17, 27, 35, 36, 37, 43, 44));

    /** Pur : catégories présentes (Mute/Warn/Ban dans cet ordre), absente si section lang manquante. */
    public static List<String> categories(FileConfiguration lang) {
        List<String> out = new ArrayList<>();
        for (String cat : ALL_CATEGORIES) {
            if (lang.getConfigurationSection("sanction." + cat.toLowerCase()) != null) {
                out.add(cat);
            }
        }
        return out;
    }

    /** Pur : ids des raisons (clés de la section), dans l'ordre du fichier ; catégorie absente → vide. */
    public static List<String> reasons(FileConfiguration lang, String category) {
        ConfigurationSection section = lang.getConfigurationSection("sanction." + category.toLowerCase());
        if (section == null) {
            return new ArrayList<>();
        }
        List<String> out = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            if (section.isString(key + ".name") || section.isString(key + ".duration")) {
                out.add(key);
            }
        }
        return out;
    }

    /** Pur : nom affiché de la raison — déjà présentée sous '%reason%' à l'ancien code. */
    public static String reasonName(FileConfiguration lang, String category, String key) {
        return lang.getString("sanction." + category.toLowerCase() + "." + key + ".name", key);
    }

    /** Pur : durée de la raison (ou "?" si absente). */
    public static String reasonDuration(FileConfiguration lang, String category, String key) {
        return lang.getString("sanction." + category.toLowerCase() + "." + key + ".duration", "?");
    }

    /** Pur : perm requise pour la catégorie (plugin.yml). */
    public static String categoryPerm(String category) {
        return "guard.ss_" + category.toLowerCase();
    }

    private final Menu oldMenu;
    private final org.bukkit.entity.Player target;

    public SanctionScreen(Menu oldMenu, Player target) {
        this.oldMenu = oldMenu;
        this.target = target;
    }

    /** Pur : lore enum existante par catégorie (menu.sanctionset.<cat>.lore). */
    public static List<String> categoryLore(String category) {
        switch (category) {
            case "Warn":
                return Messages.MENU_SANCTIONSET_WARN_LORE.getLore();
            case "Ban":
                return Messages.MENU_SANCTIONSET_BAN_LORE.getLore();
            default:
                return Messages.MENU_SANCTIONSET_MUTE_LORE.getLore();
        }
    }

    @Override
    public String getTitle(Player player) {
        return ChatUtil.translate(Guard.getLangConfig()
                .getString("menu.sanction.title", "&2Sanctions &f%target%")
                .replace("%target%", target.getName()));
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        List<String> visible = new ArrayList<>();
        for (String cat : categories(Guard.getLangConfig())) {
            if (player.hasPermission(categoryPerm(cat))) {
                visible.add(cat);
            }
        }
        final String current = visible.contains(selected) ? selected : (visible.isEmpty() ? null : visible.get(0));

        // filtres (rangée du haut), catégorie active marquée en palette flash
        int filterSlot = 2;
        for (final String cat : visible) {
            final boolean active = cat.equals(current);
            buttons.put(filterSlot, new Button() {
                @Override
                public ItemStack getButtonItem(Player p) {
                    String name = Guard.getLangConfig().getString(
                            "menu.sanction.category." + cat.toLowerCase() + (active ? ".selected" : ".name"),
                            active ? "▶ " + cat : cat);
                    return new ItemBuilder(categoryItem(cat)).setName(ChatUtil.translate(name)).toItemStack();
                }

                @Override
                public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                    selected = cat;
                }

                @Override
                public boolean shouldUpdate(Player p, int slot, ClickType clickType) {
                    return true; // sans ça, changer de catégorie ne re-rendait pas le menu (aucun effet visible)
                }
            });
            filterSlot++;
        }

        // toggle durée personnalisée
        if (current != null) {
            final String category = current;
            buttons.put(6, new Button() {
                @Override
                public ItemStack getButtonItem(Player p) {
                    String key = customDuration ? "toggle.on" : "toggle.off";
                    return new ItemBuilder(XMaterial.CLOCK.parseItem())
                            .setName(ChatUtil.translate(Guard.getLangConfig()
                                    .getString("menu.sanction." + key, customDuration ? "&aDurée perso: ON" : "&7Durée perso: OFF")))
                            .toItemStack();
                }

                @Override
                public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                    customDuration = !customDuration;
                }

                @Override
                public boolean shouldUpdate(Player p, int slot, ClickType clickType) {
                    return true; // re-render pour refleter le nouvel etat du toggle
                }
            });

            // raisons de la catégorie active, itérées depuis les clés lang
            final Menu back = oldMenu != null ? oldMenu : new SelectPlayerSS();
            int reasonSlot = 19;
            for (final String key : reasons(Guard.getLangConfig(), category)) {
                final String name = reasonName(Guard.getLangConfig(), category, key);
                final String duration = reasonDuration(Guard.getLangConfig(), category, key);
                if (BORDER_45.contains(reasonSlot)) {
                    reasonSlot++; // slot réservé à la bordure
                    continue;
                }
                if (reasonSlot >= getSize() - 4) {
                    break; // 1 page v1 ; pagination si ça déborde (monter en PaginatedMenu si besoin)
                }
                buttons.put(reasonSlot++, new Button() {
                    @Override
                    public ItemStack getButtonItem(Player p) {
                        ItemStack item = new ItemBuilder(categoryItem(category)).setName(ChatUtil.translate("&f" + name)).toItemStack();
                        ItemBuilder builder = new ItemBuilder(item);
                        for (String lore : categoryLore(category)) {
                            builder.addLoreLine(ChatUtil.translate(lore
                                    .replace("%type%", category)
                                    .replace("%reason%", name)
                                    .replace("%duration%", duration)));
                        }
                        return builder.toItemStack();
                    }

                    @Override
                    public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                        confirm(p, category, name, duration, back);
                    }
                });
            }

            // back hors bordure
            buttons.put(41, new BackButton(back));
        }

        // bordure seule (pattern glass de la lib) — plus de remplissage integral
        for (int i : new int[]{0, 1, 7, 8, 9, 17, 27, 35, 36, 37, 43, 44}) {
            if (!buttons.containsKey(i)) {
                buttons.put(i, new Glass(9));
            }
        }
        return buttons;
    }

    @Override
    public int getSize() {
        return 45;
    }

    private ItemStack confirmItem(Player p, String category, String name, String duration) {
        return new ItemBuilder(categoryItem(category)).setName(ChatUtil.translate("&f" + name)).toItemStack();
    }

    private void confirm(Player player, String category, String reasonName, String duration, Menu back) {
        if (customDuration) {
            ItemStack item = confirmItem(player, category, reasonName, duration);
            String message = ChatUtil.translate(Guard.getLangConfig()
                    .getString("menu.sanction.custom-duration", "&fDurée pour &b" + reasonName + "&f ?")
                    .replace("%reason%", reasonName));
            new ConversationButton<String>(item, reasonName, message, (targetReason, pair) -> {
                ConversationContext cc = pair.getLeft();
                Player staff = (Player) cc.getForWhom();
                new ConfirmationMenu(() -> SanctionSet.runSanction(staff, category, target, pair.getRight(), targetReason),
                        item, back).openMenu(staff);
            }).clicked(player, 0, null, 0);
            return;
        }
        new ConfirmationMenu(() -> SanctionSet.runSanction(player, category, target, duration, reasonName),
                confirmItem(player, category, reasonName, duration), back).openMenu(player);
    }

    private org.bukkit.Material categoryItem(String category) {
        switch (category) {
            case "Warn":
                return Material.MAP;
            case "Ban":
                return Material.ANVIL;
            default:
                return Material.BOOK;
        }
    }

    private String selected;
    private boolean customDuration;
}
