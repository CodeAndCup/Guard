package fr.perrier.guard.manager.menu.profile;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pur : ordre des onglets (fixe) + couleur du ping (thresholds deterministes). */
public class ProfileMenuTest {

    @Test
    public void tabOrderIsFixed() {
        assertEquals(Arrays.asList("SANCTIONS", "INVENTORY", "HISTORY", "INFO"), ProfileMenu.tabs());
    }

    @Test
    public void pingColorThresholds() {
        assertEquals("&a", ProfileMenu.pingColor(12));
        assertEquals("&e", ProfileMenu.pingColor(99));
        assertEquals("&e", ProfileMenu.pingColor(150));
        assertEquals("&c", ProfileMenu.pingColor(151));
        assertEquals("&c", ProfileMenu.pingColor(400));
    }
}
