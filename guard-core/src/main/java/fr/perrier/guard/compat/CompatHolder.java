package fr.perrier.guard.compat;

import java.util.logging.Logger;

/** Point d'accès unique au Compat sélectionné au boot. */
public final class CompatHolder {

    private static volatile Compat compat = new LegacyCompat();
    private static final Logger LOG = Logger.getLogger("Guard");

    private CompatHolder() {
    }

    /** Jamais null après init ; retombe sur legacy si init pas encore passée. */
    public static Compat get() {
        return compat;
    }

    public static void init(String mcVersion, String javaVersion) {
        compat = CompatResolver.select(mcVersion, javaVersion);
        LOG.info("Compat: " + compat.getClass().getSimpleName()
                + " (mc " + mcVersion + ", java " + javaVersion + ")");
    }
}