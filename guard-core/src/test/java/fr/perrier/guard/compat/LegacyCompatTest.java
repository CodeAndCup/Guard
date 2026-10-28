package fr.perrier.guard.compat;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Épingle le mapping legacy de la palette B (spéc : #0E6F7D→§3, #17A1B2→§b, #2EE6D6→§b, #FFFFFF→§f, hex inconnu → §b). */
public class LegacyCompatTest {

    private final LegacyCompat compat = new LegacyCompat();

    @Test
    public void translatesLegacyAmpCodes() {
        assertEquals("§aok", compat.colorize("&aok"));
    }

    @Test
    public void mapsPaletteVifToAqua() {
        assertEquals("§bhi", compat.colorize("#17A1B2hi"));
    }

    @Test
    public void mapsPaletteSombreToDarkAqua() {
        assertEquals("§3dark", compat.colorize("#0E6F7Ddark"));
    }

    @Test
    public void mapsPaletteFlashAndWhite() {
        assertEquals("§bflash", compat.colorize("#2EE6D6flash"));
        assertEquals("§finfo", compat.colorize("#FFFFFFinfo"));
    }

    @Test
    public void unknownHexFallsBackToAquaWithoutRawVisible() {
        assertEquals("§bzzz", compat.colorize("#123456zzz"));
        assertFalse(compat.colorize("#123456zzz").contains("#"), "aucun caractère brut hex ne doit rester");
    }

    @Test
    public void hexIsCaseInsensitive() {
        assertEquals("§3dark", compat.colorize("#0e6f7ddark"));
    }

    private static void assertFalse(boolean b, String msg) {
        Assertions.assertFalse(b, msg);
    }
}