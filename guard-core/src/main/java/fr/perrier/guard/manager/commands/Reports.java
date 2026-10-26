package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.commands.annotations.Command;
import fr.perrier.guard.commands.annotations.Param;
import fr.perrier.guard.manager.menu.dashboard.ReportsMenu;
import fr.perrier.guard.reports.Report;
import fr.perrier.guard.reports.ReportCache;
import fr.perrier.guard.reports.ReportNotifications;
import fr.perrier.guard.reports.ReportStores;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Sous-commandes staff des signalements :
 * /reports [filtre]  — ouvre la file (filtre actif si fourni)
 * /reports search user|id|reason|handler <valeur>  — pose un filtre sur la file
 * /reports sethandler <id> <joueur|none>
 * /reports on|off   — toggle notifications
 * /reports clear    — nettoie les rapports clos au-delà de la rétention
 */
public class Reports {

    /** Sentinelle = paramètre non fourni (le framework ne différencie pas "" vide de "absent"). */
    private static final String ABSENT = "\u00A7";

    @Command(names = CommandsUtils.REPORTS, perm = "guard.reports.view")
    public static void onReports(Player player,
                                 @Param(name = "action", baseValue = ABSENT) String action,
                                 @Param(name = "value", baseValue = "") String value,
                                 @Param(name = "value2", baseValue = "") String value2) {
        String a = action.toLowerCase(Locale.ROOT);
        switch (a) {
            case ABSENT:
            case "list":
                new ReportsMenu().openMenu(player);
                break;
            case "search":
                search(player, value, value2);
                break;
            case "sethandler":
                if (!player.hasPermission("guard.reports.manage")) {
                    noPerm(player);
                    return;
                }
                setHandler(player, value, value2);
                break;
            case "on":
            case "off":
                if (!player.hasPermission("guard.reports.notify")) {
                    noPerm(player);
                    return;
                }
                ReportNotifications.setEnabled(player.getUniqueId(), a.equals("on"));
                player.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                        .getString(a.equals("on") ? "reports.notify-on" : "reports.notify-off",
                                a.equals("on") ? "&aNotifications activées." : "&cNotifications désactivées."))));
                break;
            case "clear":
                if (!player.hasPermission("guard.reports.clear")) {
                    noPerm(player);
                    return;
                }
                clear(player);
                break;
            default:
                // action = un filtre de statut direct ("/reports OPEN")
                if (Report.STATUS_OPEN.equalsIgnoreCase(a) || Report.STATUS_IN_PROGRESS.equalsIgnoreCase(a)
                        || Report.STATUS_CLOSED.equalsIgnoreCase(a) || Report.STATUS_DELETED.equalsIgnoreCase(a)) {
                    new ReportsMenu(a.toUpperCase(Locale.ROOT), fr.perrier.guard.reports.ReportFilters.SORT_NEWEST,
                            false, null).openMenu(player);
                } else {
                    usage(player);
                }
                break;
        }
    }

    private static void search(Player player, String what, String value) {
        if (what.isEmpty() || value.isEmpty()) {
            usage(player);
            return;
        }
        String w = what.toLowerCase(Locale.ROOT);
        if (!w.equals("user") && !w.equals("id") && !w.equals("reason") && !w.equals("handler")) {
            usage(player);
            return;
        }
        // la recherche cible le champ demandé ; on la traduit en requête multi-champs
        // (la requête "user" couvre signalant ET signalé, "id" matche aussi l'UUID, cf. ReportFilters)
        // aucun filtre de statut : un rapport ancien/fermé doit rester trouvable
        new ReportsMenu(null, fr.perrier.guard.reports.ReportFilters.SORT_NEWEST, false, value)
                .openMenu(player);
        player.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                .getString("reports.search-set", "&fFiltre de recherche : &b%value%")
                .replace("%value%", value))));
    }

    private static void setHandler(Player player, String id, String name) {
        if (id.isEmpty() || name.isEmpty()) {
            player.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString("reports.usage-sethandler", "&c/reports sethandler <id> <joueur|none>"))));
            return;
        }
        // recherche par préfixe d'UUID (ce que la GUI affiche)
        String targetId = resolveShortId(id);
        if (targetId == null) {
            player.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString("reports.not-found", "&cRapport introuvable : %id%")
                    .replace("%id%", id))));
            return;
        }
        if (name.equalsIgnoreCase("none")) {
            ReportStores.get().setHandler(targetId, null, null)
                    .whenComplete((nil, t) -> reportOutcome(player, t, "reports.handler-cleared"));
            return;
        }
        Player handler = Bukkit.getPlayerExact(name);
        if (handler == null) {
            player.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString("reports.handler-offline", "&cCe modo n'est pas connecté."))));
            return;
        }
        ReportStores.get().setHandler(targetId, handler.getName(), handler.getUniqueId().toString())
                .whenComplete((nil, t) -> reportOutcome(player, t, "reports.handler-set"));
    }

    private static void clear(Player player) {
        int retention = Guard.getDefaultConfig().getInt("reports.retention-days", 30);
        ReportStores.get().clearClosed(retention).whenComplete((count, t) -> {
            if (t != null) {
                t.printStackTrace();
                player.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                        .getString("reports.clear-failed", "&cNettoyage impossible (store ?)."))));
                return;
            }
            ReportCache.refreshAsync();
            player.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString("reports.cleared", "&a%count% rapport(s) nettoyé(s).")
                    .replace("%count%", String.valueOf(count)))));
        });
    }

    private static void reportOutcome(Player player, Throwable t, String langKey) {
        if (t != null) {
            t.printStackTrace();
            player.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString("reports.write-failed", "&cÉcriture impossible (store ?)."))));
            return;
        }
        ReportCache.refreshAsync();
        player.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                .getString(langKey, "&aFait."))));
    }

    /** Préfixe d'UUID → id complet depuis le cache (la GUI n'affiche que 8 caractères). */
    private static String resolveShortId(String prefix) {
        for (Report r : ReportCache.allSnapshot()) {
            if (r.getId().startsWith(prefix)) {
                return r.getId();
            }
        }
        return prefix; // l'appelant a peut-être collé l'UUID complet
    }

    private static void noPerm(Player player) {
        player.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                .getString("reports.no-perm", "&cPermission insuffisante."))));
    }

    private static void usage(Player player) {
        player.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                .getString("reports.usage", "&c/reports | search <user|id|reason|handler> <v> | sethandler <id> <joueur|none> | on|off | clear"))));
    }
}
