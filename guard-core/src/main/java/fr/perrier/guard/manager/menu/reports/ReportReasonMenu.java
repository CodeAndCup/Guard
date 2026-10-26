package fr.perrier.guard.manager.menu.reports;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.GlassMenu;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Choix de la raison d'un signalement : les raisons sont itérées depuis les clés de lang
 * {@code reports.reasons.<clé>.name} (aucune enum codée). « Raison libre » apparaît si le mode le permet.
 */
public class ReportReasonMenu extends GlassMenu {

    private final Player reporter;
    private final String target;

    public ReportReasonMenu(Player reporter, String target) {
        this.reporter = reporter;
        this.target = target;
    }

    /** Pur (testé) : clés des raisons dans l'ordre du fichier ; section absente → vide. */
    public static List<String> reasonKeys(FileConfiguration lang) {
        ConfigurationSection section = lang.getConfigurationSection("reports.reasons");
        if (section == null) {
            return new ArrayList<>();
        }
        List<String> out = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            if (section.isString(key + ".name")) {
                out.add(key);
            }
        }
        return out;
    }

    @Override
    public String getTitle(Player player) {
        return ChatUtil.translate(Guard.getLangConfig()
                .getString("menu.reports.reason.title", "&cRaison du signalement &f%target%")
                .replace("%target%", target));
    }

    @Override
    public int getGlassColor() {
        return 9;
    }

    @Override
    public Map<Integer, Button> getAllButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int slot = 10; // hors slots de bordure GlassMenu

        for (final String key : reasonKeys(Guard.getLangConfig())) {
            final String label = Guard.getLangConfig().getString("reports.reasons." + key + ".name", key);
            buttons.put(slot++, new Button() {
                @Override
                public ItemStack getButtonItem(Player p) {
                    ItemBuilder item = new ItemBuilder(XMaterial.PAPER.parseItem())
                            .setName(ChatUtil.translate(Guard.getLangConfig()
                                    .getString("menu.reports.reason.item.name", "&a%reason%")
                                    .replace("%reason%", label)
                                    .replace("%target%", target)));
                    for (String lore : Guard.getLangConfig().getStringList("menu.reports.reason.item.lore")) {
                        item.addLoreLine(ChatUtil.translate(lore
                                .replace("%reason%", label)
                                .replace("%target%", target)));
                    }
                    return item.toItemStack();
                }

                @Override
                public void clicked(Player p, int i, ClickType clickType, int hotbarButton) {
                    Button.playNeutral(p);
                    new ReportConfirmMenu(reporter, target, key, label).openMenu(p);
                }
            });
        }

        // raison libre (commande) si le mode le permet
        String mode = Guard.getDefaultConfig().getString("reports.reason-mode", "BOTH");
        if (!"PREDEFINED".equalsIgnoreCase(mode)) {
            buttons.put(slot, new Button() {
                @Override
                public ItemStack getButtonItem(Player p) {
                    return new ItemBuilder(XMaterial.WRITABLE_BOOK.parseItem())
                            .setName(ChatUtil.translate(Guard.getLangConfig()
                                    .getString("menu.reports.reason.free.name", "&eRaison libre")))
                            .toItemStack();
                }

                @Override
                public void clicked(Player p, int i, ClickType clickType, int hotbarButton) {
                    Button.playNeutral(p);
                    p.closeInventory();
                    p.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                            .getString("menu.reports.reason.free.hint", "&fTapez &b/report " + target + " <raison>"))));
                }
            });
        }
        return buttons;
    }
}
