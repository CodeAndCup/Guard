package fr.perrier.guard.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pur : UUID offline deterministe (algorithme Mojang) + recherche de methode put (Warn Paper du mainteneur). */
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
        assertEquals(false, Heads.offlineUuid("Steve").equals(Heads.offlineUuid("Alex")));
    }

    /** Faux PropertySet Paper : pas de put(Object,Object) érasé, seulement put(String, Object). */
    static class PaperLikePropertySet {
        String lastKey;
        Object lastValue;

        public void put(String key, Object value) {
            this.lastKey = key;
            this.lastValue = value;
        }
    }

    @Test
    public void putFindsNonErasedSignature() throws Exception {
        // LE warn du mainteneur : CraftPlayerProfile$PropertySet.put(Object,Object) introuvable.
        // Le helper doit trouver la signature reelle parmi les methodes publiques.
        PaperLikePropertySet set = new PaperLikePropertySet();
        ItemBuilder.reflectivePut(set, "textures", "hash-value");
        assertEquals("textures", set.lastKey);
        assertEquals("hash-value", set.lastValue);
    }

    @Test
    public void putStillPrefersErasedSignature() throws Exception {
        // HashMap expose bien put(Object,Object) : le chemin direct doit rester pris
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        ItemBuilder.reflectivePut(map, "k", "v");
        assertEquals("v", map.get("k"));
    }

    @Test
    public void putFailsLoudlyWhenNothingMatches() {
        assertThrows(NoSuchMethodException.class,
                () -> ItemBuilder.reflectivePut(new Object(), "k", "v"));
    }
}

