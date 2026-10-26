package fr.perrier.guard.manager.menu.dashboard;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.Menu;
import fr.perrier.guard.menu.pagination.PaginatedMenu;
import fr.perrier.guard.reports.Report;
import fr.perrier.guard.reports.ReportCache;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.Heads;
import fr.perrier.guard.utils.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tuile Reports : file des reports OPEN.
 * source = ReportCache (memoire, rafraichi en async) : lecture SYNCHRONE à l'affichage,
 * donc aucun re-render asynchrone — c'etait la cause de l'affichage intermittent.
 */
public class ReportsMenu extends PaginatedMenu {

    /** Pur : glyphe du statut (deterministe, inconnu → "?"). */
    public static String statusGlyph(String status) {
        if (Report.STATUS_OPEN.equals(status)) {
            return "●";
        }
        if (Report.STATUS_IN_PROGRESS.equals(status)) {
            return "★";
        }
        if (Report.STATUS_CLOSED.equals(status)) {
            return "✔";
        }
        if (Report.STATUS_DELETED.equals(status)) {
            return "✘";
        }
        return "?";
    }

    /** Pur : template lang → lignes de lore (placeholders remplacés, sans traduction couleur ici). */
    public static List<String> loreLines(Report report, List<String> template) {
        List<String> out = new ArrayList<>();
        for (String line : template) {
            out.add(line
                    .replace("%id%", String.valueOf(report.getId()))
                    .replace("%reporter%", safe(report.getReporter()))
                    .replace("%reported%", safe(report.getReported()))
                    .replace("%reason%", safe(report.getReason()))
                    .replace("%server%", safe(report.getServerName()))
                    .replace("%date%", new SimpleDateFormat("dd/MM HH:mm").format(new Date(report.getCreatedAt())))
                    .replace("%status%", statusGlyph(report.getStatus())));
        }
        return out;
    }

    private static String safe(String s) {
        return s == null ? "?" : s;
    }

    @Override
    public String getPrePaginatedTitle(Player p0) {
        return ChatUtil.translate(Guard.getLangConfig().getString("menu.reports.title", "&2Reports"));
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player p0) {
        HashMap<Integer, Button> buttons = new HashMap<>();
        for (Report report : ReportCache.openSnapshot()) {
            buttons.put(buttons.size(), new ReportButton(report));
        }
        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        if (!ReportCache.openSnapshot().isEmpty()) {
            return null;
        }
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(22, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                // 0 report = vraiment 0 (cache synchrone) ; un store injoignable est signale
                // dans la console au rafraichissement du cache, pas ici (ecran vide ambigu sinon)
                return new ItemBuilder(XMaterial.PAPER.parseItem())
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.reports.empty", "&fAucun report")))
                        .toItemStack();
            }
        });
        return buttons;
    }

    @Override
    public Menu backButton() {
        return new DashboardMenu();
    }

    @Override
    public void onOpen(Player player) {
        // prepare la prochaine ouverture ; l'affichage courant lit deja le cache en synchrone
        ReportCache.refreshAsync();
    }

    /** Bouton d'un report : tête du joueur signalé + lore lang ; clic → écran de détail. */
    public static class ReportButton extends Button {
        private final Report report;

        public ReportButton(Report report) {
            this.report = report;
        }

        @Override
        public ItemStack getButtonItem(Player p0) {
            ItemBuilder item = new ItemBuilder(Heads.skullOf(report.getReported()))
                    .setName(ChatUtil.translate(Guard.getLangConfig()
                            .getString("menu.reports.item.name", "&f" + report.getReported())
                            .replace("%id%", String.valueOf(report.getId()))
                            .replace("%reported%", safe(report.getReported()))
                            .replace("%reporter%", safe(report.getReporter()))));
            List<String> template = Guard.getLangConfig().getStringList("menu.reports.item.lore");
            if (template.isEmpty()) {
                item.addLoreLine(ChatUtil.translate("&7" + safe(report.getReason())));
            } else {
                for (String line : loreLines(report, template)) {
                    item.addLoreLine(ChatUtil.translate(line));
                }
            }
            return item.toItemStack();
        }

        @Override
        public void clicked(Player p0, int slot, ClickType clickType, int hotbarButton) {
            Button.playNeutral(p0);
            new ReportDetailMenu(report, new ReportsMenu()).openMenu(p0);
        }
    }
}
