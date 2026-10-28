package fr.perrier.guard.manager.xray;

import fr.perrier.guard.Guard;
import fr.perrier.guard.reports.ReportFlash;
import lombok.Getter;
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

    /** Flash cross au seuil de diamants (config commands.xray.diamond-alert), une fois par session.
     *  L'alerte locale est faite par ReportFlash.broadcast (le Forward BungeeCord n'a pas de loopback). */
    private void checkDiamondAlert() {
        if (diamondAlertSent) {
            return;
        }
        int threshold = Guard.getDefaultConfig().getInt("commands.xray.diamond-alert", 32);
        if (threshold < 0 || diamond < threshold) {
            return;
        }
        diamondAlertSent = true;
        ReportFlash.broadcast(ReportFlash.KIND_XRAY, player.getName() + ": " + diamond + " diamants minés");
    }
}
