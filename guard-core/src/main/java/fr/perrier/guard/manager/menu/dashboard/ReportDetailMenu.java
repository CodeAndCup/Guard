package fr.perrier.guard.manager.menu.dashboard;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.menu.dashboard.ReportsMenu;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.GlassMenu;
import fr.perrier.guard.menu.Menu;
import fr.perrier.guard.menu.buttons.BackButton;
import fr.perrier.guard.reports.Report;
import fr.perrier.guard.reports.ReportCache;
import fr.perrier.guard.reports.ReportLocation;
import fr.perrier.guard.reports.ReportStores;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.Heads;
import fr.perrier.guard.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Détail d'un signalement : récap, changement de statut, assignation d'un responsable,
 * téléportation à la position sauvegardée, et terminaison accepter/rejeter (→ CLOSED + verdict).
 */
public class ReportDetailMenu extends GlassMenu {

    private final Report report;
    private final Menu back;
    private boolean decided;

    public ReportDetailMenu(Report report, Menu back) {
        this.report = report;
        this.back = back;
    }

    @Override
    public String getTitle(Player player) {
        return ChatUtil.truncateTitle(ChatUtil.translate(Guard.getLangConfig()
                .getString("menu.reports.detail.title", "&2Report #%id%")
                .replace("%id%", ReportsMenu.shortId(report.getId()))
                .replace("%reported%", report.getReported() == null ? "?" : report.getReported())), 32);
    }

    @Override
    public int getGlassColor() {
        return 9;
    }

