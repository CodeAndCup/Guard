package fr.perrier.guard.manager.commands;

import org.junit.jupiter.api.Test;
import org.bukkit.configuration.file.YamlConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Contrat pur du staffchat : détection du message "@..." + substitution du format lang. */
public class StaffChatTest {

    @Test
    public void detectsAtPrefix() {
        assertTrue(StaffChat.isStaffChatMessage("@salut"));
        assertTrue(StaffChat.isStaffChatMessage("@ multi mots"));
        assertFalse(StaffChat.isStaffChatMessage("salut"));
        assertFalse(StaffChat.isStaffChatMessage(""));
    }

    @Test
    public void formatSubstitutesPlaceholdersAndColors() {
        YamlConfiguration lang = new YamlConfiguration();
        lang.set("commands.staffchat.format", "&8[{server_name}] &b{staff} &f: {message}");
        String out = StaffChat.format(lang, "Alex", "salut & les &codes bruts", "lobby");
        assertEquals("\u00A78[lobby] \u00A7bAlex \u00A7f: salut & les &codes bruts",
                out);
    }

    @Test
    public void stripPrefixRemovesAt() {
        assertEquals("salut", StaffChat.stripPrefix("@salut"));
        assertEquals("salut ", StaffChat.stripPrefix("@ salut "));
    }
}
