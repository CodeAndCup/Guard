package fr.perrier.guard.reports;

import fr.perrier.guard.Guard;
import fr.perrier.guard.sql.DatabaseManager;

import java.io.File;

/** Choix du store au boot : MySQL si la DB est active, sinon YAML local. */
public final class ReportStores {

    private ReportStores() {
    }

    public static ReportStore create() {
        if (DatabaseManager.isEnabled() && Guard.getInstance().getDbManager() != null) {
            return new MySqlReportStore(Guard.getInstance().getDbManager());
        }
        return new YamlReportStore(new File(Guard.getInstance().getDataFolder(), "reports.yml"));
    }
}
