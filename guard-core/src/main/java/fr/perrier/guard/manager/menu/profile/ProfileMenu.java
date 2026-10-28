package fr.perrier.guard.manager.menu.profile;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.cps.CpsService;
import fr.perrier.guard.manager.cps.CpsTracker;
import fr.perrier.guard.manager.listener.PluginMessage;
import fr.perrier.guard.manager.menu.dashboard.PlayersMenu;
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

import java.util.*;

/**
 * Profil joueur : tête au centre (ping/adresse/brand), 3 onglets centrés en haut,
 * CPS centré en bas. SANCTIONS → SanctionScreen, INVENTORY → ViewInventoryMenu,
 * HISTORY → reports de la cible. L'onglet INFO est retiré : son « clic » ne faisait
 * rien (l'info est déjà rendue par la tête du centre) — c'était un bouton mort.
 * Toute action re-vérifie que la cible est encore en ligne (menu.profile.offline sinon).
 */
public class ProfileMenu extends GlassMenu {

    public static final String TAB_SANCTIONS = "SANCTIONS";
    public static final String TAB_INVENTORY = "INVENTORY";
    public static final String TAB_HISTORY = "HISTORY";

    /** Pur : ordre d'affichage des onglets, fixe. */
    public static List<String> tabs() {
        return Arrays.asList(TAB_SANCTIONS, TAB_INVENTORY, TAB_HISTORY);
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

    /**
     * Version du client : ViaVersion si présent (réflexion, dépendance optionnelle),
     * sinon la version du serveur (sans ViaVersion un client ne peut pas être d'une autre version).
     */
    public static String clientVersion(Player p) {
        Integer protocol = viaProtocol(p.getUniqueId());
        if (protocol != null) {
            return fr.perrier.guard.utils.ProtocolVersion.of(protocol);
        }
        return serverVersion();
    }

    /** Réflexion ViaVersion : protocol du joueur, ou null si le plugin est absent. */
    private static Integer viaProtocol(java.util.UUID uuid) {
        try {
            Class<?> via = Class.forName("com.viaversion.viaversion.api.Via");
            Object api = via.getMethod("getAPI").invoke(null);
            Object version = api.getClass().getMethod("getPlayerVersion", java.util.UUID.class)
                    .invoke(api, uuid);
            return ((Number) version).intValue();
        } catch (Throwable absent) {
            return null; // plugin non installé (Throwable : NoClassDefFoundError inclus)
        }
    }

    /** Version du serveur "1.x.y", ou "?" si indéterminable. */
    private static String serverVersion() {
        try {
            String bukkit = org.bukkit.Bukkit.getBukkitVersion(); // "1.21.5-R0.1-SNAPSHOT"
            int dash = bukkit.indexOf('-');
            return dash > 0 ? bukkit.substring(0, dash) : bukkit;
        } catch (Throwable ignored) {
            return "?";
        }
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

    /** 27 slots : bordure GlassMenu {0,1,7,8,9,17,18,19,25,26} ; utile = 2-6 / 10-16 / 20-24. */
    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public Map<Integer, Button> getAllButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        int slot = 3; // onglets centrés en rangée 1 (utile 2-6 : 3,4,5)
        for (final String tab : tabs()) {
            buttons.put(slot, new Button() {
                @Override
                public ItemStack getButtonItem(Player p) {
                    return new ItemBuilder(tabItem(tab, p))
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

        // le cadre glass est posé par GlassMenu (pattern documenté de la lib)

        // back vers la liste des joueurs (seul écran d'ouverture du profil), hors bordure (2-6)
        buttons.put(20, new BackButton(new PlayersMenu()));

        return buttons;
    }

    private List<String> lore(String pingText) {
        String brand = PluginMessage.brandOf(target);
        String version = clientVersion(target);
        String address = target.getAddress() == null ? "?"
                : target.getAddress().getAddress().getHostAddress();
        List<String> out = new ArrayList<>();

        CpsTracker flux = CpsService.peek(target.getUniqueId());
        Double left = flux == null ? null : flux.leftCps();
        Double right = flux == null ? null : flux.rightCps();

        for (String line : Guard.getLangConfig().getStringList("menu.profile.info.lore")) {
            out.add(ChatUtil.translate(line
                    .replace("%ping%", pingText)
                    .replace("%address%", address)
                    .replace("%brand%", brand)
                    .replace("%version%", version))
                    .replace("%left%", left == null ? "?" : String.format("%.1f", left))
                    .replace("%right%", right == null ? "?" : String.format("%.1f", right))
            );
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

    private ItemStack tabItem(String tab, Player p) {
        switch (tab) {
            case TAB_SANCTIONS:
                return Heads.textured("profile_sanctions");
            case TAB_INVENTORY:
                return Heads.textured("profile_inventory");
            case TAB_HISTORY:
                return Heads.textured("profile_history");
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
                                        .replace("%reporter%", String.valueOf(report.getReporter()))

                                ))
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
