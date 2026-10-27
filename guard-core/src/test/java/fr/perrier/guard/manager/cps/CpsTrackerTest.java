package fr.perrier.guard.manager.cps;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Fenêtre glissante : pic > limit, stabilité (variance ≤ tolerance) avec plancher, cooldown. */
public class CpsTrackerTest {

    private static CpsTracker tracker() {
        return new CpsTracker("Steve", 20, 8, 0.1, 5, 30);
    }

    /** Pousse n secondes identiques de `cps` clics gauches. */
    private static void fill(CpsTracker t, int seconds, int cps) {
        for (int i = 0; i < seconds; i++) {
            for (int c = 0; c < cps; c++) {
                t.leftClick();
            }
            t.rollSecond();
        }
    }

    @Test
    public void peakAboveLimitTriggersAlert() {
        CpsTracker t = tracker();
        fill(t, 5, 21);
        assertEquals(CpsTracker.Signal.PEAK, t.evaluate(1000000L));
    }

    @Test
    public void atLimitDoesNotTrigger() {
        // moyenne pile 20 (pas > limit) ET irrégulière (pas stable) : aucun signal
        CpsTracker t = tracker();
        int[] counts = {19, 21, 19, 21, 20};
        for (int c : counts) {
            for (int i = 0; i < c; i++) {
                t.leftClick();
            }
            t.rollSecond();
        }
        assertNull(t.evaluate(1000000L));
    }

    @Test
    public void incompleteWindowStaysSilent() {
        CpsTracker t = tracker();
        fill(t, 3, 40);
        assertNull(t.evaluate(1000000L));
    }

    @Test
    public void constantRateAboveFloorIsStable() {
        CpsTracker t = tracker();
        fill(t, 5, 12);
        assertEquals(CpsTracker.Signal.STABLE, t.evaluate(1000000L));
    }

    @Test
    public void jitteryRateIsNotStable() {
        CpsTracker t = tracker();
        int[] counts = {9, 15, 10, 14, 11};
        for (int c : counts) {
            for (int i = 0; i < c; i++) {
                t.leftClick();
            }
            t.rollSecond();
        }
        assertNull(t.evaluate(1000000L));
    }

    @Test
    public void stabilityBelowFloorIsIgnored() {
        CpsTracker t = tracker();
        fill(t, 5, 5);
        assertNull(t.evaluate(1000000L));
    }

    @Test
    public void cooldownSuppressesSecondAlert() {
        CpsTracker t = tracker();
        fill(t, 5, 21);
        long now = 1000000L;
        assertEquals(CpsTracker.Signal.PEAK, t.evaluate(now));
        // toujours en pic mais dans les 30 s du cooldown : silencieux
        fill(t, 1, 21);
        assertNull(t.evaluate(now + 10000L));
        // après le cooldown l'alerte repart (le pic persiste toujours)
        assertEquals(CpsTracker.Signal.PEAK, t.evaluate(now + 31000L));
    }

    @Test
    public void staleSecondsDrainTheWindow() {
        CpsTracker t = tracker();
        fill(t, 5, 25);
        fill(t, 5, 0); // 5 s sans clic : la fenêtre ne contient plus que des 0
        assertNull(t.evaluate(1000000L));
        assertEquals(0.0, t.leftCps(), 1e-9);
    }

    @Test
    public void rollReportsCompletedSecondThenResets() {
        CpsTracker t = tracker();
        t.leftClick();
        t.leftClick();
        t.rightClick(1.0);
        t.rightClick(0.5);
        assertArrayEquals(new double[]{2.0, 1.5}, t.rollSecond(), 1e-9);
        assertArrayEquals(new double[]{0.0, 0.0}, t.rollSecond(), 1e-9);
    }

    @Test
    public void cpsAveragesExposeWindowValues() {
        CpsTracker t = tracker();
        for (int i = 0; i < 5; i++) {
            for (int c = 0; c < 10; c++) {
                t.leftClick();
            }
            for (int c = 0; c < 4; c++) {
                t.rightClick(1.0);
            }
            t.rollSecond();
        }
        assertEquals(10.0, t.leftCps(), 1e-9);
        assertEquals(4.0, t.rightCps(), 1e-9);
    }

    @Test
    public void observersReceiveExactlyTheirSampleCount() {
        CpsTracker t = tracker();
        List<double[]> shortRun = new ArrayList<>();
        List<double[]> longRun = new ArrayList<>();
        t.observe(2, shortRun::add);
        t.observe(4, longRun::add);
        fill(t, 6, 7); // 6 fins de seconde : les deux relevés s'arrêtent seuls
        assertEquals(2, shortRun.size());
        assertEquals(4, longRun.size());
        assertEquals(7.0, longRun.get(3)[0], 1e-9);
    }
}
