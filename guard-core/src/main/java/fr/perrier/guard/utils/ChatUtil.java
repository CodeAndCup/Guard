package fr.perrier.guard.utils;

import fr.perrier.guard.compat.CompatHolder;
import net.md_5.bungee.api.*;

public class ChatUtil {

    /** Limite des titres d'inventaire : compter les caractères VISIBLES (§x§R§G§B = séquences de 14 chars, 0 visible). */
    public static final int TITLE_LIMIT = 32;

    public static String translate(String message) {
        return CompatHolder.get().colorize(message);
    }

    /**
     * Truncate a translated title to a count of VISIBLE characters, never cutting mid-sequence
     * (a §x§R§G§B hex run is 14 chars / 0 visible) so colors never leak on the tail.
     */
    public static String truncateTitle(String title, int maxVisible) {
        int visible = 0;
        int i = 0;
        while (i < title.length()) {
            char c = title.charAt(i);
            if (c == '\u00A7' && i + 1 < title.length()) {
                i += 2; // code de couleur : 0 caractère visible
                continue;
            }
            if (visible == maxVisible) {
                // pas de §6 orphelin en queue : on recule sur les codes de couleur pendants
                while (i >= 2 && title.charAt(i - 2) == '\u00A7') {
                    i -= 2;
                }
                return title.substring(0, i);
            }
            visible++;
            i++;
        }
        return title;
    }

    public static String prefix(String message) {
        return Messages.prefix() + ChatUtil.translate(message);
    }
}
