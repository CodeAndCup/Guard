package fr.perrier.guard.utils;

import com.cryptomorin.xseries.messages.ActionBar;
import com.mysql.cj.protocol.x.XMessage;
import fr.perrier.guard.*;
import lombok.*;
import org.bukkit.entity.*;

import java.util.*;

@Getter
public enum Messages {

    PREFIX("prefix",Guard.getLangConfig().getString("prefix")),
    NOPERM("no_perm",Guard.getLangConfig().getString("no_perm")),
    PLAYERNOTFOUND("playernotfound",Guard.getLangConfig().getString("playernotfound")),
    RELOADED("reloaded",Guard.getLangConfig().getString("reloaded")),
    COMMANDDISABLE("commanddisable",Guard.getLangConfig().getString("commanddisable")),

    ITEMS_VANISHON("items.vanish_on",Guard.getLangConfig().getString("items.vanish_on")),
    ITEMS_VANISHOFF("items.vanish_off",Guard.getLangConfig().getString("items.vanish_off")),
    ITEMS_FREEZE("items.freeze",Guard.getLangConfig().getString("items.freeze")),
    ITEMS_CPS("items.cps",Guard.getLangConfig().getString("items.cps")),
    ITEMS_RANDOMTP("items.random_tp",Guard.getLangConfig().getString("items.random_tp")),
    ITEMS_SS_BOOK("items.ss_book",Guard.getLangConfig().getString("items.ss_book")),
    ITEMS_PARAMETERS("items.parameters",Guard.getLangConfig().getString("items.parameters")),
    ITEMS_CHATCOLOR("items.chat_color",Guard.getLangConfig().getString("items.chat_color")),
    ITEMS_NOCLIP("items.no_clip",Guard.getLangConfig().getString("items.no_clip")),
    ITEMS_INVENTORY("items.inventory_view",Guard.getLangConfig().getString("items.inventory_view")),

    COMMANDS_FREEZE_MISSARGS("commands.freeze.missargs",Guard.getLangConfig().getString("commands.freeze.missargs")),
    COMMANDS_FREEZE_ACTIVATED("commands.freeze.activated",Guard.getLangConfig().getString("commands.freeze.activated")),
    COMMANDS_FREEZE_DESACTIVATED("commands.freeze.desactivated",Guard.getLangConfig().getString("commands.freeze.desactivated")),
    COMMANDS_FREEZE_DISCONNECT("commands.freeze.disconnect",Guard.getLangConfig().getString("commands.freeze.disconnect")),

    COMMANDS_CPS_MISSARGS("commands.cps.missargs",Guard.getLangConfig().getString("commands.cps.missargs")),
    COMMANDS_CPS_RUNNING("commands.cps.running",Guard.getLangConfig().getString("commands.cps.running")),
    COMMANDS_CPS_FORMAT("commands.cps.format",Guard.getLangConfig().getString("commands.cps.format")),
    COMMANDS_CPS_END("commands.cps.end",Guard.getLangConfig().getString("commands.cps.end")),

    COMMANDS_VANISH_ACTIVATED("commands.vanish.activated",Guard.getLangConfig().getString("commands.vanish.activated")),
    COMMANDS_VANISH_DESACTIVATED("commands.vanish.desactivated",Guard.getLangConfig().getString("commands.vanish.desactivated")),

    COMMANDS_TELEPORT_MISSARGS("commands.teleport.missargs",Guard.getLangConfig().getString("commands.teleport.missargs")),
    COMMANDS_TELEPORT_CONFIRMATION("commands.teleport.confirmation",Guard.getLangConfig().getString("commands.teleport.confirmation")),

    COMMANDS_TELEPORTHERE_MISSARGS("commands.teleporthere.missargs",Guard.getLangConfig().getString("commands.teleporthere.missargs")),
    COMMANDS_TELEPORTHERE_CONFIRMATION("commands.teleporthere.confirmation",Guard.getLangConfig().getString("commands.teleporthere.confirmation")),

    COMMANDS_SANCTIONSET_MISSARGS("commands.sanctionset.missargs",Guard.getLangConfig().getString("commands.sanctionset.missargs")),

