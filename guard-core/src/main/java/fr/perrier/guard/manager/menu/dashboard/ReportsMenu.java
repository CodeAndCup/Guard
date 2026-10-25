package fr.perrier.guard.manager.menu.dashboard;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.Menu;
import fr.perrier.guard.menu.pagination.PaginatedMenu;
import fr.perrier.guard.reports.Report;
import fr.perrier.guard.reports.ReportStores;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.ItemBuilder;
import org.bukkit.Bukkit;
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
 * Tuile Reports : file des reports OPEN (store relu à chaque ouverture — source de vérité).
 * Chargement ASYNC puis re-rendu main thread ; 0 report → écran vide lang menu.reports.empty.
 */
public class ReportsMenu extends PaginatedMenu {

    private List<Report> reports = new ArrayList<>();
    private String lastIds = "";
    private boolean storeError;

    /** Pur : glyphe du statut (deterministe, inconnu → "?"). */
    public static String statusGlyph(String status) {
        if (Report.STATUS_OPEN.equals(status)) {
            return "●";
        }
        if (Report.STATUS_ACCEPTED.equals(status)) {
            return "✔";
        }
        if (Report.STATUS_REJECTED.equals(status)) {
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
        for (Report report : reports) {
            buttons.put(buttons.size(), new ReportButton(report));
        }
        return buttons;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        if (!reports.isEmpty()) {
            return null;
        }
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(22, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                String key = storeError ? "menu.reports.store-error" : "menu.reports.empty";
                String def = storeError ? "&cStore injoignable (console)" : "&fAucun report";
                return new ItemBuilder(XMaterial.PAPER.parseItem())
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString(key, def)))
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
        ReportStores.get().listOpen().whenComplete((list, throwable) -> Bukkit.getScheduler().runTask(Guard.getInstance(), () -> {
            if (!player.isOnline() || Menu.getCurrentlyOpenedMenus().get(player.getName()) != this) {
                return; // menu fermé entre-temps : ne pas rouvrir à la décharge du async
            }
            if (throwable != null) {
                // store HS : on DIT que le store est en erreur (sinon l'écran vide est indistinguable d'un 0 report)
                throwable.printStackTrace();
                if (!storeError) {
                    storeError = true;
                    openMenu(player); // un seul re-rendu par erreur (sinon boucle onOpen → load)
                }
                return;
            }
            storeError = false;
            if (list == null || list.isEmpty()) {
                lastIds = "";
                reports = new ArrayList<>();
                return;
            }
            String ids = idsOf(list);
            if (ids.equals(lastIds)) {
                return; // rien de neuf : pas de re-rendu (évite la boucle onOpen → load)
            }
            lastIds = ids;
            reports = list;
            openMenu(player); // re-rendu (chemin update du Menu si même titre/taille)
        }));
    }

    private String idsOf(List<Report> list) {
        StringBuilder sb = new StringBuilder();
        for (Report r : list) {
            sb.append(r.getId()).append(',');
        }
        return sb.toString();
    }

    /** Bouton d'un report : tête du joueur signalé + lore lang ; clic → écran de détail. */
    public static class ReportButton extends Button {
        private final Report report;

        public ReportButton(Report report) {
            this.report = report;
        }

        @Override
        public ItemStack getButtonItem(Player p0) {
            ItemBuilder item = new ItemBuilder(XMaterial.PLAYER_HEAD.parseItem())
                    .setSkullOwner(report.getReported())
                    .setName(ChatUtil.translate(Guard.getLangConfig()
                            .getString("menu.reports.item.name", "&f" + report.getReported())
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
