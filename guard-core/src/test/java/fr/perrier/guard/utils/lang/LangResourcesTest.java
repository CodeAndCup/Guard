package fr.perrier.guard.utils.lang;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Filet YAML : les deux ressources lang shipées doivent parser proprement en Map racine non vide
 * (une config cassée rend les menus muets — keys indéfinies tombent sur les defaults du code).
 */
public class LangResourcesTest {

    @SuppressWarnings("unchecked")
    private Map<String, Object> load(String resource) throws Exception {
        InputStream in = getClass().getResourceAsStream("/" + resource);
        assertNotNull(in, "resource absente du jar: " + resource);
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            Object root = new Yaml().load(reader);
            assertNotNull(root, resource + " : YAML cassé (racine vide)");
            assertTrue(root instanceof Map, resource + " : YAML cassé (racine non-map)");
            return (Map<String, Object>) root;
        }
    }

    private void check(String file) throws Exception {
        Map<String, Object> root = load(file);
        assertTrue(root.containsKey("menu"), file + " : section menu absente");
        assertTrue(root.containsKey("items"), file + " : section items absente");
        assertTrue(root.containsKey("commands"), file + " : section commands absente");
    }

    @Test
    public void messageFrParses() throws Exception {
        check("message_fr.yml");
    }

    @Test
    public void messageEnParses() throws Exception {
        check("message_en.yml");
    }
}
