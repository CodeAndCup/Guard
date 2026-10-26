package fr.perrier.guard.reports;

import fr.perrier.guard.Guard;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Cache memoire des rapports — TOUS les statuts (le filtrage appartient aux GUIs, pas au stockage).
 *
 * Pourquoi : les menus ouvraient un inventaire VIDE puis tentaient un re-render asynchrone
 * garde par un test fragile (le menu est-il encore celui ouvert ?). Des que ce garde echouait
 * une fois (fermeture, course, update), le menu restait vide — d'ou l'affichage intermittent.
 * A Present, les menus lisent ce cache de facon SYNCHRONE : aucun re-render, aucune course.
 *
 * Rafraichissement : au boot, apres chaque ecriture (submit/resolve/setStatus/setHandler/clear),
 * et periodiquement (filet de securite).
 */
public final class ReportCache {

    private static final List<Report> ALL = new CopyOnWriteArrayList<>();
    private static BukkitTask refreshTask;

    private ReportCache() {
    }

    /** Instantane synchrone (jamais null) de tous les statuts. */
    public static List<Report> allSnapshot() {
        return new ArrayList<>(ALL);
    }

    /** Compat : seuls les rapports ouverts (anciens appelants). */
    public static List<Report> openSnapshot() {
        List<Report> out = new ArrayList<>();
        for (Report r : ALL) {
            if (Report.STATUS_OPEN.equals(r.getStatus())) {
                out.add(r);
            }
        }
        return out;
    }

    /** Relit le store en asynchrone ; a appeler apres toute ecriture. */
    public static void refreshAsync() {
        ReportStores.get().listAll().whenComplete((list, throwable) -> Bukkit.getScheduler()
                .runTask(Guard.getInstance(), () -> {
                    if (throwable != null) {
                        Guard.getInstance().getLogger().warning("[Reports] cache non rafraichi: " + throwable);
                        return;
                    }
                    List<Report> sorted = new ArrayList<>(list == null ? new ArrayList<>() : list);
                    sorted.sort(Comparator.comparingLong(Report::getCreatedAt).reversed());
                    ALL.clear();
                    ALL.addAll(sorted);
                }));
    }

    /** Filet de securite : si une ecriture echappe a un appel explicite. */
    public static void startPeriodicRefresh() {
        if (refreshTask != null) {
            return;
        }
        refreshTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Guard.getInstance(),
                ReportCache::refreshAsync, 200L, 200L); // toutes les 10 s
    }
}
