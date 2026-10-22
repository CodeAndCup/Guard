package fr.perrier.guard.manager.menu.dashboard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Mapping pur slot de tuile → clé lang du dashboard (le rendu Bukkit est vérifié en jeu + à la review). */
public class DashboardMenuTest {

    @Test
    public void fourTilesHaveStableSlotsAndKeys() {
        assertEquals("players", DashboardMenu.tileKey(10));
        assertEquals("reports", DashboardMenu.tileKey(12));
        assertEquals("moderation", DashboardMenu.tileKey(14));
        assertEquals("tools", DashboardMenu.tileKey(16));
    }

    @Test
    public void unknownSlotIsNull() {
        assertNull(DashboardMenu.tileKey(0));
        assertNull(DashboardMenu.tileKey(11));
        assertNull(DashboardMenu.tileKey(99));
    }
}
