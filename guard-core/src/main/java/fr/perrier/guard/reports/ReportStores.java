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
        if (DatabaseManager.isEnabled() && Guard.getInstance().getDbManager() != null) {
            return new MySqlReportStore(Guard.getInstance().getDbManager());
        }
        return new YamlReportStore(new File(Guard.getInstance().getDataFolder(), "reports.yml"));
    }
}
