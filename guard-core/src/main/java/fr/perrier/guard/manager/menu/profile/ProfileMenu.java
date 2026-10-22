package fr.perrier.guard.manager.menu.profile;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.commands.Cps;
import fr.perrier.guard.manager.listener.PluginMessage;
import fr.perrier.guard.manager.menu.dashboard.ReportsMenu;
import fr.perrier.guard.manager.menu.moderation.ViewInventoryMenu;
import fr.perrier.guard.manager.menu.sanctionset.SanctionScreen;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.Menu;
import fr.perrier.guard.menu.buttons.BackButton;
import fr.perrier.guard.menu.buttons.Glass;
import fr.perrier.guard.reports.Report;
import fr.perrier.guard.reports.ReportStores;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Profil joueur à onglets. Onglet INFO rendu ici (tête/ping/adresse/brand/CPS) ;
 * SANCTIONS → SanctionScreen, INVENTORY → ViewInventoryMenu, HISTORY → sous-menu reports filtrés.
 * Toute action re-vérifie que la cible est encore en ligne (menu.profile.offline sinon).
 */
public class ProfileMenu extends Menu {

    public static final String TAB_SANCTIONS = "SANCTIONS";
    public static final String TAB_INVENTORY = "INVENTORY";
    public static final String TAB_HISTORY = "HISTORY";
    public static final String TAB_INFO = "INFO";

    /** Pur : ordre d'affichage des onglets, fixe. */
    public static List<String> tabs() {
        return Arrays.asList(TAB_SANCTIONS, TAB_INVENTORY, TAB_HISTORY, TAB_INFO);
    }

    /** Réflexion : l'API ping change selon la version (1.21 = Player#getPing ; 1.8 = Player.Spigot#getPing). */
    static int ping(Player p) {
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

    /** Pur : couleur du ping (< 50 vert, < 150 jaune, sinon rouge). */
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
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        int slot = 1;
        for (final String tab : tabs()) {
            final int s = slot;
            buttons.put(s, new Button() {
                @Override
                public ItemStack getButtonItem(Player p) {
                    ItemBuilder item = new ItemBuilder(tabItem(tab))
                            .setName(ChatUtil.translate(Guard.getLangConfig()
                                    .getString("menu.profile.tab." + tab.toLowerCase(), tab)));
                    return item.toItemStack();
                }

                @Override
                public void clicked(Player p, int i, ClickType clickType, int hotbarButton) {
                    openTab(p, tab);
                }
            });
            slot += 2;
        }

        // onglet INFO : tête de la cible (ping, adresse, brand client) + session CPS
        buttons.put(13, new Button() {
            @Override
            public ItemStack getButtonItem(Player p) {
                ItemBuilder item = new ItemBuilder(XMaterial.PLAYER_HEAD.parseItem())
                        .setSkullOwner(target.getName())
                        .setName(ChatUtil.translate(Guard.getLangConfig()
                                .getString("menu.profile.info.name", "&f" + target.getName())));
                int ping = ping(target);
                String brand = PluginMessage.brandOf(target);
                String pingText = ping < 0 ? "&7?" : pingColor(ping) + String.valueOf(ping);
                for (String line : Guard.getLangConfig().getStringList("menu.profile.info.lore")) {
                    item.addLoreLine(ChatUtil.translate(line
                            .replace("%ping%", pingText)
                            .replace("%address%", target.getAddress() == null ? "?" : target.getAddress().getAddress().getHostAddress())
                            .replace("%brand%", brand)));
                }
                return item.toItemStack();
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

        for (int i = 0; i < getSize(); i++) {
            if (!buttons.containsKey(i)) {
                buttons.put(i, new Glass(15));
            }
        }
        return buttons;
    }

    @Override
    public int getSize() {
        return 27;
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

    /** Onget Historique : reports OPEN signalant la cible (source = store, pas de DB v1) + rappel CPS en tête. */
    public class HistoryMenu extends Menu {

        private List<Report> reports = new ArrayList<>();
        private String lastIds = "";

        @Override
        public String getTitle(Player player) {
            return ChatUtil.translate(Guard.getLangConfig()
                    .getString("menu.profile.history.title", "&2Historique &f%target%")
                    .replace("%target%", target.getName()));
        }

        @Override
        public Map<Integer, Button> getButtons(Player player) {
            Map<Integer, Button> buttons = new HashMap<>();

            int slot = 9;
            for (final Report report : reports) {
                if (slot >= getSize()) {
                    break;
                }
                final int s = slot++;
                buttons.put(s, new Button() {
                    @Override
                    public ItemStack getButtonItem(Player p) {
                        ItemBuilder item = new ItemBuilder(XMaterial.PAPER.parseItem())
                                .setName(ChatUtil.translate(Guard.getLangConfig()
                                        .getString("menu.reports.item.name", "&f%reported%")
                                        .replace("%id%", String.valueOf(report.getId()))
                                        .replace("%reported%", String.valueOf(report.getReported()))
                                        .replace("%reporter%", String.valueOf(report.getReporter()))));
                        List<String> template = Guard.getLangConfig().getStringList("menu.reports.item.lore");
                        if (template.isEmpty()) {
                            item.addLoreLine(ChatUtil.translate("&7" + report.getReason()));
                        } else {
                            for (String line : ReportsMenu.loreLines(report, template)) {
                                item.addLoreLine(ChatUtil.translate(line));
                            }
                        }
                        return item.toItemStack();
                    }
                });
            }

            buttons.put(26, new BackButton(ProfileMenu.this));
            for (int i = 0; i < getSize(); i++) {
                if (!buttons.containsKey(i)) {
                    buttons.put(i, new Glass(15));
                }
            }
            return buttons;
        }

        @Override
        public int getSize() {
            return 27;
        }

        @Override
        public void onOpen(Player player) {
            ReportStores.get().listOpen().thenAccept(list -> Bukkit.getScheduler().runTask(Guard.getInstance(), () -> {
                String ids = filterIds(list);
                if (ids.equals(lastIds)) {
                    return; // rien de neuf : pas de re-rendu (évite la boucle onOpen → load)
                }
                lastIds = ids;
                reports = new ArrayList<>();
                for (Report report : list) {
                    if (target.getName().equalsIgnoreCase(report.getReported())) {
                        reports.add(report);
                    }
                }
                openMenu(player);
            }));
        }

        private String filterIds(List<Report> list) {
            StringBuilder sb = new StringBuilder();
            for (Report r : list) {
                if (target.getName().equalsIgnoreCase(r.getReported())) {
                    sb.append(r.getId()).append(',');
                }
            }
            return sb.toString();
        }
    }
}
