package fr.perrier.guard.manager.menu.profile;

import fr.perrier.guard.utils.ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pur : ordre des onglets (fixe) + couleur du ping (thresholds deterministes). */
public class ProfileMenuTest {

    @Test
    public void tabOrderIsFixed() {
        assertEquals(Arrays.asList("SANCTIONS", "INVENTORY", "HISTORY"), ProfileMenu.tabs());
    }

    @Test
    public void pingColorThresholds() {
        assertEquals("&a", ProfileMenu.pingColor(12));
        assertEquals("&e", ProfileMenu.pingColor(99));
        assertEquals("&e", ProfileMenu.pingColor(150));
        assertEquals("&c", ProfileMenu.pingColor(151));
        assertEquals("&c", ProfileMenu.pingColor(400));
    }

    @Test
    public void protocolVersionKnownMappings() {
        assertEquals("1.8", ProtocolVersion.of(47));
        assertEquals("1.16.4", ProtocolVersion.of(754));
        assertEquals("1.21", ProtocolVersion.of(767));
    }

    @Test
    public void protocolVersionUnknownFallsBack() {
        assertEquals("p999", ProtocolVersion.of(999));
    }
}
