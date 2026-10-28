package fr.perrier.guard.manager.menu.dashboard;

import fr.perrier.guard.reports.Report;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pur : glyphe de statut + rendu du lore d'un report depuis le template lang (sans Bukkit). */
public class ReportsMenuTest {

    @Test
    public void shortIdKeepsEightFirstChars() {
        assertEquals("abcdefgh", ReportsMenu.shortId("abcdefgh-1234-5678"));
        assertEquals("abc", ReportsMenu.shortId("abc")); // plus court : inchangé
    }

    @Test
    public void shortIdSurvivesNull() {
        assertEquals("null", ReportsMenu.shortId(null));
    }

    @Test
    public void loreShowsHandlerDashWhenNone() {
        Report r = new Report("11111111-2222-3333-4444-555555555555", "Alex", "Steve", "x", "lobby",
                0L, Report.STATUS_IN_PROGRESS, null);
        assertEquals("&eResponsable: \u2014",
                ReportsMenu.loreLines(r, Arrays.asList("&eResponsable: %handler%")).get(0));
        r.setHandler("Modo1");
        assertEquals("&eResponsable: Modo1",
                ReportsMenu.loreLines(r, Arrays.asList("&eResponsable: %handler%")).get(0));
    }

    @Test
    public void statusGlyphIsDeterministicPerStatus() {
        assertEquals("●", ReportsMenu.statusGlyph(Report.STATUS_OPEN));
        assertEquals("★", ReportsMenu.statusGlyph(Report.STATUS_IN_PROGRESS));
        assertEquals("✔", ReportsMenu.statusGlyph(Report.STATUS_CLOSED));
        assertEquals("✘", ReportsMenu.statusGlyph(Report.STATUS_DELETED));
        assertEquals("?", ReportsMenu.statusGlyph("whatever"));
        assertEquals("?", ReportsMenu.statusGlyph(null));
    }

    @Test
    public void loreLinesReplacePlaceholders() {
        Report r = new Report("11111111-1111-1111-1111-111111111111", "Alex", "Steve", "forcefield", "lobby",
                1690000000000L, Report.STATUS_OPEN, null);
        List<String> out = ReportsMenu.loreLines(r, Arrays.asList(
                "#0E6F7DReport #%id% %status%",
                "&fDe: %reporter% -> %reported%",
                "&fRaison: %reason%",
                "&fServeur: %server%"));

        // UUID raccourci à l'affichage (recherche par préfixe côté commande)
        assertEquals("#0E6F7DReport #11111111 ●", out.get(0));
        assertEquals("&fDe: Alex -> Steve", out.get(1));
        assertEquals("&fRaison: forcefield", out.get(2));
        assertEquals("&fServeur: lobby", out.get(3));
        assertEquals(4, out.size());
    }
}
