package fr.perrier.guard.reports;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Parse du payload notify "kind/server/text" : malformé → drop, jamais d'exception. */
public class ReportFlashTest {

    @Test
    public void parsesWellFormedPayload() {
        assertArrayEquals(new String[]{"REPORT", "lobby", "#12 Alex->Steve: hacks"},
                ReportFlash.parse("REPORT\u0000lobby\u0000#12 Alex->Steve: hacks"));
    }

    @Test
    public void malformedPayloadsAreDropped() {
        assertNull(ReportFlash.parse("REPORT"));
        assertNull(ReportFlash.parse("REPORT\u0000lobby"));
        assertNull(ReportFlash.parse(""));
    }

    /** Le préfixe vient UNIQUEMENT de la clé prefix (passé en argument, déjà traduit) ;
     *  la clé flash ne porte que le contenu — plus de marqueur [Report]/[Guard] codé en dur. */
    @Test
    public void alertRenderPrependsPluginPrefix() {
        assertEquals("#0E6F7D&lGuard &8» §f#12 Alex->Steve: grief",
                ReportFlash.render("#0E6F7D&lGuard &8» ", "&f%text%", "#12 Alex->Steve: grief"));
        assertEquals("#0E6F7D&lGuard &8» Steve pic à 21.0 cps !",
                ReportFlash.render("#0E6F7D&lGuard &8» ", "%text%", "Steve pic à 21.0 cps !"));
    }
}