    COMMANDS_NOCLIP_ACTIVATED("commands.noclip.activated",Guard.getLangConfig().getString("commands.noclip.activated")),
    COMMANDS_NOCLIP_DESACTIVATED("commands.noclip.desactivated",Guard.getLangConfig().getString("commands.noclip.desactivated")),

    COMMANDS_CHATCOLOR_ACTIVATED("COMMANDS_CHATCOLOR_ACTIVATED",Guard.getLangConfig().getString("commands.chatcolor.activated")),
    COMMANDS_CHATCOLOR_DESACTIVATED("commands.chatcolor.activated",Guard.getLangConfig().getString("commands.chatcolor.desactivated")),
    COMMANDS_CHATCOLOR_COLOR("commands.chatcolor.color",Guard.getLangConfig().getString("commands.chatcolor.color")),

    COMMANDS_INVENTORY_MISSARGS("commands.inventory.missargs",Guard.getLangConfig().getString("commands.inventory.missargs")),

    COMMANDS_MODLIST_FORMAT("commands.modlist.format",Guard.getLangConfig().getStringList("commands.modlist.format")),

    COMMANDS_CHATLOCK_ACTIVATED("commands.chatlock.activated",Guard.getLangConfig().getString("commands.chatlock.activated")),
    COMMANDS_CHATLOCK_DESACTIVATED("commands.chatlock.desactivated",Guard.getLangConfig().getString("commands.chatlock.desactivated")),
    COMMANDS_CHATLOCK_LOCK("commands.chatlock.lock_message",Guard.getLangConfig().getString("commands.chatlock.lock_message")),

    COMMANDS_LOOKUP_MESSAGE("commands.lookup.message",Guard.getLangConfig().getStringList("commands.lookup.message")),

    MENU_FREEZE_TITLE("menu.freeze.title",Guard.getLangConfig().getString("menu.freeze.title")),
    MENU_FREEZE_LORE("menu.freeze.lore", Guard.getLangConfig().getStringList("menu.freeze.lore")),

    MENU_SANCTIONSET_SELECTOR_TITLE("menu.sanctionset.selector.title",Guard.getLangConfig().getString("menu.sanctionset.selector.title")),
    MENU_SANCTIONSET_SELECTOR_PLAYER_LORE("menu.sanctionset.selector.player_lore",Guard.getLangConfig().getStringList("menu.sanctionset.selector.player_lore")),

    MENU_SANCTIONSET_MAIN_TITLE("menu.sanctionset.main.title",Guard.getLangConfig().getString("menu.sanctionset.main.title")),
    MENU_SANCTIONSET_MAIN_BANTITLE("menu.sanctionset.main.ban",Guard.getLangConfig().getString("menu.sanctionset.main.ban")),
    MENU_SANCTIONSET_MAIN_BANLORE("menu.sanctionset.main.ban_lore",Guard.getLangConfig().getStringList("menu.sanctionset.main.ban_lore")),
    MENU_SANCTIONSET_MAIN_WARNTITLE("menu.sanctionset.main.warn",Guard.getLangConfig().getString("menu.sanctionset.main.warn")),
    MENU_SANCTIONSET_MAIN_WARNLORE("menu.sanctionset.main.warn_lore",Guard.getLangConfig().getStringList("menu.sanctionset.main.warn_lore")),
    MENU_SANCTIONSET_MAIN_MUTETITLE("menu.sanctionset.main.mute",Guard.getLangConfig().getString("menu.sanctionset.main.mute")),
    MENU_SANCTIONSET_MAIN_MUTELORE("menu.sanctionset.main.mutelore",Guard.getLangConfig().getStringList("menu.sanctionset.main.mute_lore")),

    MENU_SANCTIONSET_MUTE_TITLE("menu.sanctionset.mute.title",Guard.getLangConfig().getString("menu.sanctionset.mute.title")),
    MENU_SANCTIONSET_MUTE_LORE("menu.sanctionset.mute.lore", Guard.getLangConfig().getStringList("menu.sanctionset.mute.lore")),

