package fr.perrier.guard.reports;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Positions sauvegardées : le tp doit s'annuler proprement (pas de NPE, pas de téléportation
 * fallacieuse) si le monde n'est pas chargé ou si le rapport n'a pas de position.
 */
public class ReportLocationTest {

    private static Report withLocation(String world, double x, double y, double z,
                                       float yaw, float pitch) {
        Report r = new Report("id", "Alex", "Steve", "r", "lobby", 0L, Report.STATUS_OPEN, null);
        r.setLocationWorld(world);
        r.setLocationX(x);
        r.setLocationY(y);
        r.setLocationZ(z);
        r.setLocationYaw(yaw);
        r.setLocationPitch(pitch);
        return r;
    }

    @Test
    public void valuesRoundTripFromReport() {
        ReportLocation loc = ReportLocation.of(withLocation("world", 12.5, 64.0, -3.25, 90.0F, -15.0F));

        assertTrue(loc.matches("world"));
        assertEquals("world", loc.world());
        assertEquals(12.5, loc.x(), 0.0001);
        assertEquals(64.0, loc.y(), 0.0001);
        assertEquals(-3.25, loc.z(), 0.0001);
        assertEquals(90.0F, loc.yaw(), 0.001F);
        assertEquals(-15.0F, loc.pitch(), 0.001F);
    }

    @Test
    public void otherWorldRefusesToTeleport() {
        ReportLocation loc = ReportLocation.of(withLocation("world_nether", 1, 2, 3, 0F, 0F));
        assertFalse(loc.matches("world"));
        assertNull(loc.toLocation("world")); // jamais de tp dans le mauvais monde
    }

    @Test
    public void reportWithoutLocationHasNoTeleport() {
        Report r = new Report("id", "Alex", "Steve", "r", "lobby", 0L, Report.STATUS_OPEN, null);
        ReportLocation loc = ReportLocation.of(r);
        assertNull(loc); // pas de snapshot → pas de bouton de tp
    }

    @Test
    public void partialLocationStillRoundTripsCoordinates() {
        // monde posé mais coordonnées à 0 (rapport très ancien / partiel) : position valide
        ReportLocation loc = ReportLocation.of(withLocation("world", 0, 0, 0, 0F, 0F));
        assertNotNull(loc);
        assertTrue(loc.matches("world"));
    }
}
