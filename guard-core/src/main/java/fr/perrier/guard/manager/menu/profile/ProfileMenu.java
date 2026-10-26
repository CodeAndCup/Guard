package fr.perrier.guard.manager.menu.profile;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.commands.Cps;
import fr.perrier.guard.manager.listener.PluginMessage;
import fr.perrier.guard.manager.menu.dashboard.ReportsMenu;
import fr.perrier.guard.manager.menu.moderation.ViewInventoryMenu;
import fr.perrier.guard.manager.menu.sanctionset.SanctionScreen;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.GlassMenu;
import fr.perrier.guard.menu.Menu;
import fr.perrier.guard.menu.buttons.BackButton;
import fr.perrier.guard.reports.Report;
import fr.perrier.guard.reports.ReportCache;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.Heads;
import fr.perrier.guard.utils.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Profil joueur à onglets. Onglet INFO rendu ici (tête/ping/adresse/brand/CPS) ;
 * SANCTIONS → SanctionScreen, INVENTORY → ViewInventoryMenu, HISTORY → reports de la cible.
 * Toute action re-vérifie que la cible est encore en ligne (menu.profile.offline sinon).
 */
public class ProfileMenu extends GlassMenu {

    public static final String TAB_SANCTIONS = "SANCTIONS";
    public static final String TAB_INVENTORY = "INVENTORY";
    public static final String TAB_HISTORY = "HISTORY";
    public static final String TAB_INFO = "INFO";

    /** Pur : ordre d'affichage des onglets, fixe. */
    public static List<String> tabs() {
        return Arrays.asList(TAB_SANCTIONS, TAB_INVENTORY, TAB_HISTORY, TAB_INFO);
    }

    /** Réflexion : l'API ping change selon la version (1.21 = Player#getPing ; 1.8 = Player.Spigot#getPing). */
    public static int ping(Player p) {
        try {
            return ((Number) p.getClass().getMethod("getPing").invoke(p)).intValue();
        } catch (Exception ignored) {
        }
        try {
            Object spigot = p.getClass().getMethod("spigot").invoke(p);
            return ((Number) spigot.getClass().getMethod("getPing").invoke(spigot)).intValue();
        } catch (Exception ignored) {
        }
        return -1;
    }

    /** Pur : couleur du ping (< 50 vert, <= 150 jaune, sinon rouge). */
    public static String pingColor(int ping) {
        if (ping < 50) {
            return "&a";
        }
        if (ping <= 150) {
            return "&e";
        }
        return "&c";
    }

    private final Player target;

    public ProfileMenu(Player target) {
        this.target = target;
    }

    @Override
    public String getTitle(Player player) {
        return ChatUtil.translate(Guard.getLangConfig()
                .getString("menu.profile.title", "&2Profil &f%target%")
                .replace("%target%", target.getName()));
    }

    @Override
    public int getGlassColor() {
        return 9; // cyan — palette B (avant : remplissage intégral en glass gris 15)
    }

    @Override
    public Map<Integer, Button> getAllButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        int slot = 2; // 1 et 7 sont des slots de bordure GlassMenu : les onglets commencent à 2
        for (final String tab : tabs()) {
            buttons.put(slot, new Button() {
                @Override
                public ItemStack getButtonItem(Player p) {
                    return new ItemBuilder(tabItem(tab))
                            .setName(ChatUtil.translate(Guard.getLangConfig()
                                    .getString("menu.profile.tab." + tab.toLowerCase(), tab)))
                            .toItemStack();
                }

                @Override
                public void clicked(Player p, int i, ClickType clickType, int hotbarButton) {
                    openTab(p, tab);
                }
            });
            slot++;
        }

