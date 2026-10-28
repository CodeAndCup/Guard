package fr.perrier.guard.sql;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/** Pool configuré sans réseau : builders purs, aucun objet de connexion n'est ouvert ici. */
public class DatabaseManagerTest {

    @Test
    public void noPoolWhenDatabaseDisabled() {
        YamlConfiguration off = new YamlConfiguration();
        off.set("database.enable", false);
        assertNull(DatabaseManager.buildHikariConfig(off));
    }

    @Test
    public void noPoolWhenConfigMissing() {
        assertNull(DatabaseManager.buildHikariConfig(new YamlConfiguration()));
    }

    @Test
    public void poolBuiltWhenDatabaseEnabled() {
        YamlConfiguration on = new YamlConfiguration();
        on.set("database.enable", true);
        on.set("database.host", "localhost");
        on.set("database.port", 3306);
        on.set("database.name", "minecraft");
        on.set("database.user", "root");
        on.set("database.password", "root");
        assertNotNull(DatabaseManager.buildHikariConfig(on));
    }

    @Test
    public void jdbcUrlMatchesLegacyFormat() {
        assertEquals("jdbc:mysql://localhost:3306/minecraft?characterEncoding=utf8",
                DatabaseManager.jdbcUrl("localhost", 3306, "minecraft"));
    }
}