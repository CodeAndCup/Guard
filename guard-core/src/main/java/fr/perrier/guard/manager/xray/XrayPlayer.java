package fr.perrier.guard.manager.xray;

import fr.perrier.guard.Guard;
import fr.perrier.guard.reports.ReportFlash;
import fr.perrier.guard.utils.ChatUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;

@Getter
public class XrayPlayer {

    public static HashMap<Player,XrayPlayer> list = new HashMap<Player,XrayPlayer>();

    Player player;
    int diamond;
    int emerald;
    int gold;
    int iron;
    int restone;
    int lapis;
    int coal;
    int stone;
    boolean diamondAlertSent;

    public XrayPlayer(Player player) {
        this.player = player;
        this.diamond = 0;
        this.emerald = 0;
        this.gold = 0;
        this.iron = 0;
        this.restone = 0;
        this.lapis = 0;
        this.coal = 0;
        this.stone = 0;

        list.put(player, this);
    }

    @SuppressWarnings("incomplete-switch")
    public void update(Material material) {
        switch (material) {
            case DIAMOND_ORE:
                diamond++;
                checkDiamondAlert();
                break;
            case EMERALD_ORE:
                emerald++;
                break;
            case GOLD_ORE:
                gold++;
                break;
            case IRON_ORE:
                iron++;
                break;
            case REDSTONE_ORE:
                restone++;
                break;
            case LAPIS_ORE:
                lapis++;
                break;
            case COAL_ORE:
                coal++;
                break;
            case STONE:
            case ANDESITE:
            case DIORITE:
            case GRANITE:
                stone++;
                break;
        }
        list.put(player, this);
    }

    /** Alerte X-Ray locale + flash cross au seuil de diamants condig.yml (commands.xray.diamond-alert), une fois par session. */
    private void checkDiamondAlert() {
        if (diamondAlertSent) {
            return;
        }
        int threshold = Guard.getDefaultConfig().getInt("commands.xray.diamond-alert", 32);
        if (threshold < 0 || diamond < threshold) {
            return;
        }
        diamondAlertSent = true;
        String text = player.getName() + ": " + diamond + " diamants minés";
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("guard.xray")) {
                staff.sendMessage(ChatUtil.translate(
                        Guard.getLangConfig().getString("commands.report.xray-flash", "&b[Guard] &f%text%")
                                .replace("%text%", text)));
            }
        }
        ReportFlash.broadcast(ReportFlash.KIND_XRAY, text);
    }
}
