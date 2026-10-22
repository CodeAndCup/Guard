package fr.perrier.guard.manager.menu.sanctionset;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pur : catégories, raisons et durées ittérées depuis les clés lang sanction.<cat>.<raison>.* — rien de codé en dur. */
public class SanctionScreenTest {

    private YamlConfiguration lang() {
        YamlConfiguration lang = new YamlConfiguration();
        lang.set("sanction.mute.spam.duration", "1h");
        lang.set("sanction.mute.spam.name", "Spam");
        lang.set("sanction.mute.flood.name", "Flood");
        lang.set("sanction.mute.flood.duration", "30m");
        lang.set("sanction.warn.crossteam.name", "Crossteam");
        lang.set("sanction.warn.crossteam.duration", "-1");
        lang.set("sanction.ban.killaura.name", "KillAura");
        lang.set("sanction.ban.killaura.duration", "Perm");
        return lang;
    }

    @Test
    public void categoriesAreMuteWarnBanOrderFromLang() {
        assertEquals(Arrays.asList("Mute", "Warn", "Ban"), SanctionScreen.categories(lang()));
    }

    @Test
    public void missingCategoryIsExcluded() {
        YamlConfiguration lang = new YamlConfiguration();
        lang.set("sanction.mute.spam.name", "Spam");
        lang.set("sanction.mute.spam.duration", "1h");
        lang.set("sanction.ban.killaura.name", "KillAura");
        lang.set("sanction.ban.killaura.duration", "Perm");
        assertEquals(Arrays.asList("Mute", "Ban"), SanctionScreen.categories(lang));
    }

    @Test
    public void reasonsIterateLangKeysInOrder() {
        assertEquals(Arrays.asList("spam", "flood"), SanctionScreen.reasons(lang(), "Mute"));
        assertEquals(Collections.singletonList("killaura"), SanctionScreen.reasons(lang(), "Ban"));
        assertEquals(Collections.emptyList(), SanctionScreen.reasons(lang(), "Inconnu"));
    }

    @Test
    public void reasonFieldsReadOnly() {
        assertEquals("Spam", SanctionScreen.reasonName(lang(), "Mute", "spam"));
        assertEquals("1h", SanctionScreen.reasonDuration(lang(), "Mute", "spam"));
    }

    @Test
    public void categoryPermMapping() {
        assertEquals("guard.ss_mute", SanctionScreen.categoryPerm("Mute"));
        assertEquals("guard.ss_warn", SanctionScreen.categoryPerm("Warn"));
        assertEquals("guard.ss_ban", SanctionScreen.categoryPerm("Ban"));
    }
}
