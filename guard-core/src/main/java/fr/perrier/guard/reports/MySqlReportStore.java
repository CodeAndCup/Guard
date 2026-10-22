package fr.perrier.guard.reports;

import fr.perrier.guard.sql.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Store principal (database.enable=true) : opère sur le pool DatabaseManager via query(). */
public class MySqlReportStore implements ReportStore {

    private final DatabaseManager db;

    public MySqlReportStore(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<Report> submit(Report report) {
        return db.query(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO reports (reporter, reported, reason, server_name, created, status, resolved_by) VALUES (?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, report.getReporter());
                ps.setString(2, report.getReported());
                ps.setString(3, report.getReason());
                ps.setString(4, report.getServerName());
                ps.setLong(5, report.getCreatedAt());
                ps.setString(6, report.getStatus());
                ps.setString(7, report.getResolvedBy());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        report.setId(keys.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return report;
        });
    }

    @Override
    public CompletableFuture<List<Report>> listOpen() {
        return db.query(c -> {
            List<Report> out = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id, reporter, reported, reason, server_name, created, status, resolved_by FROM reports WHERE status = 'OPEN' ORDER BY created DESC LIMIT 100");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Report(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getString(5), rs.getLong(6), rs.getString(7), rs.getString(8)));
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return out;
        });
    }

    @Override
    public CompletableFuture<Void> resolve(int id, String by, String outcome) {
        return db.query(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE reports SET status = ?, resolved_by = ? WHERE id = ?")) {
                ps.setString(1, outcome);
                ps.setString(2, by);
                ps.setInt(3, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return null;
        });
    }
}
