package fr.perrier.guard.manager.menu.dashboard;

import fr.perrier.guard.reports.Report;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pur : glyphe de statut + rendu du lore d'un report depuis le template lang (sans Bukkit). */
public class ReportsMenuTest {

    @Test
    public void statusGlyphIsDeterministicPerStatus() {
        assertEquals("●", ReportsMenu.statusGlyph(Report.STATUS_OPEN));
        assertEquals("✔", ReportsMenu.statusGlyph(Report.STATUS_ACCEPTED));
        assertEquals("✘", ReportsMenu.statusGlyph(Report.STATUS_REJECTED));
        assertEquals("?", ReportsMenu.statusGlyph("whatever"));
        assertEquals("?", ReportsMenu.statusGlyph(null));
    }

    @Test
    public void loreLinesReplacePlaceholders() {
        Report r = new Report(12, "Alex", "Steve", "forcefield", "lobby",
                1690000000000L, Report.STATUS_OPEN, null);
        List<String> out = ReportsMenu.loreLines(r, Arrays.asList(
                "#0E6F7DReport #%id% %status%",
                "&fDe: %reporter% -> %reported%",
                "&fRaison: %reason%",
                "&fServeur: %server%"));

        assertEquals("#0E6F7DReport #12 ●", out.get(0));
        assertEquals("&fDe: Alex -> Steve", out.get(1));
        assertEquals("&fRaison: forcefield", out.get(2));
        assertEquals("&fServeur: lobby", out.get(3));
        assertEquals(4, out.size());
    }
}
