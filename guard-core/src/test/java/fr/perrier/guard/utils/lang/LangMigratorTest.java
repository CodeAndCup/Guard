package fr.perrier.guard.utils.lang;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Le migrateur n'ajoute QUE les clés absentes : les traductions de l'utilisateur sont préservées. */
public class LangMigratorTest {

    @TempDir
    File temp;

    private Map<String, Object> defaults() {
        Map<String, Object> m = new HashMap<>();
        m.put("menu.dashboard.title", "#0E6F7DGuard");
        m.put("menu.reports.empty", "#FFFFFFAucun report");
        // les lore des menus sont des LISTES dans les lang : le migrateur doit les conserver telles quelles
        m.put("menu.reports.item.lore", Arrays.asList("l1", "l2"));
        return m;
    }

    @Test
    public void addsMissingKeys() throws IOException {
        File lang = new File(temp, "message_fr.yml");
        YamlConfiguration init = new YamlConfiguration();
        init.set("old.key", "vieux");
        init.save(lang);

        int added = LangMigrator.fill(lang, defaults());

        assertEquals(3, added);
        YamlConfiguration after = YamlConfiguration.loadConfiguration(lang);
        assertEquals("vieux", after.getString("old.key"));
        assertEquals("#0E6F7DGuard", after.getString("menu.dashboard.title"));
        assertEquals("#FFFFFFAucun report", after.getString("menu.reports.empty"));
        // la clé liste reste une liste : getStringList doit renvoyer le contenu intact
        assertEquals(Arrays.asList("l1", "l2"), after.getStringList("menu.reports.item.lore"));
    }

    @Test
    public void neverOverwritesUserTranslations() throws IOException {
        File lang = new File(temp, "message_fr.yml");
        YamlConfiguration init = new YamlConfiguration();
        init.set("menu.dashboard.title", "Ma Couleur A Moi");
        init.save(lang);

        int added = LangMigrator.fill(lang, defaults());

        assertEquals(2, added); // menu.reports.empty + menu.reports.item.lore
        YamlConfiguration after = YamlConfiguration.loadConfiguration(lang);
        assertEquals("Ma Couleur A Moi", after.getString("menu.dashboard.title"));
        assertEquals("#FFFFFFAucun report", after.getString("menu.reports.empty"));
    }

    @Test
    public void secondFillAddsNothing() throws IOException {
        File lang = new File(temp, "message_fr.yml");

        LangMigrator.fill(lang, defaults());
        int second = LangMigrator.fill(lang, defaults());

        assertEquals(0, second);
    }

    @Test
    public void missingFileIsCreated() {
        File lang = new File(temp, "message_fr.yml");
        int added = LangMigrator.fill(lang, defaults());
        assertEquals(3, added);
        assertTrue(lang.exists());
    }
}