    @Override
    public Map<Integer, Button> getAllButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        // récap (tête du signalé)
        buttons.put(13, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                ItemBuilder item = new ItemBuilder(Heads.skullOf(report.getReported()))
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.reports.detail.info.name", "&f%reported%")
                                .replace("%reported%", String.valueOf(report.getReported()))
                                .replace("%reporter%", String.valueOf(report.getReporter()))));
                for (String line : ReportsMenu.loreLines(report, template())) {
                    item.addLoreLine(ChatUtil.translate(line));
                }
                return item.toItemStack();
            }
        });

        // statut (cycle : IN_PROGRESS ← → CLOSED)
        buttons.put(11, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                return new ItemBuilder(XMaterial.PAINTING.parseItem())
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.reports.detail.status.name", "&bStatut &f: %status%")
                                .replace("%status%", String.valueOf(report.getStatus()))))
                        .addLoreLine(ChatUtil.translate("&8> &ePrendre en charge (IN_PROGRESS)"))
                        .toItemStack();
            }

            @Override
            public void clicked(Player p, int i, ClickType clickType, int hotbarButton) {
                setStatus(p, Report.STATUS_IN_PROGRESS);
            }
        });

        // responsable : le staff qui agit s'attribue le rapport
        buttons.put(15, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                String handler = report.getHandler() == null ? "—" : report.getHandler();
                return new ItemBuilder(XMaterial.NAME_TAG.parseItem())
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.reports.detail.handler.name", "&bResponsable &f: %handler%")
                                .replace("%handler%", handler)))
                        .addLoreLine(ChatUtil.translate("&8> &eM'attribuer ce rapport"))
                        .toItemStack();
            }

            @Override
            public void clicked(Player p, int i, ClickType clickType, int hotbarButton) {
                ReportStores.get().setHandler(report.getId(), p.getName(), p.getUniqueId().toString())
                        .whenComplete((nil, throwable) -> afterWrite(p, throwable));
            }
        });

        // tp à la position sauvegardée (bouton absent s'il n'y a pas de position utilisable)
        ReportLocation location = ReportLocation.of(report);
        if (location != null) {
            buttons.put(17, new Button() {
                @Override
                public ItemStack getButtonItem(Player p) {
                    boolean usable = p.getWorld().getName().equals(location.world());
                    return new ItemBuilder((usable ? XMaterial.ENDER_PEARL : XMaterial.GRAY_DYE).parseItem())
                            .setName(ChatUtil.translate(Guard.getLangConfig()
                                    .getString("menu.reports.detail.teleport.name", "&bSe téléporter")))
                            .addLoreLine(ChatUtil.translate(Guard.getLangConfig()
                                    .getString("menu.reports.detail.teleport.lore", "&7Position : %world% %x% %y% %z%")
                                    .replace("%world%", location.world())
                                    .replace("%x%", String.valueOf((int) location.x()))
                                    .replace("%y%", String.valueOf((int) location.y()))
                                    .replace("%z%", String.valueOf((int) location.z()))))
                            .addLoreLine(ChatUtil.translate(usable
                                    ? "&8> &eClique pour y aller"
                                    : "&cMonde différent du vôtre"))
                            .toItemStack();
                }

                @Override
                public void clicked(Player p, int i, ClickType clickType, int hotbarButton) {
                    Location target = location.toLocation(p.getWorld().getName());
                    if (target == null) {
                        p.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.reports.location-unavailable", "&cPosition indisponible (monde déchargé ?)"))));
                        return;
                    }
                    p.closeInventory();
                    p.teleport(target);
                }
            });
        }

        // terminaison : accepter (tp au signalé si connecté) / rejeter
        buttons.put(21, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                return actionButton(XMaterial.LIME_DYE, "accept");
            }

            @Override
            public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                if (decided) {
                    return; // spam : un seul coup
                }
                decided = true;
                p.closeInventory();
                String reported = report.getReported();
                if (reported != null) {
                    Player target = Bukkit.getPlayerExact(reported);
                    if (target != null) {
                        p.teleport(target.getLocation());
                    }
                }
                resolve(p, Report.OUTCOME_ACCEPTED, "menu.reports.accepted");
            }
        });
        buttons.put(23, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                return actionButton(XMaterial.RED_DYE, "reject");
            }

            @Override
            public void clicked(Player p, int slot, ClickType clickType, int hotbarButton) {
                if (decided) {
                    return;
                }
                decided = true;
                p.closeInventory();
                resolve(p, Report.OUTCOME_REJECTED, "menu.reports.rejected");
            }
        });

        // back hors des slots de bordure ({...,25,26})
        buttons.put(24, new BackButton(back));
        return buttons;
    }

    private List<String> template() {
        List<String> t = Guard.getLangConfig().getStringList("menu.reports.item.lore");
        return t.isEmpty() ? Collections.singletonList("&7%reason%") : t;
    }

    private ItemStack actionButton(XMaterial material, String key) {
        ItemBuilder item = new ItemBuilder(material.parseItem())
                .setName(ChatUtil.translate(Guard.getLangConfig()
                        .getString("menu.reports.detail." + key + ".name", key)));
        for (String lore : Guard.getLangConfig().getStringList("menu.reports.detail." + key + ".lore")) {
            item.addLoreLine(ChatUtil.translate(lore));
        }
        return item.toItemStack();
    }

    private void setStatus(Player staff, String status) {
        ReportStores.get().setStatus(report.getId(), status, staff.getName())
                .whenComplete((nil, throwable) -> afterWrite(staff, throwable));
    }

    private void resolve(Player staff, String outcome, String langKey) {
        ReportStores.get().resolve(report.getId(), staff.getName(), outcome)
                .whenComplete((nil, throwable) -> {
                    if (throwable == null) {
                        ReportCache.refreshAsync(); // rapports résolus : disparaissent de la file
                    }
                    Bukkit.getScheduler().runTask(Guard.getInstance(), () -> {
                        if (throwable != null) {
                            // le staff doit savoir si le resolve a échoué (sinon il croit avoir agi)
                            staff.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                                    .getString("menu.reports.resolve-failed", "&cÉchec du resolve (store ?), réessayez."))));
                            throwable.printStackTrace();
                            return;
                        }
                        staff.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                                .getString(langKey, "&aFait.")
                                .replace("%id%", ReportsMenu.shortId(report.getId())))));
                    });
                });
    }

    /** Après une écriture (statut/responsable) : rafraîchit la file et rouvre le détail à jour. */
    private void afterWrite(Player staff, java.lang.Throwable throwable) {
        Bukkit.getScheduler().runTask(Guard.getInstance(), () -> {
            if (throwable != null) {
                staff.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                        .getString("menu.reports.write-failed", "&cÉcriture impossible (store ?)."))));
                throwable.printStackTrace();
                return;
            }
            ReportCache.refreshAsync();
            new ReportDetailMenu(refreshed(), back).openMenu(staff);
        });
    }

    /** Relit le rapport depuis le cache (état à jour après l'écriture). */
    private Report refreshed() {
        for (Report r : ReportCache.allSnapshot()) {
            if (r.getId().equals(report.getId())) {
                return r;
            }
        }
        return report;
    }
}
