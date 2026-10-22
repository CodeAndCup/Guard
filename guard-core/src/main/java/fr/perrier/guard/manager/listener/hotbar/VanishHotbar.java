package fr.perrier.guard.manager.listener.hotbar;

import com.cryptomorin.xseries.*;
import fr.perrier.guard.*;
import fr.perrier.guard.manager.commands.*;
import fr.perrier.guard.manager.items.*;
import fr.perrier.guard.utils.*;
import org.bukkit.enchantments.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

import java.util.*;

public class VanishHotbar {

    public static void page1(Player p) {

        ArrayList<ItemStack> items = new ArrayList<ItemStack>();

        items.add(ModerationItems.nothing);
        items.add(new ItemBuilder(XMaterial.BOOK.parseItem()).setName(Guard.getInstance().getLangConfig().getString("items.ss_book")).toItemStack());
        items.add(ModerationItems.teleporter.toItemStack());
        items.add(ModerationItems.nothing);
        if(Vanish.vanish.contains(p)) {
            items.add(ModerationItems.vanish_on.toItemStack());
        } else {
            items.add(ModerationItems.vanish_off.toItemStack());
        }
        items.add(ModerationItems.nothing);
        items.add(ModerationItems.parameters.toItemStack());
        items.add(ModerationItems.nothing);
        items.add(ModerationItems.page2from1.toItemStack());

        for(int i =0; i<items.size();i++) {
            p.getInventory().setItem(i,items.get(i));
        }
    }

    public static void page2(Player p) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();

        items.add(ModerationItems.page1from2.toItemStack());
        items.add(ModerationItems.nothing);
        items.add(new ItemBuilder(XMaterial.ICE.parseItem()).setName(Guard.getInstance().getLangConfig().getString("items.freeze")).toItemStack());
        items.add(new ItemBuilder(XMaterial.PISTON.parseItem()).setName(Guard.getInstance().getLangConfig().getString("items.cps")).toItemStack());
        items.add(ModerationItems.nothing);
        items.add(new ItemBuilder(XMaterial.SLIME_BALL.parseItem()).setName(Guard.getInstance().getLangConfig().getString("items.knockback")).addEnchant(Enchantment.KNOCKBACK,3).hideItemFlags().toItemStack());
        items.add(ModerationItems.random_tp.toItemStack());
        items.add(ModerationItems.nothing);
        items.add(ModerationItems.page3from2.toItemStack());

        for(int i=0; i<items.size(); i++) {
            p.getInventory().setItem(i,items.get(i));
        }
    }

    public static void page3(Player p) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        items.add(ModerationItems.page2from3.toItemStack());
        items.add(ModerationItems.nothing);
        items.add(ModerationItems.noclip.toItemStack());
        items.add(new ItemBuilder(XMaterial.CHEST.parseItem()).setName(Guard.getInstance().getLangConfig().getString("items.inventory_view")).toItemStack());
        items.add(ModerationItems.nothing);
        items.add(ModerationItems.nothing);
        items.add(ModerationItems.chatcolor.toItemStack());
        items.add(ModerationItems.nothing);
        items.add(ModerationItems.nothing);

        for(int i=0; i<items.size(); i++) {
            p.getInventory().setItem(i,items.get(i));
        }

    }
}
