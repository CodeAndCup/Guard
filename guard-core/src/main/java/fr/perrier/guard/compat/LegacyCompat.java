package fr.perrier.guard.compat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

/** Chemin des serveurs qui ne peuvent pas charger le module Java 21 : palette B dégradée en codes §. */
public final class LegacyCompat implements Compat {

    // Palette B (spec) — hex connus → nuancier legacy le plus proche.
    static final Map<String, String> PALETTE = new HashMap<>();
    static {
        PALETTE.put("0E6F7D", "\u00A73");
        PALETTE.put("17A1B2", "\u00A7b");
        PALETTE.put("2EE6D6", "\u00A7b");
        PALETTE.put("FFFFFF", "\u00A7f");
    }

    private static final Pattern HEX = Pattern.compile("#[0-9a-fA-F]{6}");

    @Override
    public String colorize(String raw) {
        // --release 8 : Matcher n'accepte pas StringBuilder, StringBuffer obligatoire
        StringBuffer out = new StringBuffer();
        Matcher m = HEX.matcher(raw);
        while (m.find()) {
            String code = PALETTE.get(m.group().substring(1).toUpperCase(Locale.ROOT));
            m.appendReplacement(out, Matcher.quoteReplacement(code != null ? code : "\u00A7b"));
        }
        m.appendTail(out);
        return ChatColor.translateAlternateColorCodes('&', out.toString());
    }
}