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

    private static final String DDL_REPORTS =
            "CREATE TABLE IF NOT EXISTS reports (id VARCHAR(36) PRIMARY KEY, reporter VARCHAR(16) NOT NULL, " +
            "reporter_uuid VARCHAR(36), reported VARCHAR(16) NOT NULL, reported_uuid VARCHAR(36), " +
            "reason VARCHAR(255) NOT NULL, server_name VARCHAR(64) NOT NULL, " +
            "created BIGINT NOT NULL, status VARCHAR(16) NOT NULL, resolved_by VARCHAR(16), " +
            "outcome VARCHAR(16), handler VARCHAR(16), handler_uuid VARCHAR(36), resolved_at BIGINT, " +
            "location_world VARCHAR(64), location_x DOUBLE, location_y DOUBLE, location_z DOUBLE, " +
            "location_yaw FLOAT, location_pitch FLOAT)";

    /** Colonnes ajoutées par les signalements v2 : ALTER best-effort (silencieux si déjà là). */
    private static final String[] REPORTS_V2_COLUMNS = {
            "ALTER TABLE reports ADD COLUMN reporter_uuid VARCHAR(36)",
            "ALTER TABLE reports ADD COLUMN reported_uuid VARCHAR(36)",
            "ALTER TABLE reports ADD COLUMN outcome VARCHAR(16)",
            "ALTER TABLE reports ADD COLUMN handler VARCHAR(16)",
            "ALTER TABLE reports ADD COLUMN handler_uuid VARCHAR(36)",
            "ALTER TABLE reports ADD COLUMN resolved_at BIGINT",
            "ALTER TABLE reports ADD COLUMN location_world VARCHAR(64)",
            "ALTER TABLE reports ADD COLUMN location_x DOUBLE",
            "ALTER TABLE reports ADD COLUMN location_y DOUBLE",
            "ALTER TABLE reports ADD COLUMN location_z DOUBLE",
            "ALTER TABLE reports ADD COLUMN location_yaw FLOAT",
            "ALTER TABLE reports ADD COLUMN location_pitch FLOAT",
    };

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
                st.execute(DDL_REPORTS);
                // colonnes v2 sur une table préexistante : on tente, on ignore l'échec (déjà là)
                for (String ddl : REPORTS_V2_COLUMNS) {
                    try {
                        st.execute(ddl);
                    } catch (SQLException ignored) {
                    }
                }
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

    /** Travail SQL arbitraire sur le pool/exécuteur internes ; exception → future exceptionnelle, jamais crash main thread. */
    public <T> CompletableFuture<T> query(java.util.function.Function<Connection, T> work) {
        CompletableFuture<T> result = new CompletableFuture<>();
        executor.execute(() -> {
            try (Connection c = dataSource.getConnection()) {
                result.complete(work.apply(c));
            } catch (Throwable t) {
                t.printStackTrace();
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    public CompletableFuture<Void> toggleVanish(UUID uuid, boolean vanish) {
        CompletableFuture<Void> done = new CompletableFuture<>();
        executor.execute(() -> {
            try (Connection c = dataSource.getConnection()) {
                // pas de clé unique sur uuid (schéma historique conservé) : ON DUPLICATE KEY ne
                // se déclencherait jamais → UPDATE puis INSERT si aucune ligne touchée
                try (PreparedStatement ps = c.prepareStatement("UPDATE player_vanish SET vanish=? WHERE uuid=?")) {
                    ps.setBoolean(1, vanish);
                    ps.setString(2, uuid.toString());
                    if (ps.executeUpdate() > 0) {
                        done.complete(null);
                    }
                }
                if (!done.isDone()) {
                    try (PreparedStatement ins = c.prepareStatement("INSERT INTO player_vanish (uuid, vanish) VALUES (?,?)")) {
                        ins.setString(1, uuid.toString());
                        ins.setBoolean(2, vanish);
                        ins.executeUpdate();
                        done.complete(null);
                    }
                }
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
        // on laisse l'exécuteur finir ses tâches avant de couper le pool, sinon un toggle en vol est coupé à onDisable
        executor.shutdown();
        try {
            executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        dataSource.close();
    }
}