package fr.perrier.guard.compat;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/** Épingle le seuil modern/legacy : JVM ≥ 21 ET MC > 1.16.0, sinon fallback legacy jamais modern. */
public class CompatResolverTest {

    @Test
    public void modernRendersNativeHexWhenServerAndJvmAreRecent() {
        // le contrat visible : sur un serveur moderne, les hex restent natifs en forme CANONIQUE §x
        // (un §x suivi de codes format, un par chiffre — sinon le serializer legacy laisse les chiffres bruts)
        assertEquals("\u00A7x\u00A71\u00A77\u00A7A\u00A71\u00A7B\u00A72hi",
                CompatResolver.select("26.3", "25").colorize("#17A1B2hi"));
    }

    @Test
    public void missingModernClassFallsBackToLegacy() {
        assertInstanceOf(LegacyCompat.class,
                CompatResolver.select("26.3", "25", "fr.perrier.guard.modern.DoesNotExist"));
    }

    @Test
    public void oldMcNeverLoadsModern() {
        assertInstanceOf(LegacyCompat.class, CompatResolver.select("1.8.8", "25"));
    }

    @Test
    public void oldJvmNeverLoadsModern() {
        assertInstanceOf(LegacyCompat.class, CompatResolver.select("26.3", "17"));
    }

    @Test
    public void mcVersionThreshold() {
        assertTrue(CompatResolver.isModernMc("26.3"));
        assertTrue(CompatResolver.isModernMc("1.21.9"));
        assertTrue(CompatResolver.isModernMc("1.16.1"));
        assertFalse(CompatResolver.isModernMc("1.16.0"));   // seuil posé par le plan : au-dessus de 1.16.0
        assertFalse(CompatResolver.isModernMc("1.15.2"));
        assertFalse(CompatResolver.isModernMc("1.8.8"));
    }

    @Test
    public void mcVersionThresholdAcceptsRealBukkitStrings() {
        // formes brutes de getServer().getBukkitVersion() : "1.21.5-R0.1-SNAPSHOT"
        assertTrue(CompatResolver.isModernMc("1.21.5-R0.1-SNAPSHOT"));
        assertTrue(CompatResolver.isModernMc("26.3-dev"));
        assertFalse(CompatResolver.isModernMc("1.8.8-R0.1-SNAPSHOT"));
    }

    @Test
    public void javaVersionThreshold() {
        assertFalse(CompatResolver.javaMajorAtLeast21("17.0.9"));
        assertFalse(CompatResolver.javaMajorAtLeast21("1.8.0_361"));
        assertTrue(CompatResolver.javaMajorAtLeast21("21"));
        assertTrue(CompatResolver.javaMajorAtLeast21("25"));
    }
}