    MENU_SANCTIONSET_WARN_TITLE("menu.sanctionset.warn.title",Guard.getLangConfig().getString("menu.sanctionset.warn.title")),
    MENU_SANCTIONSET_WARN_LORE("menu.sanctionset.warn.lore", Guard.getLangConfig().getStringList("menu.sanctionset.warn.lore")),

    MENU_SANCTIONSET_BAN_TITLE("menu.sanctionset.ban.title",Guard.getLangConfig().getString("menu.sanctionset.ban.title")),
    MENU_SANCTIONSET_BAN_LORE("menu.sanctionset.ban.lore", Guard.getLangConfig().getStringList("menu.sanctionset.ban.lore")),

    MENU_PARAMETERS_FLYSPEED_TITLE("menu.parameters.flyspeed.title", Guard.getLangConfig().getString("menu.parameters.flyspeed.title")),
    MENU_PARAMETERS_FLYSPEED_LORE("menu.parameters.flyspeed.lore", Guard.getLangConfig().getStringList("menu.parameters.flyspeed.lore")),

    MENU_PARAMETERS_GODMODE_TITLE("menu.parameters.godmode.title", Guard.getLangConfig().getString("menu.parameters.godmode.title")),
    MENU_PARAMETERS_GODMODE_LORE("menu.parameters.godmode.lore", Guard.getLangConfig().getStringList("menu.parameters.godmode.lore")),

    MENU_PARAMETERS_NIGHTVISION_TITLE("menu.parameters.nightvision.title", Guard.getLangConfig().getString("menu.parameters.nightvision.title")),
    MENU_PARAMETERS_NIGHTVISION_LORE("menu.parameters.nightvision.lore", Guard.getLangConfig().getStringList("menu.parameters.nightvision.lore")),

    MENU_XRAY_TITLE("menu.xray.title", Guard.getLangConfig().getString("menu.xray.title")),
    MENU_XRAY_PLAYER_NAME("menu.xray.player.name", Guard.getLangConfig().getString("menu.xray.player.name")),
    MENU_XRAY_PLAYER_LORE("menu.xray.player.lore", Guard.getLangConfig().getStringList("menu.xray.player.lore")),

    AUTOMOD_CHAT_WARN("automod.chat.warning_message",Guard.getLangConfig().getStringList("automod.chat.warning_message")),
    AUTOMOD_CHAT_ALERT("automod.chat.moderator_alert",Guard.getLangConfig().getString("automod.chat.moderator_alert")),
    ;



    private final String path;
    private String message;
    private List<String> lore;

    Messages(String path, String message) {
        this.path = path;
        this.message = message;
        this.lore = null;
    }

    Messages(String path, List<String> lore) {
        this.path = path;
        this.message = null;
        this.lore = lore;
    }

    public String getMessage() {
        if(message == null)
            return "&4Error Checkout your message config (need an update ?)";
        return message;
    }

    public List<String> getLore() {
        if(lore == null)
            return Collections.singletonList("&4Error Checkout your message config (need an update ?)");
        return lore;
    }

    public static String prefix() { return ChatUtil.translate(Guard.getLangConfig().getString("prefix"));}

    public static void send(Player p, Messages typemsg) {
        if(typemsg.getMessage().contains("[ACTION]")) {
            String msg = typemsg.getMessage().replace("[CHAT]","").replace("[ACTION]","");

            Title.sendActionBar(p,ChatUtil.translate(msg));
        }
        if (typemsg.getMessage().contains("[CHAT]")) {

            String msg = typemsg.getMessage().replace("[CHAT]","").replace("[ACTION]","");
            p.sendMessage(ChatUtil.translate(Guard.getLangConfig().getString("prefix")) + ChatUtil.translate(msg));
            return;

        }
        p.sendMessage( ChatUtil.translate(Guard.getLangConfig().getString("prefix")) + ChatUtil.translate(typemsg.getMessage()));

    }

    public static void refreshAll() {
        for (Messages e : values()) {
            if(e.message != null)
                e.message = Guard.getLangConfig().getString(e.path);
            else
                e.lore = Guard.getLangConfig().getStringList(e.path);
        }
    }
}
