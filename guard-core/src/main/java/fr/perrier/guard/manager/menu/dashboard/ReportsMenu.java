package fr.perrier.guard.manager.menu.dashboard;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.menu.dashboard.DashboardMenu;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.Menu;
import fr.perrier.guard.menu.pagination.PaginatedMenu;
import fr.perrier.guard.reports.Report;
import fr.perrier.guard.reports.ReportCache;
import fr.perrier.guard.reports.ReportFilters;
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
 * File des signalements côté staff : filtres (OPEN / IN_PROGRESS / CLOSED / DELETED),
 * tri, toggle « seulement les rapports dont je suis responsable », recherche active.
 * source = {@link ReportCache} (mémoire, rafraîchi en async) : lecture SYNCHRONE, aucun re-render.
 */
public class ReportsMenu extends PaginatedMenu {

    private final String filter;
    private final String sort;
    private final boolean onlyMine;
    private final String query;

    public ReportsMenu() {
        this(Report.STATUS_OPEN, ReportFilters.SORT_NEWEST, false, null);
    }

    public ReportsMenu(String filter, String sort, boolean onlyMine, String query) {
        this.filter = filter;
        this.sort = sort;
        this.onlyMine = onlyMine;
        this.query = query;
    }

    /* ---------- pur ---------- */

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

    /** Pur : template lang → lignes de lore (placeholders remplacés). */
    public static List<String> loreLines(Report report, List<String> template) {
        List<String> out = new ArrayList<>();
        for (String line : template) {
            out.add(line
                    .replace("%id%", shortId(report.getId()))
                    .replace("%reporter%", safe(report.getReporter()))
                    .replace("%reported%", safe(report.getReported()))
                    .replace("%reason%", safe(report.getReason()))
                    .replace("%server%", safe(report.getServerName()))
                    .replace("%date%", new SimpleDateFormat("dd/MM HH:mm").format(new Date(report.getCreatedAt())))
                    .replace("%status%", statusGlyph(report.getStatus()))
                    .replace("%handler%", report.getHandler() == null ? "—" : report.getHandler()));
        }
        return out;
    }

    /** Pur : UUID raccourci pour l'affichage (les recherches restent par préfixe). */
    public static String shortId(String id) {
        return id == null || id.length() <= 8 ? String.valueOf(id) : id.substring(0, 8);
    }

    private static String safe(String s) {
        return s == null ? "?" : s;
    }

    /* ---------- GUI ---------- */

    @Override
    public String getPrePaginatedTitle(Player p0) {
        String scope = Guard.getLangConfig().getString(
                onlyMine ? "menu.reports.scope.mine" : "menu.reports.scope.all", onlyMine ? "mes" : "tous");
        String suffix = " &8(&7" + filterLabel() + " &8| &7" + scope
                + " &8| &7" + getPage() + "/" + getPages(p0) + "&8)";
        return ChatUtil.translate(Guard.getLangConfig().getString("menu.reports.title", "&2Reports") + suffix);
    }

    private String filterLabel() {
        return filter == null
                ? Guard.getLangConfig().getString("menu.reports.filter.all", "TOUS")
                : filter;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player p0) {
        HashMap<Integer, Button> buttons = new HashMap<>();
        String onlyMineUuid = onlyMine ? p0.getUniqueId().toString() : null;
        for (Report report : ReportFilters.apply(ReportCache.allSnapshot(), filter, sort, onlyMineUuid, query)) {
            buttons.put(buttons.size(), new ReportButton(report));
        }
        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        // filtre : cycle OPEN → IN_PROGRESS → CLOSED → DELETED
        buttons.put(3, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                return new ItemBuilder(XMaterial.HOPPER.parseItem())
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.reports.filter.name", "&bFiltre : &f%filter%")
                                .replace("%filter%", filterLabel())))
                        .addLoreLine(ChatUtil.translate("&8> &eClique pour changer."))
                        .toItemStack();
            }

            @Override
            public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                new ReportsMenu(nextFilter(), sort, onlyMine, query).openMenu(p);
            }
        });

        // tri : NEWEST → OLDEST → OPEN_FIRST
        buttons.put(5, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                return new ItemBuilder(XMaterial.COMPARATOR.parseItem())
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.reports.sort.name", "&bTri : &f%sort%")
                                .replace("%sort%", sort)))
                        .addLoreLine(ChatUtil.translate("&8> &eClique pour changer."))
                        .toItemStack();
            }

            @Override
            public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                new ReportsMenu(filter, nextSort(), onlyMine, query).openMenu(p);
            }
        });

        // toggle « seulement mes rapports »
        buttons.put(6, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                String state = onlyMine ? "ON" : "OFF";
                return new ItemBuilder(XMaterial.NAME_TAG.parseItem())
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.reports.onlymine.name", "&bMes rapports : &f%state%")
                                .replace("%state%", state)))
                        .toItemStack();
            }

            @Override
            public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                new ReportsMenu(filter, sort, !onlyMine, query).openMenu(p);
            }
        });
        return buttons;
    }

    private String nextFilter() {
        if (Report.STATUS_OPEN.equals(filter)) {
            return Report.STATUS_IN_PROGRESS;
        }
        if (Report.STATUS_IN_PROGRESS.equals(filter)) {
            return Report.STATUS_CLOSED;
        }
        if (Report.STATUS_CLOSED.equals(filter)) {
            return Report.STATUS_DELETED;
        }
        return Report.STATUS_OPEN;
    }

    private String nextSort() {
        if (ReportFilters.SORT_NEWEST.equals(sort)) {
            return ReportFilters.SORT_OLDEST;
        }
        if (ReportFilters.SORT_OLDEST.equals(sort)) {
            return ReportFilters.SORT_OPEN_FIRST;
        }
        return ReportFilters.SORT_NEWEST;
    }

    @Override
    public Menu backButton() {
        return new DashboardMenu();
    }

    @Override
    public void onOpen(Player player) {
        ReportCache.refreshAsync(); // prépare la prochaine ouverture
    }

    /** Entrée de la file : tête du signalé + lore lang. Clic → profil si connecté, sinon détail. */
    public class ReportButton extends Button {
        private final Report report;

        public ReportButton(Report report) {
            this.report = report;
        }

        @Override
        public ItemStack getButtonItem(Player p0) {
            ItemBuilder item = new ItemBuilder(Heads.skullOf(report.getReported()))
                    .setName(ChatUtil.translate(Guard.getLangConfig()
                            .getString("menu.reports.item.name", "&f" + report.getReported())
                            .replace("%id%", shortId(report.getId()))
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
            // toujours l'écran de détail : c'est là que se fait le traitement
            // (le tp vers le signalé est sur le bouton "accepter")
            new ReportDetailMenu(report, new ReportsMenu(filter, sort, onlyMine, query)).openMenu(p0);
        }
    }
}
