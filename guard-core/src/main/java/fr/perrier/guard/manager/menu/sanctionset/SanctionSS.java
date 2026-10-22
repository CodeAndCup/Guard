package fr.perrier.guard.manager.menu.sanctionset;

import com.cryptomorin.xseries.*;
import fr.perrier.guard.*;
import lombok.*;
import org.bukkit.*;

public enum SanctionSS {

    //TODO MUTE
    MUTE_SPAM("Mute", Guard.getInstance().getLangConfig().getString("sanction.mute.spam.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.spam.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_FLOOD("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.flood.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.flood.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_PROVOCATION("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.provocation.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.provocation.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_INSULT("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.insult.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.insult.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_ACL("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.abusive_capital_letter.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.abusive_capital_letter.duration"), XMaterial.PAPER.parseMaterial()),
    MUTE_BADLANGUAGE("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.bad_language.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.bad_language.duration"), XMaterial.PAPER.parseMaterial()),
    MUTE_HARASSMENT("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.harassment.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.harassment.duration"), XMaterial.PAPER.parseMaterial()),
    MUTE_DEFAMATION("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.defamation.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.defamation.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_NORESPECT("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.no_respect.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.no_respect.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_RACISM("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.racism.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.racism.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_ItS("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.its.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.its.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_AD("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.advertisement.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.advertisement.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_DOXING("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.doxing.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.doxing.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_IPLEAK("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.ip_leak.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.ip_leak.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_ItO("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.ItO.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.ItO.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_SERVER_BASHING("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.server_bashing.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.server_bashing.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_STAFF_DENIGRATION("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.staff_denigration.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.staff_denigration.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_INSOLENCE_STAFF("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.insolence_staff.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.insolence_staff.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_PROVOCATION_STAFF("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.provocation_staff.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.provocation_staff.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_BAD_LANGUAGE_STAFF("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.bad_language_staff.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.bad_language_staff.duration"),XMaterial.PAPER.parseMaterial()),
    MUTE_INSULT_STAFF("Mute",Guard.getInstance().getLangConfig().getString("sanction.mute.insult_staff.name"),Guard.getInstance().getLangConfig().getString("sanction.mute.insult_staff.duration"),XMaterial.PAPER.parseMaterial()),



    //TODO WARN
    WARN_CROSSTEAM("Warn",Guard.getInstance().getLangConfig().getString("sanction.warn.crossteam.name"),Guard.getInstance().getLangConfig().getString("sanction.warn.crossteam.duration"), XMaterial.WOODEN_SWORD.parseMaterial()),
    WARN_CPS("Warn",Guard.getInstance().getLangConfig().getString("sanction.warn.cps.name"),Guard.getInstance().getLangConfig().getString("sanction.warn.cps.duration"),XMaterial.PISTON.parseMaterial()),
    WARN_GAMEABUSE("Warn",Guard.getInstance().getLangConfig().getString("sanction.warn.game_abuse.name"),Guard.getInstance().getLangConfig().getString("sanction.warn.game_abuse.duration"),XMaterial.BARRIER.parseMaterial()),
    WARN_INAPPROPRIATE_SKIN("Warn",Guard.getInstance().getLangConfig().getString("sanction.warn.inappropriate_skin.name"),Guard.getInstance().getLangConfig().getString("sanction.warn.inappropriate_skin.duration"),XMaterial.SKELETON_SKULL.parseMaterial()),

    //TODO BAN
    BAN_KILLAURA("Ban",Guard.getInstance().getLangConfig().getString("sanction.ban.killaura.name"),Guard.getInstance().getLangConfig().getString("sanction.ban.killaura.duration"),XMaterial.IRON_SWORD.parseMaterial()),
    BAN_VELOCITY("Ban",Guard.getInstance().getLangConfig().getString("sanction.ban.velocity.name"),Guard.getInstance().getLangConfig().getString("sanction.ban.velocity.duration"),XMaterial.ANVIL.parseMaterial()),
    BAN_REACH("Ban",Guard.getInstance().getLangConfig().getString("sanction.ban.reach.name"),Guard.getInstance().getLangConfig().getString("sanction.ban.reach.duration"),XMaterial.PISTON.parseMaterial()),
    BAN_FLY("Ban",Guard.getInstance().getLangConfig().getString("sanction.ban.fly.name"),Guard.getInstance().getLangConfig().getString("sanction.ban.fly.duration"),XMaterial.FEATHER.parseMaterial()),
    BAN_BUNNY("Ban",Guard.getInstance().getLangConfig().getString("sanction.ban.bunny_up.name"),Guard.getInstance().getLangConfig().getString("sanction.ban.bunny_up.duration"),XMaterial.SLIME_BLOCK.parseMaterial()),
    BAN_SPEEDHACK("Ban",Guard.getInstance().getLangConfig().getString("sanction.ban.speed_hack.name"),Guard.getInstance().getLangConfig().getString("sanction.ban.speed_hack.duration"),XMaterial.SUGAR_CANE.parseMaterial()),
    BAN_JESUS("Ban",Guard.getInstance().getLangConfig().getString("sanction.ban.jesus.name"),Guard.getInstance().getLangConfig().getString("sanction.ban.jesus.duration"),XMaterial.WATER_BUCKET.parseMaterial()),
    BAN_AUTOCLICK("Ban",Guard.getInstance().getLangConfig().getString("sanction.ban.auto_click.name"),Guard.getInstance().getLangConfig().getString("sanction.ban.auto_click.duration"),XMaterial.STICKY_PISTON.parseMaterial()),
    BAN_USURPATION("Ban",Guard.getInstance().getLangConfig().getString("sanction.ban.usurpation.name"),Guard.getInstance().getLangConfig().getString("sanction.ban.usurpation.duration"),XMaterial.SKELETON_SKULL.parseMaterial()),
    BAN_BOT("Ban",Guard.getInstance().getLangConfig().getString("sanction.ban.bot.name"),Guard.getInstance().getLangConfig().getString("sanction.ban.bot.duration"),XMaterial.COMMAND_BLOCK.parseMaterial()),


    ;

    @Getter
    private final String type;
    private final String reason;
    private final String duration;
    private final Material item;

    SanctionSS(String type, String reason, String duration, Material item) {
        this.type = type;
        this.reason = reason;
        this.duration = duration;
        this.item = item;
    }

    public String getReason() {
        return reason;
    }

    public String getDuration() {
        return duration;
    }

    public Material getItem() {
        return item;
    }
}
