package fr.perrier.guard.reports;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Fallback mono-serveur (database.enable=false) : un reports.yml local, format minimum.
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
                int id = yml.getInt("next-id", 1);
                report.setId(id);
                yml.set("next-id", id + 1);
                String p = "reports." + id + ".";
                yml.set(p + "reporter", report.getReporter());
                yml.set(p + "reported", report.getReported());
                yml.set(p + "reason", report.getReason());
                yml.set(p + "server", report.getServerName());
                yml.set(p + "created", report.getCreatedAt());
                yml.set(p + "status", report.getStatus());
                yml.set(p + "resolved_by", report.getResolvedBy());
                if (save(yml, done)) {
                    done.complete(report);
                }
            } catch (RuntimeException e) {
                fail(done, e);
            }
        });
        return done;
    }

    @Override
    public CompletableFuture<List<Report>> listOpen() {
        CompletableFuture<List<Report>> done = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                YamlConfiguration yml = load();
                List<Report> out = new ArrayList<>();
                ConfigurationSection all = yml.getConfigurationSection("reports");
                if (all != null) {
                    for (String key : all.getKeys(false)) {
                        String p = "reports." + key + ".";
                        if (Report.STATUS_OPEN.equals(yml.getString(p + "status"))) {
                            out.add(new Report(Integer.parseInt(key),
                                    yml.getString(p + "reporter"),
                                    yml.getString(p + "reported"),
                                    yml.getString(p + "reason"),
                                    yml.getString(p + "server"),
                                    yml.getLong(p + "created"),
                                    yml.getString(p + "status"),
                                    yml.getString(p + "resolved_by")));
                        }
                    }
                }
                out.sort(Comparator.comparingLong(Report::getCreatedAt).reversed());
                done.complete(out);
            } catch (RuntimeException e) {
                fail(done, e);
            }
        });
        return done;
    }

    @Override
    public CompletableFuture<Void> resolve(int id, String by, String outcome) {
        CompletableFuture<Void> done = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                YamlConfiguration yml = load();
                if (yml.getConfigurationSection("reports." + id) == null) {
                    done.completeExceptionally(new IllegalArgumentException("report inconnu: " + id));
                    return;
                }
                String p = "reports." + id + ".";
                yml.set(p + "status", outcome);
                yml.set(p + "resolved_by", by);
                if (save(yml, done)) {
                    done.complete(null);
                }
            } catch (RuntimeException e) {
                fail(done, e);
            }
        });
        return done;
    }

    private YamlConfiguration load() {
        return YamlConfiguration.loadConfiguration(file);
    }

    private boolean save(YamlConfiguration yml, CompletableFuture<?> done) {
        try {
            yml.save(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            done.completeExceptionally(e);
            return false;
        }
    }

    private void fail(CompletableFuture<?> done, RuntimeException e) {
        e.printStackTrace();
        done.completeExceptionally(e);
    }
}
