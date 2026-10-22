package fr.perrier.guard.manager.commands;

import fr.perrier.guard.Guard;
import fr.perrier.guard.commands.annotations.Command;
import fr.perrier.guard.commands.annotations.Param;
import fr.perrier.guard.reports.ReportCooldown;
import fr.perrier.guard.reports.ReportFlash;
import fr.perrier.guard.reports.ReportStores;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.Messages;
import fr.perrier.guard.utils.commands.CommandsUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** /report : cooldown par joueur, persistance via ReportStores.get(), flash multi-serveurs via ReportFlash. */
public class Report {

    @Command(names = CommandsUtils.REPORT, perm = "")
    public static void onReport(Player reporter, @Param(name = "target") String starget,
                                @Param(name = "raison", wildcard = true, baseValue = "\u00A7") String reason) {
        if (!Guard.getDefaultConfig().getBoolean("commands.report.active", true)) {
            Messages.send(reporter, Messages.COMMANDDISABLE);
            return;
        }
        if (!Guard.getDefaultConfig().getBoolean("reports.enable", true)) {
            return;
        }
        if ("\u00A7".equals(reason) || starget == null || starget.isEmpty() || reason == null || reason.isEmpty()) {
            reporter.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString("commands.report.usage", "&c/report <joueur> <raison>"))));
            return;
        }
        if (reason.length() > 255) {
            // colonne reports.reason VARCHAR(255) : une raison trop longue tuerait le submit (SQL strict)
            reason = reason.substring(0, 255);
        }
        final String safeReason = reason;
        long cooldownSeconds = Guard.getDefaultConfig().getLong("reports.cooldown-seconds", 60) * 1000L;
        long left = ReportCooldown.cooldownLeft(reporter.getUniqueId(), System.currentTimeMillis(), cooldownSeconds);
        if (left > 0) {
            reporter.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString("commands.report.cooldown", "&cAttendez %seconds% s avant un nouveau report.")
                    .replace("%seconds%", String.valueOf(left / 1000 + 1)))));
            return;
        }
        Player target = Bukkit.getPlayerExact(starget);
        if (target == null) {
            Messages.send(reporter, Messages.PLAYERNOTFOUND);
            return;
        }
        fr.perrier.guard.reports.Report report = new fr.perrier.guard.reports.Report(0, reporter.getName(), target.getName(), safeReason,
                Guard.getDefaultConfig().getString("network.server-name", "Serveur"),
                System.currentTimeMillis(), fr.perrier.guard.reports.Report.STATUS_OPEN, null);
        ReportStores.get().submit(report).thenAccept(saved -> {
            ReportCooldown.remember(reporter.getUniqueId(), System.currentTimeMillis());
            reporter.sendMessage(ChatUtil.prefix(ChatUtil.translate(Guard.getLangConfig()
                    .getString("commands.report.sent", "&aReport #%id% envoyé.")
                    .replace("%id%", String.valueOf(saved.getId())))));
            // flash best-effort vers les autres instances (source de vérité = store)
            ReportFlash.broadcast(ReportFlash.KIND_REPORT,
                    "#" + saved.getId() + " " + saved.getReporter() + "->" + saved.getReported() + ": " + safeReason);
        });
    }
}
