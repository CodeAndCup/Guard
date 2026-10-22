package fr.perrier.guard.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.perrier.guard.Guard;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Couche persistance au-dessus d'un pool HikariCP.
 * Aucune méthode publique ne touche une connection de façon bloquante : tout travail SQL est sur l'exécuteur interne.
 */
public final class DatabaseManager {

    private static final String DDL_VANISH =
            "CREATE TABLE IF NOT EXISTS player_vanish (uuid VARCHAR(36) not null, vanish BOOLEAN not null)";

    private final HikariDataSource dataSource;
    private final ExecutorService executor;

    private DatabaseManager(HikariDataSource dataSource) {
        this.dataSource = dataSource;
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Guard-DB");
            t.setDaemon(true);
            return t;
        });
        executor.execute(() -> {
            try (java.sql.Connection c = dataSource.getConnection(); java.sql.Statement st = c.createStatement()) {
                st.execute(DDL_VANISH);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static boolean isEnabled() {
        return Guard.getDefaultConfig().getBoolean("database.enable");
    }

    /** Instance partagée par le boot ; un seul pool pour tout le plugin. */
    public static DatabaseManager create() {
        return new DatabaseManager(new HikariDataSource(buildHikariConfig(Guard.getDefaultConfig())));
    }

    /** null si la DB est désactivée/absente : le plugin doit démarrer sans pool. */
    static HikariConfig buildHikariConfig(FileConfiguration config) {
        if (config == null || !config.getBoolean("database.enable", false)) {
            return null;
        }
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(jdbcUrl(
                config.getString("database.host", "localhost"),
                config.getInt("database.port", 3306),
                config.getString("database.name", "minecraft")));
        hc.setUsername(config.getString("database.user", "root"));
        hc.setPassword(config.getString("database.password", "root"));
        hc.setPoolName("Guard-DB");
        hc.setMaximumPoolSize(5);
        hc.setInitializationFailTimeout(-1);
        return hc;
    }

    /** Format historique du plugin, gardé tel quel. */
    static String jdbcUrl(String host, int port, String db) {
        return "jdbc:mysql://" + host + ":" + port + "/" + db + "?characterEncoding=utf8";
    }

    public CompletableFuture<Void> toggleVanish(UUID uuid, boolean vanish) {
        CompletableFuture<Void> done = new CompletableFuture<>();
        executor.execute(() -> {
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO player_vanish (uuid, vanish) VALUES (?,?) " +
                         "ON DUPLICATE KEY UPDATE vanish=VALUES(vanish)")) {
                ps.setString(1, uuid.toString());
                ps.setBoolean(2, vanish);
                ps.executeUpdate();
                done.complete(null);
            } catch (SQLException e) {
                e.printStackTrace();
                done.completeExceptionally(e);
            }
        });
        return done;
    }

    /** État au boot ; crée la ligne si absente (comportement historique de PlayerJoin). */
    public CompletableFuture<Boolean> getVanish(UUID uuid) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        executor.execute(() -> {
            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT vanish FROM player_vanish WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        result.complete(rs.getBoolean(1));
                    } else {
                        try (PreparedStatement insert = c.prepareStatement("INSERT INTO player_vanish VALUES (?,?)")) {
                            insert.setString(1, uuid.toString());
                            insert.setBoolean(2, false);
                            insert.executeUpdate();
                        }
                        result.complete(false);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                result.complete(false);
            }
        });
        return result;
    }

    public void close() {
        executor.shutdown();
        dataSource.close();
    }
}