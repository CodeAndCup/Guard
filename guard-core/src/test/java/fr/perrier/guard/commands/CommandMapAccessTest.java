package fr.perrier.guard.commands;

import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Régression Paper mojmap : getOBCClass("CraftServer") construisait un nom de paquet devine
 * (ClassNotFoundException sur Paper 1.20+). On lit la classe REELLE du serveur : champ
 * "commandMap" (1.8 → moderne), repli methode publique.
 */
public class CommandMapAccessTest {

    /** SimpleCommandMap s'instancie avec un Server (interface) proxyfie — zero serveur reel. */
    private static SimpleCommandMap newMap() {
        Server fakeServer = (Server) Proxy.newProxyInstance(
                CommandMapAccessTest.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> null);
        return new SimpleCommandMap(fakeServer);
    }

    static class ServerWithFieldOnly {
        private final SimpleCommandMap commandMap;

        ServerWithFieldOnly(SimpleCommandMap commandMap) {
            this.commandMap = commandMap;
        }
    }

    static class ServerWithMethodOnly {
        private final SimpleCommandMap inner;

        ServerWithMethodOnly(SimpleCommandMap inner) {
            this.inner = inner;
        }

        public SimpleCommandMap getCommandMap() {
            return inner;
        }
    }

    static class ServerWithoutAnything {
    }

    @Test
    public void readsPrivateFieldPath() {
        SimpleCommandMap map = newMap();
        ServerWithFieldOnly server = new ServerWithFieldOnly(map);
        // chemin historique CraftServer : champ prive "commandMap"
        assertSame(map, CommandHandler.readCommandMap(server));
    }

    @Test
    public void fallsBackToPublicMethod() {
        SimpleCommandMap map = newMap();
        ServerWithMethodOnly server = new ServerWithMethodOnly(map);
        // pas de champ "commandMap" : la methode publique prend le relais
        assertSame(map, CommandHandler.readCommandMap(server));
    }

    @Test
    public void neitherFieldNorMethodFailsLoudly() {
        ServerWithoutAnything server = new ServerWithoutAnything();
        assertThrows(IllegalStateException.class, () -> CommandHandler.readCommandMap(server));
    }
}
