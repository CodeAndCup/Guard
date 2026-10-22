package fr.perrier.guard.reports;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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
}
