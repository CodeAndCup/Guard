package fr.perrier.guard.modern;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

/** Chemin des serveurs capables d'exécuter le bytecode Java 21 : rend les hex natifs (#RRGGBB), pas de dégradation de palette.
 *  Aucune référence au core : lié par réflexion par CompatResolver (cycle Maven interdit l'import). */
public final class ModernCompat {

    private static final Pattern HEX = Pattern.compile("#[0-9a-fA-F]{6}");

    public String colorize(String raw) {
        StringBuilder out = new StringBuilder();
        Matcher m = HEX.matcher(raw);
        while (m.find()) {
            // forme canonique §x : un code format par chiffre, sinon le serializer legacy
            // laisse les 6 chiffres en texte brut
            StringBuilder digits = new StringBuilder();
            for (char c : m.group().substring(1).toUpperCase(Locale.ROOT).toCharArray()) {
                digits.append('\u00A7').append(c);
            }
            m.appendReplacement(out, Matcher.quoteReplacement("\u00A7x" + digits));
        }
        m.appendTail(out);
        return ChatColor.translateAlternateColorCodes('&', out.toString());
    }
}