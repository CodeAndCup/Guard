package fr.perrier.guard.utils.lang;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Ajoute les clés lang absentes (defaults du jar) SANS toucher aux traductions existantes.
 * Appelé au boot et à chaque /hguard reload pour couvrir les fichiers lang anciens installés chez l'utilisateur.
 * Les valeurs sont conservées BRUTES (String ou List) : les lore de menus sont des listes.
 */
public final class LangMigrator {

    private LangMigrator() {
    }

    public static int fill(File langFile, Map<String, Object> defaults) {
        FileConfiguration lang = YamlConfiguration.loadConfiguration(langFile);
        int added = 0;
        for (Map.Entry<String, Object> e : defaults.entrySet()) {
            if (lang.get(e.getKey()) == null) {
                lang.set(e.getKey(), e.getValue());
                added++;
            }
        }
        if (added > 0) {
            try {
                lang.save(langFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return added;
    }
}
