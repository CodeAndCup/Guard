package fr.perrier.guard.reports;

import fr.perrier.guard.Guard;
import fr.perrier.guard.sql.DatabaseManager;

import java.io.File;

/** Choix du store au boot : MySQL si la DB est active, sinon YAML local. */
public final class ReportStores {

    private ReportStores() {
    }

    /** Instance memoisée (le store YAML ne doit être créé qu'une fois : il détient son exécuteur). */
    private static volatile ReportStore instance;

    public static ReportStore get() {
        if (instance == null) {
            synchronized (ReportStores.class) {
                if (instance == null) {
                    instance = create();
                }
            }
        }
        return instance;
    }

    public static ReportStore create() {
        ReportStore store;
        if (DatabaseManager.isEnabled() && Guard.getInstance().getDbManager() != null) {
            store = new MySqlReportStore(Guard.getInstance().getDbManager());
        } else {
            store = new YamlReportStore(new File(Guard.getInstance().getDataFolder(), "reports.yml"));
        }
        Guard.getInstance().getLogger().info("[Reports] store: " + store.getClass().getSimpleName());
        return store;
    }
}
