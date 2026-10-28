package fr.perrier.guard.utils;

import com.cryptomorin.xseries.messages.*;
import org.bukkit.*;
import org.bukkit.entity.*;

@SuppressWarnings("all")
public class Title {
    /**
     * Permet de send un title sans sub title au joueur souhaité
     * @param player
     * @param fadeIn en tick
     * @param stay en tick
     * @param fadeOut en tick
     * @param message
     */
    public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String message) {
        sendTitle(player, fadeIn, stay, fadeOut, message, "");
    }

    /**
     * Permet de send une actionbar au joueur souhaité
     * @param p
     * @param msg
     */
    public static void sendActionBar(Player p, String msg) {

        ActionBar.sendActionBar(p,msg);
    }

    /**
     * Permet d'envoier une anction bar au joueur connecté sur le serveur
     * @param msg
     */
    public static void sendActionBar(String msg) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendActionBar(p, msg);
        }
    }

    /**
     * Permet d'envoyer un subtitle sans title au joueur souhaité
     * @param player
     * @param fadeIn en tick
     * @param stay en tick
     * @param fadeOut en tick
     * @param message
     */
    public static void sendSubtitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String message) {
        sendTitle(player, fadeIn, stay, fadeOut, "", message);
    }

    /**
     * Permet d'envoyer un title (title + subtitle) au joueur souhaité
     * @param player
     * @param fadeIn en tick
     * @param stay en tick
     * @param fadeOut en tick
     * @param title
     * @param subtitle
     */
    public static void sendFullTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
        sendTitle(player, fadeIn, stay, fadeOut, title, subtitle);
    }

    /**
     * Permet d'envoyer le packet que l'on veut à un joueur
     * @param player
     * @param packet
     */
    public static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle", new Class[0]).invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", new Class[]{getNMSClass("Packet")}).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Permet d'avoir la bonne version de class NMS souhaité suivant le serveur
     * @param name
     * @return
     */
    public static Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Constructeur des envoies de title (passer par sendSubTitle sendTitle et sendFullTitle
     * @param player
     * @param fadeIn en tick
     * @param stay en tick
     * @param fadeOut en tick
     * @param title
     * @param subtitle
     */
    public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
        title = ChatUtil.translate(title);
        subtitle = ChatUtil.translate(subtitle);
        Titles.sendTitle(player, (title != null ? title : " "), (subtitle != null ? subtitle : " "));
    }

    /**
     * Permet d'effacer le title que peut avoir un joueur
     * @param player
     */
    public static void clearTitle(Player player) {
        Titles.clearTitle(player);
    }

    /**
     * Permet d'envoyer un tab à un joueur souhaité
     * @param player
     * @param header
     * @param footer
     */
    public static void sendTabTitle(Player player, String header, String footer) {
        Titles.sendTabList(header,footer,player);
    }
}