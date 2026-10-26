package fr.perrier.guard.reports;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Fallback mono-serveur (database.enable=false) : un reports.yml local.
 *  Chaque tâche est isolée : une exception (fichier corrompu...) complète la future, jamais de task qui pend. */
public class YamlReportStore implements ReportStore {

    private final File file;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Guard-Reports-YAML");
        t.setDaemon(true);
        return t;
    });

    public YamlReportStore(File file) {
        this.file = file;
    }

    @Override
    public CompletableFuture<Report> submit(Report report) {
        CompletableFuture<Report> done = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                YamlConfiguration yml = load();
                String id = UUID.randomUUID().toString();
                report.setId(id);
                write(yml, "reports." + id + ".", report);
                save(yml);
                done.complete(report);
            } catch (RuntimeException e) {
                e.printStackTrace();
                done.completeExceptionally(e);
            }
        });
        return done;
    }

    @Override
    public CompletableFuture<List<Report>> listAll() {
        CompletableFuture<List<Report>> done = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                List<Report> out = new ArrayList<>();
                YamlConfiguration yml = load();
                ConfigurationSection all = yml.getConfigurationSection("reports");
                if (all != null) {
                    for (String key : all.getKeys(false)) {
                        out.add(read(yml, "reports." + key + ".", key));
                    }
                }
                out.sort(Comparator.comparingLong(Report::getCreatedAt).reversed());
                done.complete(out);
            } catch (RuntimeException e) {
                e.printStackTrace();
                done.completeExceptionally(e);
            }
        });
        return done;
    }

    @Override
    public CompletableFuture<Void> resolve(String id, String by, String outcome) {
        return update(id, report -> {
            report.setStatus(Report.STATUS_CLOSED);
            report.setOutcome(outcome);
            report.setResolvedBy(by);
            report.setResolvedAt(System.currentTimeMillis());
        });
    }

    @Override
    public CompletableFuture<Void> setStatus(String id, String status, String by) {
        return update(id, report -> {
            report.setStatus(status);
            report.setResolvedBy(by);
            if (!Report.STATUS_CLOSED.equals(status)) {
                // réouverture / suppression : le verdict ne s'applique plus
                report.setOutcome(null);
                report.setResolvedAt(0);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setHandler(String id, String name, String uuid) {
        return update(id, report -> {
            report.setHandler(name);
            report.setHandlerUuid(uuid);
        });
    }

    /** Supprime (TAG) ou purge les rapports CLOSED plus vieux que la rétention. Reprend le nom du dossier. */
    @Override
    public CompletableFuture<Integer> clearClosed(int retentionDays) {
        CompletableFuture<Integer> done = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                YamlConfiguration yml = load();
                ConfigurationSection all = yml.getConfigurationSection("reports");
                int cleared = 0;
                if (all != null) {
                    long limit = System.currentTimeMillis() - retentionDays * 86_400_000L;
                    for (String key : new ArrayList<>(all.getKeys(false))) {
                        Report r = read(yml, "reports." + key + ".", key);
                        long stamp = r.getResolvedAt() > 0 ? r.getResolvedAt() : r.getCreatedAt();
                        if (Report.STATUS_CLOSED.equals(r.getStatus()) && stamp < limit) {
                            yml.set("reports." + key, null);
                            cleared++;
                        }
                    }
                }
                save(yml);
                done.complete(cleared);
            } catch (RuntimeException e) {
                e.printStackTrace();
                done.completeExceptionally(e);
            }
        });
        return done;
    }

    private CompletableFuture<Void> update(String id, java.util.function.Consumer<Report> change) {
        CompletableFuture<Void> done = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                YamlConfiguration yml = load();
                if (yml.getConfigurationSection("reports." + id) == null) {
                    done.completeExceptionally(new IllegalArgumentException("report inconnu: " + id));
                    return;
                }
                Report report = read(yml, "reports." + id + ".", id);
                change.accept(report);
                write(yml, "reports." + id + ".", report);
                save(yml);
                done.complete(null);
            } catch (RuntimeException e) {
                e.printStackTrace();
                done.completeExceptionally(e);
            }
        });
        return done;
    }

    private void write(YamlConfiguration yml, String p, Report report) {
        yml.set(p + "reporter", report.getReporter());
        yml.set(p + "reported", report.getReported());
        yml.set(p + "reason", report.getReason());
        yml.set(p + "server", report.getServerName());
        yml.set(p + "created", report.getCreatedAt());
        yml.set(p + "status", report.getStatus());
        yml.set(p + "resolved_by", report.getResolvedBy());
        yml.set(p + "outcome", report.getOutcome());
        yml.set(p + "handler", report.getHandler());
        yml.set(p + "handler_uuid", report.getHandlerUuid());
        yml.set(p + "resolved_at", report.getResolvedAt());
        yml.set(p + "location_world", report.getLocationWorld());
        yml.set(p + "location_x", report.getLocationX());
        yml.set(p + "location_y", report.getLocationY());
        yml.set(p + "location_z", report.getLocationZ());
        yml.set(p + "location_yaw", report.getLocationYaw());
        yml.set(p + "location_pitch", report.getLocationPitch());
    }

    private Report read(YamlConfiguration yml, String p, String id) {
        Report report = new Report(id,
                yml.getString(p + "reporter"),
                yml.getString(p + "reported"),
                yml.getString(p + "reason"),
                yml.getString(p + "server"),
                yml.getLong(p + "created"),
                yml.getString(p + "status"),
                yml.getString(p + "resolved_by"));
        report.setOutcome(yml.getString(p + "outcome"));
        report.setHandler(yml.getString(p + "handler"));
        report.setHandlerUuid(yml.getString(p + "handler_uuid"));
        report.setResolvedAt(yml.getLong(p + "resolved_at"));
        report.setLocationWorld(yml.getString(p + "location_world"));
        report.setLocationX(yml.getDouble(p + "location_x"));
        report.setLocationY(yml.getDouble(p + "location_y"));
        report.setLocationZ(yml.getDouble(p + "location_z"));
        report.setLocationYaw((float) yml.getDouble(p + "location_yaw"));
        report.setLocationPitch((float) yml.getDouble(p + "location_pitch"));
        return report;
    }

    private YamlConfiguration load() {
        return YamlConfiguration.loadConfiguration(file);
    }

    private void save(YamlConfiguration yml) {
        try {
            yml.save(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
