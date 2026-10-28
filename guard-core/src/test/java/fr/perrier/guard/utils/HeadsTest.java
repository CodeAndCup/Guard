package fr.perrier.guard.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Pur : UUID offline deterministe (algorithme Mojang) utilise par Heads.skullOf sans reseau.
 * Les tests sur reflectivePut ont disparu avec la suppression des textures custom
 * (WARN PropertySet) : plus aucune dependance a authlib dans le plugin.
 */
public class HeadsTest {

    @Test
    public void offlineUuidIsDeterministic() {
        // meme appel -> meme UUID (tete identique a chaque ouverture de menu)
        assertEquals(Heads.offlineUuid("Steve"), Heads.offlineUuid("Steve"));
    }

    @Test
    public void offlineUuidIsNameBasedMd5() {
        // UUID v3 (name-based MD5) = la signature de UUID.nameUUIDFromBytes, ce qui prouve
        // que l'algorithme Mojang offline est bien applique (et pas un UUID aleatoire)
        java.util.UUID uuid = Heads.offlineUuid("Herobrine");
        assertEquals(3, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    public void offlineUuidDiffersPerName() {
        assertNotEquals(Heads.offlineUuid("Steve"), Heads.offlineUuid("Alex"));
    }
}


