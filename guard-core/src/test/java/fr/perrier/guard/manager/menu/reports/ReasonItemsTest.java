package fr.perrier.guard.manager.menu.reports;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pur : les raisons prédéfinies sont itérées depuis les clés de lang (jamais une enum codée). */
public class ReasonItemsTest {

    private YamlConfiguration lang() {
        YamlConfiguration lang = new YamlConfiguration();
        lang.set("reports.reasons.cheat.name", "Triche");
        lang.set("reports.reasons.cheat.duration", "");
        lang.set("reports.reasons.toxic.name", "Toxicité");
        lang.set("reports.reasons.grief.name", "Grief");
        return lang;
    }

    @Test
    public void reasonKeysFollowLangOrder() {
        // ordre du fichier = ordre d'insertion (LinkedHashMap Bukkit)
        assertEquals(Arrays.asList("cheat", "toxic", "grief"),
                ReportReasonMenu.reasonKeys(lang()));
    }

    @Test
    public void missingSectionMeansNoPredefinedReasons() {
        YamlConfiguration empty = new YamlConfiguration();
        assertEquals(Collections.emptyList(), ReportReasonMenu.reasonKeys(empty));
    }

    @Test
    public void sectionWithoutChildrenMeansNoReasons() {
        YamlConfiguration lang = new YamlConfiguration();
        lang.set("reports.reasons", "pas-une-section");
        assertEquals(Collections.emptyList(), ReportReasonMenu.reasonKeys(lang));
    }

    @Test
    public void everyReasonHasAReadableLabel() {
        YamlConfiguration lang = lang();
        for (String key : ReportReasonMenu.reasonKeys(lang)) {
            assertTrue(lang.getString("reports.reasons." + key + ".name", "").length() > 0,
                    "raison sans nom affichable: " + key);
        }
    }
}
