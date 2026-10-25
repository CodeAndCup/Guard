package fr.perrier.guard.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pur : tronquage des titres d'inventaire (limite 32 caractères VISIBLES, séquences § intactes). */
public class ChatUtilTest {

    @Test
    public void shortTitleUntouched() {
        assertEquals("abc", ChatUtil.truncateTitle("abc", 32));
    }

    @Test
    public void legacyCodesDoNotCountVisible() {
        // §6 + "abc" = 3 visibles : jamais tronqué
        assertEquals("\u00A76abc", ChatUtil.truncateTitle("\u00A76abc", 32));
    }

    @Test
    public void hexSequenceCountsAsZeroVisible() {
        // §x§0§E§6§F§7§D + "Joueurs" : 7 visibles, jamais tronqué
        String hexTitle = "\u00A7x\u00A70\u00A7E\u00A76\u00A7F\u00A77\u00A7DJoueurs";
        assertEquals(hexTitle, ChatUtil.truncateTitle(hexTitle, 32));
        // avec 40 visibles après le hex : on coupe à 32 visibles, séquence hex intacte en préfixe
        String longHex = "\u00A7x\u00A70\u00A7E\u00A76\u00A7F\u00A77\u00A7D" + repeat('a', 40);
        String out = ChatUtil.truncateTitle(longHex, 32);
        assertEquals("\u00A7x\u00A70\u00A7E\u00A76\u00A7F\u00A77\u00A7D" + repeat('a', 32), out);
    }

    @Test
    public void hexTitleTruncatedOnVisibleCharNotOnSequence() {
        // 14 chars §x + 40 visibles ('a') : on garde les 14 §-chars + 32 'a'
        String title = "\u00A7x\u00A70\u00A7E\u00A76\u00A7F\u00A77\u00A7D" + repeat('a', 40);
        String out = ChatUtil.truncateTitle(title, 32);
        assertEquals(repeat('a', 32), out.substring(14));
        assertEquals(46, out.length());
    }

    @Test
    public void truncationNeverLeavesStraySectionSign() {
        // 31 b + §6c = 32 visibles exactement : aucune coupe
        String title = repeat('b', 31) + "\u00A76c";
        assertEquals(title, ChatUtil.truncateTitle(title, 32));
        // 32 b + §6c : on coupe AVANT le §6 orphelin (jamais de code couleur en queue)
        String cut = ChatUtil.truncateTitle(repeat('b', 32) + "\u00A76c", 32);
        assertEquals(repeat('b', 32), cut);
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }
}
