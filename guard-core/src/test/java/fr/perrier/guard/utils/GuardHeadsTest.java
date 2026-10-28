package fr.perrier.guard.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Texture MineSkin (value + signature) codée en dur par icône de menu.
 * Sans ça, les têtes custom retombent sur l'item vanilla (pas de réseau à l'exécution).
 */
class GuardHeadsTest {

    @Test
    void everyIconHasValueAndSignature() {
        String[] keys = {
                "profile_sanctions", "profile_inventory", "profile_history",
                "moderation_vanish", "moderation_chatlock", "moderation_staffchat",
                "moderation_noclip",
        };
        for (String key : keys) {
            String[] texture = GuardHeads.texture(key);
            assertNotNull(texture, key + " : texture manquante");
            assertTrue(texture[0] != null && texture[0].length() > 50, key + " : value absente");
            assertTrue(texture[1] != null && texture[1].length() > 50, key + " : signature absente");
        }
    }

    @Test
    void unknownKeyIsNull() {
        assertNull(GuardHeads.texture("nope"));
    }
}
