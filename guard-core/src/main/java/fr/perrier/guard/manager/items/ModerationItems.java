package fr.perrier.guard.manager.items;

import com.cryptomorin.xseries.*;
import fr.perrier.guard.*;
import fr.perrier.guard.manager.commands.*;
import fr.perrier.guard.manager.listener.hotbar.*;
import fr.perrier.guard.manager.menu.moderation.Parametres;
import fr.perrier.guard.utils.*;
import fr.perrier.guard.utils.item.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

import java.util.*;

public class ModerationItems {

    public static ItemStack nothing = new ItemBuilder(XMaterial.AIR.parseItem()).toItemStack();

    public static CustomItem vanish_on = new CustomItem(
            new ItemBuilder(XMaterial.LIME_DYE.parseItem())
                    .setDurability(10)
                    .setName(Objects.requireNonNull(Guard.getLangConfig().getString("items.vanish_on"))
                            .replace("%v%","✓"))
                    .toItemStack(),
            Objects.requireNonNull(Guard.getLangConfig().getString("items.vanish_on"))
                    .replace("%v%","✓"),
            onClick -> {
                if(Guard.getInstance().getVanishALL().get(onClick.getPlayer().getUniqueId()))
                    Vanish.Show(onClick.getPlayer());
            });

    public static CustomItem vanish_off = new CustomItem(
            new ItemBuilder(XMaterial.RED_DYE.parseItem())
                    .setDurability(1)
                    .setName(Objects.requireNonNull(Guard.getLangConfig().getString("items.vanish_off"))
                            .replace("%x%","✗"))
                    .toItemStack(),
            Objects.requireNonNull(Guard.getLangConfig().getString("items.vanish_off")).replace("%x%","✗"),
            onClick -> {
                if(Guard.getInstance().getVanishALL().get(onClick.getPlayer().getUniqueId()))
                    Vanish.Hide(onClick.getPlayer());
            });

    public static CustomItem parameters = new CustomItem(
            new ItemBuilder(XMaterial.COMPARATOR.parseItem())
                    .setName(Guard.getLangConfig().getString("items.parameters"))
                    .toItemStack(),
            Guard.getLangConfig().getString("items.parameters"),
            onClick -> {
                if(Guard.getInstance().getVanishALL().get(onClick.getPlayer().getUniqueId()))
                    new Parametres().openMenu(onClick.getPlayer());
            });

    public static CustomItem teleporter = new CustomItem(
            new ItemBuilder(XMaterial.BEACON.parseItem())
                    .setName(Guard.getLangConfig().getString("items.teleporter"))
                    .toItemStack(),
            Guard.getLangConfig().getString("items.teleporter"),
            onClick -> {
                if(Guard.getInstance().getVanishALL().get(onClick.getPlayer().getUniqueId()))
                    onClick.getPlayer().sendMessage(ChatUtil.translate("&cSoon.."));
            });

    public static CustomItem random_tp = new CustomItem(
            new ItemBuilder(XMaterial.COMMAND_BLOCK_MINECART.parseItem())
                    .setName(Guard.getLangConfig().getString("items.random_tp"))
                    .toItemStack(),
            Guard.getLangConfig().getString("items.random_tp"),
            onClick -> {
                if(Guard.getInstance().getVanishALL().get(onClick.getPlayer().getUniqueId())) {
                    Player p = onClick.getPlayer();
                    if (Bukkit.getOnlinePlayers().size() <= 1) {
                        p.sendMessage(Messages.prefix() + ChatUtil.translate(Messages.PLAYERNOTFOUND.getMessage()));
                        return;
                    }
                    ArrayList<Player> players = new ArrayList<Player>();
                    for (Player rp : Bukkit.getOnlinePlayers()) {
                        if (p == rp) continue;
                        players.add(rp);
                    }
                    Player randomPlayer = players.get(new Random().nextInt(players.size()));
                    Title.sendActionBar(p, Messages.prefix() + ChatUtil.translate(Messages.COMMANDS_TELEPORT_CONFIRMATION.getMessage().replace("%target%", randomPlayer.getName())));
                    p.teleport(randomPlayer.getLocation());
                }
            });

    public static CustomItem chatcolor = new CustomItem(
            new ItemBuilder(XMaterial.PAPER.parseItem())
                    .setName(Guard.getLangConfig().getString("items.chat_color"))
                    .toItemStack(),
            Guard.getLangConfig().getString("items.chat_color"),
            onClick -> {
                if(Guard.getInstance().getVanishALL().get(onClick.getPlayer().getUniqueId()))
                    onClick.getPlayer().chat("/chatcolor");
            });

    public static CustomItem noclip = new CustomItem(
            new ItemBuilder(XMaterial.GLASS.parseItem())
                    .setName(Guard.getLangConfig().getString("items.no_clip"))
                    .toItemStack(),
            Guard.getLangConfig().getString("items.no_clip"),
            onClick -> {
                if(Guard.getInstance().getVanishALL().get(onClick.getPlayer().getUniqueId()))
                    onClick.getPlayer().chat("/noclip");
            });

    public static CustomItem page2from1 = new CustomItem(
            new ItemBuilder(XMaterial.ARROW.parseItem())
                    .setName(Guard.getLangConfig().getString("items.go_page_2_from_1"))
                    .toItemStack(),
            Guard.getLangConfig().getString("items.go_page_2_from_1"),
            onClick -> {
                VanishHotbar.page2(onClick.getPlayer());
            });

    public static CustomItem page1from2 = new CustomItem(
            new ItemBuilder(XMaterial.ARROW.parseItem())
                    .setName(Guard.getLangConfig().getString("items.go_page_1"))
                    .toItemStack(),
            Guard.getLangConfig().getString("items.go_page_1"),
            onClick -> {
                if(Guard.getInstance().getVanishALL().get(onClick.getPlayer().getUniqueId()))
                    VanishHotbar.page1(onClick.getPlayer());
            });

    public static CustomItem page3from2 = new CustomItem(
            new ItemBuilder(XMaterial.ARROW.parseItem())
                    .setName(Guard.getLangConfig().getString("items.go_page_3"))
                    .toItemStack(),
            Guard.getLangConfig().getString("items.go_page_3"),
            onClick -> {
                if(Guard.getInstance().getVanishALL().get(onClick.getPlayer().getUniqueId()))
                    VanishHotbar.page3(onClick.getPlayer());
            });

    public static CustomItem page2from3 = new CustomItem(
            new ItemBuilder(XMaterial.ARROW.parseItem())
                    .setName(Guard.getLangConfig().getString("items.go_page_2_from_3"))
                    .toItemStack(),
            Guard.getInstance().getLangConfig().getString("items.go_page_2_from_3"),
            onClick -> {
                if(Guard.getInstance().getVanishALL().get(onClick.getPlayer().getUniqueId()))
                    VanishHotbar.page2(onClick.getPlayer());
            });
}
