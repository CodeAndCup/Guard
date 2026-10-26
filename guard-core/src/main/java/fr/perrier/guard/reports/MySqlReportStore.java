package fr.perrier.guard.reports;

import fr.perrier.guard.sql.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Store principal (database.enable=true) : opère sur le pool DatabaseManager via query(). */
public class MySqlReportStore implements ReportStore {

    private final DatabaseManager db;

    public MySqlReportStore(DatabaseManager db) {
        this.db = db;
    }

    private static final String COLUMNS =
            "id, reporter, reported, reason, server_name, created, status, resolved_by, "
            + "outcome, handler, handler_uuid, resolved_at, location_world, location_x, location_y, "
            + "location_z, location_yaw, location_pitch";

    @Override
    public CompletableFuture<Report> submit(Report report) {
        report.setId(UUID.randomUUID().toString());
        return db.query(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO reports (" + COLUMNS + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                bind(ps, report);
                if (ps.executeUpdate() == 0) {
                    throw new IllegalStateException("insert report: aucune ligne insérée");
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return report;
        });
    }

    @Override
    public CompletableFuture<List<Report>> listAll() {
        return db.query(c -> {
            List<Report> out = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT " + COLUMNS + " FROM reports ORDER BY created DESC LIMIT 500");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(read(rs));
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return out;
        });
    }

    @Override
    public CompletableFuture<Void> resolve(String id, String by, String outcome) {
        return db.query(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE reports SET status = ?, outcome = ?, resolved_by = ?, resolved_at = ? WHERE id = ?")) {
                ps.setString(1, Report.STATUS_CLOSED);
                ps.setString(2, outcome);
                ps.setString(3, by);
                ps.setLong(4, System.currentTimeMillis());
                ps.setString(5, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> setStatus(String id, String status, String by) {
        return db.query(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE reports SET status = ?, resolved_by = ?, "
                    + "outcome = IF(? = 'CLOSED', outcome, NULL), resolved_at = IF(? = 'CLOSED', resolved_at, 0) WHERE id = ?")) {
                ps.setString(1, status);
                ps.setString(2, by);
                ps.setString(3, status);
                ps.setString(4, status);
                ps.setString(5, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> setHandler(String id, String name, String uuid) {
        return db.query(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE reports SET handler = ?, handler_uuid = ? WHERE id = ?")) {
                ps.setString(1, name);
                ps.setString(2, uuid);
                ps.setString(3, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Integer> clearClosed(int retentionDays) {
        return db.query(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE reports SET status = ? WHERE status = ? "
                    + "AND (CASE WHEN resolved_at > 0 THEN resolved_at ELSE created END) < ?")) {
                ps.setString(1, Report.STATUS_DELETED);
                ps.setString(2, Report.STATUS_CLOSED);
                ps.setLong(3, System.currentTimeMillis() - retentionDays * 86_400_000L);
                return ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private static void bind(PreparedStatement ps, Report r) throws SQLException {
        ps.setString(1, r.getId());
        ps.setString(2, r.getReporter());
        ps.setString(3, r.getReported());
        ps.setString(4, r.getReason());
        ps.setString(5, r.getServerName());
        ps.setLong(6, r.getCreatedAt());
        ps.setString(7, r.getStatus());
        ps.setString(8, r.getResolvedBy());
        ps.setString(9, r.getOutcome());
        ps.setString(10, r.getHandler());
        ps.setString(11, r.getHandlerUuid());
        ps.setLong(12, r.getResolvedAt());
        ps.setString(13, r.getLocationWorld());
        ps.setDouble(14, r.getLocationX());
        ps.setDouble(15, r.getLocationY());
        ps.setDouble(16, r.getLocationZ());
        ps.setFloat(17, r.getLocationYaw());
        ps.setFloat(18, r.getLocationPitch());
    }

    private static Report read(ResultSet rs) throws SQLException {
        Report r = new Report(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                rs.getString(5), rs.getLong(6), rs.getString(7), rs.getString(8));
        r.setOutcome(rs.getString(9));
        r.setHandler(rs.getString(10));
        r.setHandlerUuid(rs.getString(11));
        r.setResolvedAt(rs.getLong(12));
        r.setLocationWorld(rs.getString(13));
        r.setLocationX(rs.getDouble(14));
        r.setLocationY(rs.getDouble(15));
        r.setLocationZ(rs.getDouble(16));
        r.setLocationYaw(rs.getFloat(17));
        r.setLocationPitch(rs.getFloat(18));
        return r;
    }
}
