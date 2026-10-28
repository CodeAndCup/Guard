package fr.perrier.guard.manager.menu.sanctionset;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.commands.SanctionSet;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.Menu;
import fr.perrier.guard.menu.buttons.ConversationButton;
import fr.perrier.guard.menu.pagination.ConfirmationMenu;
import fr.perrier.guard.menu.pagination.PaginatedMenu;
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
 * Sanctions : filtres (Mute/Warn/Ban selon les clés lang + perms), raisons paginées
 * (15/page, le nombre varie par catégorie) itérées depuis sanction.<cat>.<raison>.name/.duration,
 * durée perso ConversationButton, ConfirmationMenu → execute la commande console de la
 * config via SS.runSanction. Cadre immutable : un clic sur un filtre/toggle rouvre une
 * nouvelle instance (reset page 1), même mécanique que ReportsMenu.
 */
public class SanctionScreen extends PaginatedMenu {

    private static final List<String> ALL_CATEGORIES = Arrays.asList("Mute", "Warn", "Ban");

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
    private final String category;
    private final boolean customDuration;

    public SanctionScreen(Menu oldMenu, Player target) {
        this(oldMenu, target, null, false);
    }

    private SanctionScreen(Menu oldMenu, Player target, String category, boolean customDuration) {
        this.oldMenu = oldMenu;
        this.target = target;
        this.category = category;
        this.customDuration = customDuration;
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

    /** Catégories visibles pour le joueur (perms). */
    private List<String> visibleCategories(Player player) {
        List<String> visible = new ArrayList<>();
        for (String cat : categories(Guard.getLangConfig())) {
            if (player.hasPermission(categoryPerm(cat))) {
                visible.add(cat);
            }
        }
        return visible;
    }

    /** Catégorie active : celle choisie si toujours visible, sinon la première. */
    private String currentCategory(Player player) {
        List<String> visible = visibleCategories(player);
        return visible.contains(category) ? category : (visible.isEmpty() ? null : visible.get(0));
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return ChatUtil.translate(Guard.getLangConfig()
                .getString("menu.sanction.title", "&2Sanctions &f%target%")
                .replace("%target%", target.getName()));
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        HashMap<Integer, Button> buttons = new HashMap<>();
        String current = currentCategory(player);
        if (current == null) {
            return buttons;
        }
        final String category = current;
        final Menu back = backButton();
        for (final String key : reasons(Guard.getLangConfig(), category)) {
            final String name = reasonName(Guard.getLangConfig(), category, key);
            final String duration = reasonDuration(Guard.getLangConfig(), category, key);
            buttons.put(buttons.size(), new Button() {
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
        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        final String current = currentCategory(player);

        // filtres (rangée du haut), catégorie active marquée en palette flash
        int filterSlot = 2;
        for (final String cat : visibleCategories(player)) {
            final boolean active = cat.equals(current);
            buttons.put(filterSlot++, new Button() {
                @Override
                public ItemStack getButtonItem(Player p) {
                    String name = Guard.getLangConfig().getString(
                            "menu.sanction.category." + cat.toLowerCase() + (active ? ".selected" : ".name"),
                            active ? "▶ " + cat : cat);
                    return new ItemBuilder(categoryItem(cat)).setName(ChatUtil.translate(name)).toItemStack();
                }

                @Override
                public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                    new SanctionScreen(oldMenu, target, cat, customDuration).openMenu(p);
                }
            });
        }

        // toggle durée personnalisée (rouvre une nouvelle instance : reset page 1)
        if (current != null) {
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
                    new SanctionScreen(oldMenu, target, current, !customDuration).openMenu(p);
                }
            });
        }
        return buttons;
    }

    @Override
    public Menu backButton() {
        return oldMenu != null ? oldMenu : new SelectPlayerSS();
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
}
