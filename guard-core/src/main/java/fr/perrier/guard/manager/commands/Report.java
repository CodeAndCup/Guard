package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.commands.annotations.Command;
import fr.perrier.guard.commands.annotations.Param;
import fr.perrier.guard.manager.menu.reports.ReportReasonMenu;
import fr.perrier.guard.reports.ReportCache;
import fr.perrier.guard.reports.ReportCooldown;
import fr.perrier.guard.reports.ReportFlash;
import fr.perrier.guard.reports.ReportGuard;
import fr.perrier.guard.reports.ReportStores;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.Messages;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * /report : anti-abus (ReportGuard), persistance via ReportStores.get(), flash multi-serveurs.
 * La soumission est extraite en {@link #submit} pour être réutilisée par la GUI (choix de raison).
 */
public class Report {

    /** Seuils anti-abus lus depuis la config (porteur immutable attendu par ReportGuard). */
    private static ReportGuard.GuardSettings settings() {
        return new ReportGuard.GuardSettings(
                Guard.getDefaultConfig().getInt("reports.cooldown-seconds", 30),
                Guard.getDefaultConfig().getInt("reports.max-open-per-reporter", 5),
                Guard.getDefaultConfig().getInt("reports.target-cooldown-seconds", 300),
                Guard.getDefaultConfig().getBoolean("reports.require-online-target", true));
    }

    /** Contexte du garde : rapports du signalant + rapports visant la cible. */
    private static java.util.List<fr.perrier.guard.reports.Report> guardContext(String reporterUuid, String targetUuid) {
        java.util.List<fr.perrier.guard.reports.Report> context = new java.util.ArrayList<>();
        for (fr.perrier.guard.reports.Report r : ReportCache.allSnapshot()) {
            String reporter = r.getReporterUuid() != null ? r.getReporterUuid() : r.getReporter();
            String reported = r.getReportedUuid() != null ? r.getReportedUuid() : r.getReported();
            if (reporterUuid.equals(reporter) || targetUuid.equals(reported)) {
                context.add(r);
            }
        }
        return context;
    }

    @Command(names = CommandsUtils.REPORT, perm = "")
    public static void onReport(Player reporter, @Param(name = "target", tabCompleteFlags = "players") String starget,
                                @Param(name = "raison", wildcard = true, baseValue = "\u00A7") String reason) {
        if (!Guard.getDefaultConfig().getBoolean("commands.report.active", true)) {
            Messages.send(reporter, Messages.COMMANDDISABLE);
            return;
        }
        if (!Guard.getDefaultConfig().getBoolean("reports.enable", true)) {
            return;
        }
        if (starget == null || starget.isEmpty()) {
            reporter.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString("commands.report.usage", "&c/report <joueur> <raison>"))));
            return;
        }
        boolean freetext = !"\u00A7".equals(reason) && reason != null && !reason.isEmpty();

        if (!freetext) {
            // pas de raison tapée : GUI de choix sauf si le mode est FREETEXT (libre obligatoire)
            String mode = Guard.getDefaultConfig().getString("reports.reason-mode", "BOTH");
            if (!"FREETEXT".equalsIgnoreCase(mode)) {
                new ReportReasonMenu(reporter, starget).openMenu(reporter);
                return;
            }
            reporter.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString("commands.report.usage", "&c/report <joueur> <raison>"))));
            return;
        }

        Player target = Bukkit.getPlayerExact(starget);
        if (target == null) {
            Messages.send(reporter, Messages.PLAYERNOTFOUND);
            return;
        }
        String refusal = ReportGuard.check(reporter.getName(), target.getName(),
                reporter.getUniqueId().toString(), target.getUniqueId().toString(),
                true, target.hasPermission("guard.reports.immune"),
                System.currentTimeMillis(), guardContext(reporter.getUniqueId().toString(),
                        target.getUniqueId().toString()), settings());
        if (refusal != null) {
            reporter.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString(refusal, "&cVous ne pouvez pas signaler ce joueur."))));
            return;
        }
        if (reason.length() > 255) {
            reason = reason.substring(0, 255); // colonne reason VARCHAR(255)
        }
        submit(reporter, target, reason);
    }

    /**
     * Point unique de soumission : anti-abus puis persistance, flash, cache.
     * Appelé par la commande ET par l'écran de confirmation de la GUI.
     */
    public static void submit(Player reporter, Player target, String reason) {
        String refusal = ReportGuard.check(reporter.getName(), target.getName(),
                reporter.getUniqueId().toString(), target.getUniqueId().toString(),
                true, target.hasPermission("guard.reports.immune"),
                System.currentTimeMillis(), guardContext(reporter.getUniqueId().toString(),
                        target.getUniqueId().toString()), settings());
        if (refusal != null) {
            reporter.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString(refusal, "&cVous ne pouvez pas signaler ce joueur."))));
            return;
        }
        fr.perrier.guard.reports.Report report = new fr.perrier.guard.reports.Report(null, reporter.getName(), target.getName(), reason,
                Guard.getDefaultConfig().getString("network.server-name", "Serveur"),
                System.currentTimeMillis(), fr.perrier.guard.reports.Report.STATUS_OPEN, null);
        report.setReporterUuid(reporter.getUniqueId().toString());
        report.setReportedUuid(target.getUniqueId().toString());
        writeLocation(report, target);
        ReportStores.get().submit(report).whenComplete((saved, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                reporter.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                        .getString("commands.report.failed", "&cÉchec de l'envoi du report — voir la console du serveur."))));
                return;
            }
            ReportCache.refreshAsync(); // le menu Reports le verra sans rouvrir
            reporter.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString("commands.report.sent", "&aReport #%id% envoyé.")
                    .replace("%id%", String.valueOf(saved.getId())))));
            // flash best-effort vers les autres instances (source de vérité = store)
            ReportFlash.broadcast(ReportFlash.KIND_REPORT,
                    "#" + saved.getId() + " " + saved.getReporter() + "->" + saved.getReported() + ": " + reason);
        });
    }

    private static void writeLocation(fr.perrier.guard.reports.Report report, Player target) {
        if (!Guard.getDefaultConfig().getBoolean("reports.save-location", true)) {
            return;
        }
        report.setLocationWorld(target.getWorld().getName());
        report.setLocationX(target.getLocation().getX());
        report.setLocationY(target.getLocation().getY());
        report.setLocationZ(target.getLocation().getZ());
        report.setLocationYaw(target.getLocation().getYaw());
        report.setLocationPitch(target.getLocation().getPitch());
    }
}
