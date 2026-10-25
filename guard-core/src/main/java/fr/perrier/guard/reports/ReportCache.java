package fr.perrier.guard.reports;

import fr.perrier.guard.Guard;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Cache memoire des reports ouverts (source de verite = store, mais lu en ASYNC).
 *
 * Pourquoi : les menus ouvraient un inventaire VIDE puis tentaient un re-render asynchrone
 * garde par un test fragile (le menu est-il encore celui ouvert ?). Des que ce garde echouait
 * une fois (fermeture, course, update), le menu restait vide — d'ou l'affichage intermittent.
 * A Present, les menus lisent ce cache de facon SYNCHRONE : aucun re-render, aucune course.
 *
 * Rafraichissement : au boot, apres chaque submit/resolve, et periodiquement (filet de securite).
 */
public final class ReportCache {

    private static final List<Report> OPEN = new CopyOnWriteArrayList<>();
    private static BukkitTask refreshTask;

    private ReportCache() {
    }

    /** Instantan synchrone (jamais null) : ce que les menus affichent. */
    public static List<Report> openSnapshot() {
        return new ArrayList<>(OPEN);
    }

    /** Relit le store en asynchrone ; a appeler apres toute ecriture (submit/resolve). */
    public static void refreshAsync() {
        ReportStores.get().listOpen().whenComplete((list, throwable) -> Bukkit.getScheduler()
                .runTask(Guard.getInstance(), () -> {
                    if (throwable != null) {
                        Guard.getInstance().getLogger().warning("[Reports] cache non rafraichi: " + throwable);
                        return;
                    }
                    List<Report> sorted = new ArrayList<>(list == null ? new ArrayList<>() : list);
                    sorted.sort(Comparator.comparingLong(Report::getCreatedAt).reversed());
                    OPEN.clear();
                    OPEN.addAll(sorted);
                }));
    }

    /** Filet de securite : si un submit/resolve echappe a un appel explicite. */
    public static void startPeriodicRefresh() {
        if (refreshTask != null) {
            return;
        }
        refreshTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Guard.getInstance(),
                ReportCache::refreshAsync, 200L, 200L); // toutes les 10 s
    }
}
