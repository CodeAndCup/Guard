package fr.perrier.guard.manager.cps;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Flux CPS d'un joueur : comptage par seconde (main thread) + verdict sur une fenêtre
 * glissante. Pur (zéro Bukkit) : la config est injectée, le temps est passé en argument.
 *
 * Deux signaux indépendants sur la fenêtre : PEAK (moyenne > limit) ou
 * STABLE (fenêtre pleine, moyenne ≥ floor, variance ≤ tolerance — cadence bot).
 * Un cooldown par joueur couvre les deux signaux (pas de spam d'alertes).
 * Synchronisé : comptage main thread, analyse async (CpsService).
 */
public final class CpsTracker {

    public enum Signal {PEAK, STABLE}

    private final String name;
    private final int limit;
    private final int floor;
    private final double tolerance;
    private final int window;
    private final long cooldownMs;

    private final double[] leftRing;
    private final double[] rightRing;
    private int cursor;
    private int samples;
    private long lastAlertAt;

    private double currentLeft;
    private double currentRight;

    /** Relevés actifs (/cps) : null = zéro coût pour les joueurs non observés. */
    private List<Observer> observers;

    public CpsTracker(String name, int limit, int floor, double tolerance, int window, long cooldownSeconds) {
        this.name = name;
        this.limit = limit;
        this.floor = floor;
        this.tolerance = tolerance;
        this.window = Math.max(1, window);
        this.cooldownMs = Math.max(0, cooldownSeconds) * 1000L;
        this.leftRing = new double[this.window];
        this.rightRing = new double[this.window];
    }

    public String name() {
        return name;
    }

    public synchronized void leftClick() {
        currentLeft++;
    }

    /** weight : 1.0 avec un bloc en main, 0.5 sinon (l'event se déclenche 2 fois sans bloc). */
    public synchronized void rightClick(double weight) {
        currentRight += weight;
    }

    /** Fin de seconde : fige les clics de la seconde écoulée dans la fenêtre, renvoie {gauche, droite}.
     *  Les relevés actifs reçoivent l'échantillon et se désabonnent seuls après leur dernier. */
    public synchronized double[] rollSecond() {
        double[] out = {currentLeft, currentRight};
        currentLeft = 0;
        currentRight = 0;
        leftRing[cursor] = out[0];
        rightRing[cursor] = out[1];
        cursor = (cursor + 1) % window;
        if (samples < window) {
            samples++;
        }
        if (observers != null && !observers.isEmpty()) {
            // copie : un relevé peut s'abonner depuis son propre callback
            for (Observer o : new ArrayList<>(observers)) {
                o.listener.accept(out);
                if (--o.remaining <= 0) {
                    observers.remove(o);
                }
            }
        }
        return out;
    }

    /** Relevé périodique (/cps) : `samples` fins de seconde livrées puis désabonnement auto. */
    public synchronized void observe(int samples, Consumer<double[]> listener) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        observers.add(new Observer(Math.max(1, samples), listener));
    }

    /** Moyenne de clics/s (gauche et droite) sur les secondes remplies de la fenêtre. */
    public synchronized double leftCps() {
        return average(leftRing);
    }

    public synchronized double rightCps() {
        return average(rightRing);
    }

    /** Verdict sur la fenêtre, ou null (pas de signal, fenêtre incomplète, ou cooldown).
     *  Pose le cooldown quand un signal est rendu. */
    public synchronized Signal evaluate(long nowMs) {
        if (samples < window) {
            return null;
        }
        if (nowMs - lastAlertAt < cooldownMs) {
            return null;
        }
        double avg = average(leftRing);
        Signal signal;
        if (avg > limit) {
            signal = Signal.PEAK;
        } else if (avg >= floor && variance(leftRing) <= tolerance) {
            signal = Signal.STABLE;
        } else {
            return null;
        }
        lastAlertAt = nowMs;
        return signal;
    }

    private double average(double[] ring) {
        if (samples == 0) {
            return 0.0;
        }
        double sum = 0;
        for (int i = 0; i < samples; i++) {
            sum += ring[i];
        }
        return sum / samples;
    }

    /** Variance population sur les secondes remplies. */
    private double variance(double[] ring) {
        double mean = average(ring);
        double sum = 0;
        for (int i = 0; i < samples; i++) {
            double d = ring[i] - mean;
            sum += d * d;
        }
        return sum / samples;
    }

    /** Relevé /cps : compte à rebours d'échantillons (main thread uniquement). */
    private static final class Observer {
        final Consumer<double[]> listener;
        int remaining;

        Observer(int samples, Consumer<double[]> listener) {
            this.remaining = samples;
            this.listener = listener;
        }
    }
}
