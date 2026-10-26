package fr.perrier.guard.manager.menu.reports;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.commands.Report;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.GlassMenu;
import fr.perrier.guard.menu.buttons.BackButton;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Confirmation avant soumission : récap (cible + raison) puis validation.
 * Envoie via {@link Report#submit} — le même point unique que la commande.
 */
public class ReportConfirmMenu extends GlassMenu {

    private final Player reporter;
    private final String target;
    private final String reason;
    private final String reasonLabel;

    public ReportConfirmMenu(Player reporter, String target, String reason, String reasonLabel) {
        this.reporter = reporter;
        this.target = target;
        this.reason = reason;
        this.reasonLabel = reasonLabel;
    }

    @Override
    public String getTitle(Player player) {
        return ChatUtil.translate(Guard.getLangConfig()
                .getString("menu.reports.reason.confirm.title", "&cSignaler &f%target% &cpour &b%reason%&c?")
                .replace("%target%", target)
                .replace("%reason%", reasonLabel));
    }

    @Override
    public int getGlassColor() {
        return 9;
    }

    @Override
    public Map<Integer, Button> getAllButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(13, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                ItemBuilder item = new ItemBuilder(fr.perrier.guard.utils.Heads.skullOf(target))
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.reports.reason.confirm.info.name", "&f%target%")
                                .replace("%target%", target)));
                for (String lore : Guard.getLangConfig().getStringList("menu.reports.reason.confirm.info.lore")) {
                    item.addLoreLine(ChatUtil.translate(lore
                            .replace("%target%", target)
                            .replace("%reason%", reasonLabel)));
                }
                return item.toItemStack();
            }
        });

        buttons.put(21, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                return new ItemBuilder(XMaterial.LIME_WOOL.parseItem())
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.reports.reason.confirm.yes.name", "&aConfirmer")))
                        .toItemStack();
            }

            @Override
            public void clicked(Player p, int i, ClickType clickType, int hotbarButton) {
                Button.playNeutral(p);
                p.closeInventory();
                Player targetPlayer = Bukkit.getPlayerExact(target);
                if (targetPlayer == null) {
                    p.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                            .getString("commands.report.usage", "&c/report <joueur> <raison>"))));
                    return;
                }
                Report.submit(p, targetPlayer, reasonLabel);
            }
        });

        buttons.put(23, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                return new ItemBuilder(XMaterial.RED_WOOL.parseItem())
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.reports.reason.confirm.no.name", "&cAnnuler")))
                        .toItemStack();
            }

            @Override
            public void clicked(Player p, int i, ClickType clickType, int hotbarButton) {
                Button.playNeutral(p);
                p.closeInventory();
            }
        });

        buttons.put(24, new BackButton(new ReportReasonMenu(reporter, target)));
        return buttons;
    }
}