        // onglet INFO : tête de la cible (ping, adresse, brand client) + session CPS
        buttons.put(13, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                int ping = ping(target);
                String pingText = ping < 0 ? "&7?" : pingColor(ping) + ping;
                return new ItemBuilder(Heads.skullOf(target.getName()))
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.profile.info.name", "&f%target%")
                                .replace("%target%", target.getName())))
                        .setLore(lore(pingText))
                        .toItemStack();
            }
        });
        buttons.put(21, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                Double left = Cps.saveG.get(target.getUniqueId());
                Double right = Cps.saveD.get(target.getUniqueId());
                return new ItemBuilder(XMaterial.CLOCK.parseItem())
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.profile.cps.name", "&fCPS")))
                        .addLoreLine(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.profile.cps.lore", "&f%left% / %right%")
                                .replace("%left%", left == null ? "?" : String.format("%.1f", left))
                                .replace("%right%", right == null ? "?" : String.format("%.1f", right))))
                        .toItemStack();
            }
        });

        // le cadre glass est posé par GlassMenu (pattern documenté de la lib)
        return buttons;
    }

    private List<String> lore(String pingText) {
        String brand = PluginMessage.brandOf(target);
        String address = target.getAddress() == null ? "?"
                : target.getAddress().getAddress().getHostAddress();
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String line : Guard.getLangConfig().getStringList("menu.profile.info.lore")) {
            out.add(ChatUtil.translate(line
                    .replace("%ping%", pingText)
                    .replace("%address%", address)
                    .replace("%brand%", brand)));
        }
        return out;
    }

    private void openTab(Player staff, String tab) {
        if (!target.isOnline()) {
            // la cible a pu se déconnecter entre l'ouverture du menu et le clic
            staff.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString("menu.profile.offline", "&cJoueur déconnecté."))));
            return;
        }
        Button.playNeutral(staff);
        switch (tab) {
            case TAB_SANCTIONS:
                new SanctionScreen(this, target).openMenu(staff);
                break;
            case TAB_INVENTORY:
                new ViewInventoryMenu(target).openMenu(staff);
                break;
            case TAB_HISTORY:
                new HistoryMenu().openMenu(staff);
                break;
            default:
                // INFO : déjà affiché ici
                break;
        }
    }

    private ItemStack tabItem(String tab) {
        switch (tab) {
            case TAB_SANCTIONS:
                return XMaterial.BOOK.parseItem();
            case TAB_INVENTORY:
                return XMaterial.CHEST.parseItem();
            case TAB_HISTORY:
                return XMaterial.CLOCK.parseItem();
            default:
                return XMaterial.PLAYER_HEAD.parseItem();
        }
    }

    /** Onglet Historique : reports OPEN signalant la cible (lecture synchrone du cache). */
    public class HistoryMenu extends GlassMenu {

        @Override
        public String getTitle(Player player) {
            return ChatUtil.translate(Guard.getLangConfig()
                    .getString("menu.profile.history.title", "&2Historique &f%target%")
                    .replace("%target%", target.getName()));
        }

        @Override
        public int getGlassColor() {
            return 9;
        }

        @Override
        public Map<Integer, Button> getAllButtons(Player player) {
            Map<Integer, Button> buttons = new HashMap<>();

            int slot = 10; // 9 = slot de bordure GlassMenu
            for (final Report report : ReportCache.openSnapshot()) {
                if (!target.getName().equalsIgnoreCase(String.valueOf(report.getReported()))) {
                    continue;
                }
                if (slot >= 17 && slot <= 19) {
                    slot = 20; // 17-19 sont des slots de bordure (size 27) : jamais un contenu dessus
                }
                final int s = slot++;
                buttons.put(s, new Button() {
                    @Override
                    public ItemStack getButtonItem(Player p) {
                        return new ItemBuilder(Heads.skullOf(report.getReported()))
                                .setName(ChatUtil.translate(Guard.getLangConfig()
                                        .getString("menu.reports.item.name", "&f%reported%")
                                        .replace("%id%", String.valueOf(report.getId()))
                                        .replace("%reported%", String.valueOf(report.getReported()))
                                        .replace("%reporter%", String.valueOf(report.getReporter()))))
                                .setLore(ReportsMenu.loreLines(report,
                                        Guard.getLangConfig().getStringList("menu.reports.item.lore")))
                                .toItemStack();
                    }
                });
            }

            // back hors des slots de bordure ({...,25,26})
            buttons.put(22, new BackButton(ProfileMenu.this));
            return buttons;
        }

        @Override
        public void onOpen(Player player) {
            ReportCache.refreshAsync();
        }
    }
}
