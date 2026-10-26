package fr.perrier.guard.utils.lang;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Filet YAML : les deux ressources lang shipées doivent parser proprement, SANS clé dupliquée
 * (SnakeYAML accepte les doublons en last-wins et efface silencieusement tout un bloc —
 * ça a déjà coûté la section menu.reports en entier dans les deux lang) et avec les clés
 * v2 des signalements présentes.
 */
public class LangResourcesTest {

    @SuppressWarnings("unchecked")
    private Map<String, Object> load(String resource) throws Exception {
        InputStream in = getClass().getResourceAsStream("/" + resource);
        assertNotNull(in, "resource absente du jar: " + resource);
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false); // un doublon = échec, jamais un last-wins silencieux
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            Object root = new Yaml(options).load(reader);
            assertNotNull(root, resource + " : YAML cassé (racine vide)");
            assertTrue(root instanceof Map, resource + " : YAML cassé (racine non-map)");
            return (Map<String, Object>) root;
        }
    }

    @SuppressWarnings("unchecked")
    private Object path(Map<String, Object> root, String dotted) {
        Object node = root;
        for (String part : dotted.split("\\.")) {
            assertTrue(node instanceof Map, "chemin absent: " + dotted + " (arrêt sur " + part + ")");
            node = ((Map<String, Object>) node).get(part);
            assertNotNull(node, "clé absente: " + dotted);
        }
        return node;
    }

    private void check(String file) throws Exception {
        Map<String, Object> root = load(file);
        assertTrue(root.containsKey("menu"), file + " : section menu absente");
        assertTrue(root.containsKey("items"), file + " : section items absente");
        assertTrue(root.containsKey("commands"), file + " : section commands absente");
        // clés v2 : présentes dans les DEUX lang (une perte silencieuse casse les GUI)
        path(root, "menu.reports.reason.title");
        path(root, "menu.reports.item.name");
        path(root, "menu.reports.detail.status.take");
        path(root, "menu.reports.filter.all");
        path(root, "menu.dashboard.title");
        path(root, "reports.refus.self");
        path(root, "reports.reasons.cheat.name");
        path(root, "reports.usage");
        path(root, "reports.no-perm");
    }

    @Test
    public void messageFrParses() throws Exception {
        check("message_fr.yml");
    }

    @Test
    public void messageEnParses() throws Exception {
        check("message_en.yml");
    }

    /** Le défaut de notifications est bien déclaré (il pilote le premier boot). */
    @Test
    public void configDeclaresNotifyDefault() throws Exception {
        Map<String, Object> root = load("config.yml");
        Object value = path(root, "reports.notify-default");
        assertEquals(Boolean.TRUE, value);
        path(root, "commands.reports.name"); // sans ça, l'enregistrement plante au boot
    }
}
