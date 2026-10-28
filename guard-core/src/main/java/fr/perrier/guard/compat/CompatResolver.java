package fr.perrier.guard.compat;

import java.lang.reflect.Method;

class CompatResolver {

    private CompatResolver() {
    }

    /** Sélection au boot. Throwable gardé : jamais de crash au démarrage du plugin. */
    static Compat select(String mcVersion, String javaVersion) {
        return select(mcVersion, javaVersion, "fr.perrier.guard.modern.ModernCompat");
    }

    /** modernClass paramétrable pour tester le mécanisme de fallback sans rien stubber. */
    static Compat select(String mcVersion, String javaVersion, String modernClass) {
        if (!(isModernMc(mcVersion) && javaMajorAtLeast21(javaVersion))) {
            return new LegacyCompat();
        }
        try {
            Object impl = Class.forName(modernClass).getDeclaredConstructor().newInstance();
            Method colorize = impl.getClass().getMethod("colorize", String.class);
            if (impl instanceof Compat) {
                return (Compat) impl;
            }
            return raw -> {
                try {
                    return (String) colorize.invoke(impl, raw);
                } catch (ReflectiveOperationException e) {
                    return raw;   // on ne casse pas le chat ; le fallback legacy reprend au prochain boot
                }
            };
        } catch (Throwable t) {
            // classe modern manquante (serveur/JVM sans ce bytecode) : la seule suite acceptable est legacy
            java.util.logging.Logger.getLogger("Guard").warning(
                    "Module modern indisponible (" + t.getClass().getSimpleName() + "), fallback legacy.");
            return new LegacyCompat();
        }
    }

    /** modern = version MC au-dessus du seuil hex natif ; le plan épingle le seuil strict > 1.16.0. */
    static boolean isModernMc(String mcVersion) {
        int[] v = parse(mcVersion);
        if (v == null) return false;
        if (v[0] > 1) return true;              // schéma par année (26.x)
        return v[0] == 1 && compare(v, new int[]{1, 16, 0}) > 0;
    }

    static boolean javaMajorAtLeast21(String javaVersion) {
        if (javaVersion == null) return false;
        String head = javaVersion.split("[._]")[0].trim();
        try {
            return Integer.parseInt(head) >= 21;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static int compare(int[] a, int[] b) {
        for (int i = 0; i < 3; i++) {
            int ai = i < a.length ? a[i] : 0;
            int bi = i < b.length ? b[i] : 0;
            if (ai != bi) return ai - bi;
        }
        return 0;
    }

    private static int[] parse(String version) {
        if (version == null) return null;
        try {
            // "1.21.5-R0.1-SNAPSHOT" → "1.21.5" (les getBukkitVersion() portent tous un suffixe de build)
            String[] parts = version.split("-", 2)[0].split("\\.");
            int[] out = new int[Math.min(3, parts.length)];
            for (int i = 0; i < out.length; i++) {
                out[i] = Integer.parseInt(parts[i].trim());
            }
            return out;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}