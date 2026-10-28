package fr.perrier.guard.manager.cps;

import fr.perrier.guard.Guard;
import fr.perrier.guard.reports.ReportFlash;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Scan CPS h24 pour tous les joueurs :
 * - main thread, 1×/s : fin de seconde des compteurs (O(1) par joueur, zéro alloc)
 *   — le comptage reste sur le main thread, seule l'analyse part en async ;
 * - async, 1×/s : verdict sur la fenêtre glissante (pic OU stabilité) ;
 * - main thread : émission via ReportFlash (jamais de Bukkit depuis async).
 */
public final class CpsService {

    private static final ConcurrentHashMap<UUID, CpsTracker> TRACKERS = new ConcurrentHashMap<>();

    private CpsService() {
    }

    /** Tracker du joueur, créé à la première interaction ou à la première lecture (/cps). */
    public static CpsTracker tracker(Player player) {
        CpsTracker existing = TRACKERS.get(player.getUniqueId());
        if (existing != null) {
            return existing;
        }
        CpsTracker created = new CpsTracker(player.getName(),
                Guard.getDefaultConfig().getInt("cps.limit", 20),
                Guard.getDefaultConfig().getInt("cps.floor", 8),
                Guard.getDefaultConfig().getDouble("cps.tolerance", 0.1),
                Guard.getDefaultConfig().getInt("cps.window", 5),
                Guard.getDefaultConfig().getLong("cps.cooldown", 30L));
        CpsTracker raced = TRACKERS.putIfAbsent(player.getUniqueId(), created);
        return raced != null ? raced : created;
    }

    /** Lecture seule (menu profil) : null si ce joueur n'a pas de flux — ne crée jamais. */
    public static CpsTracker peek(UUID uuid) {
        return TRACKERS.get(uuid);
    }

    /** Départ du joueur : son flux et ses relevés en cours meurent avec lui. */
    public static void forget(UUID uuid) {
        TRACKERS.remove(uuid);
    }

    /** Relevé périodique de la commande /cps : un échantillon/seconde pendant `samples` secondes. */
    public static void observe(Player target, int samples, Consumer<double[]> listener) {
        tracker(target).observe(samples, listener);
    }

    /** À appeler une fois dans onEnable. */
    public static void start() {
        // main thread : fige les clics de chaque seconde écoulée
        Bukkit.getScheduler().runTaskTimer(Guard.getInstance(), CpsService::rollAll, 20L, 20L);
        // async : analyse de la fenêtre (verdict pur, synchronisé sur le tracker)
        Bukkit.getScheduler().runTaskTimerAsynchronously(Guard.getInstance(), CpsService::analyze, 20L, 20L);
    }

    private static void rollAll() {
        for (CpsTracker tracker : TRACKERS.values()) {
            tracker.rollSecond();
        }
    }

    private static void analyze() {
        long now = System.currentTimeMillis();
        for (CpsTracker tracker : TRACKERS.values()) {
            CpsTracker.Signal signal = tracker.evaluate(now);
            if (signal != null) {
                emit(tracker, signal);
            }
        }
    }

    /** Émission en main thread : ReportFlash lit les joueurs en ligne (interdit en async).
     *  Le cooldown est déjà posé par evaluate — pas de doublon entre deux ticks d'analyse. */
    private static void emit(CpsTracker tracker, CpsTracker.Signal signal) {
        Bukkit.getScheduler().runTask(Guard.getInstance(), () -> {
            String key = signal == CpsTracker.Signal.PEAK
                    ? "commands.cps.alert.peak" : "commands.cps.alert.stable";
            String text = Guard.getLangConfig().getString(key, "%target% : %cps% cps")
                    .replace("%target%", tracker.name())
                    .replace("%cps%", String.format("%.1f", tracker.leftCps()));
            ReportFlash.broadcast(ReportFlash.KIND_CPS, text);
        });
    }
}
