package fr.perrier.guard.manager.menu.dashboard;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.GlassMenu;
import fr.perrier.guard.menu.Menu;
import fr.perrier.guard.menu.buttons.BackButton;
import fr.perrier.guard.reports.Report;
import fr.perrier.guard.reports.ReportStores;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Détail d'un report : récap + Accepter (tp au signalé + ACCEPTED) / Rejeter (REJECTED). Boutons à coup unique. */
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
        return ChatUtil.translate(Guard.getLangConfig()
                .getString("menu.reports.detail.title", "&2Report #%id%")
                .replace("%id%", String.valueOf(report.getId()))
                .replace("%reported%", report.getReported() == null ? "?" : report.getReported()));
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
                ItemBuilder item = new ItemBuilder(XMaterial.PLAYER_HEAD.parseItem())
                        .setSkullOwner(report.getReported());
                for (String line : ReportsMenu.loreLines(report, guardTemplate())) {
                    item.addLoreLine(ChatUtil.translate(line));
                }
                return item.toItemStack();
            }
        });

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
                resolve(p, Report.STATUS_ACCEPTED, "menu.reports.accepted");
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
                resolve(p, Report.STATUS_REJECTED, "menu.reports.rejected");
            }
        });

        // back hors des slots de bordure ({...,25,26})
        buttons.put(24, new BackButton(back));
        return buttons;
    }

    private List<String> guardTemplate() {
        List<String> template = Guard.getLangConfig().getStringList("menu.reports.item.lore");
        return template.isEmpty() ? java.util.Collections.singletonList("&7%reason%") : template;
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

    private void resolve(Player staff, String outcome, String langKey) {
        ReportStores.get().resolve(report.getId(), staff.getName(), outcome).whenComplete((nil, throwable) ->
                Bukkit.getScheduler().runTask(Guard.getInstance(), () -> {
                    if (throwable != null) {
                        // le staff doit savoir si le resolve a échoué (sinon il croit avoir agi)
                        staff.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.reports.resolve-failed", "&cÉchec du resolve (serveur ?), réessayez."))));
                        throwable.printStackTrace();
                        return;
                    }
                    staff.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                            .getString(langKey, "&aFait.")
                            .replace("%id%", String.valueOf(report.getId())))));
                }));
    }
}
