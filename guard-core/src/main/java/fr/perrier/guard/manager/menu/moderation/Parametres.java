package fr.perrier.guard.manager.menu.moderation;

import com.cryptomorin.xseries.*;
import fr.perrier.guard.manager.commands.*;
import fr.perrier.guard.menu.*;
import fr.perrier.guard.menu.buttons.*;
import fr.perrier.guard.utils.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.potion.*;

import java.text.*;
import java.util.*;

public class Parametres extends GlassMenu {
    @Override
    public int getGlassColor() {
        return 14;
    }

    @Override
    public Map<Integer, Button> getAllButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(20, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {

                DecimalFormat numberFormat = new DecimalFormat("#.0");

                ItemBuilder itemBuilder = new ItemBuilder(XMaterial.FEATHER.parseItem()).setName(Messages.MENU_PARAMETERS_FLYSPEED_TITLE.getMessage());

                for(String lore : Messages.MENU_PARAMETERS_FLYSPEED_LORE.getLore())
                    itemBuilder.addLoreLine(lore.replace("%value%",numberFormat.format(player.getFlySpeed()*10)));

                return itemBuilder.toItemStack();
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
                if (clickType.isLeftClick() && player.getFlySpeed() < 0.9f) {
                    player.setFlySpeed(player.getFlySpeed() + 0.1f);
                } else if (clickType.isRightClick() && player.getFlySpeed() > 0.1f) {
                    player.setFlySpeed(player.getFlySpeed() - 0.1f);
                }
            }

            @Override
            public boolean shouldUpdate(Player player, int slot, ClickType clickType) {
                return true;
            }
        });

        buttons.put(22, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {

                ItemBuilder itemBuilder = new ItemBuilder(XMaterial.GOLDEN_APPLE.parseItem()).setName(Messages.MENU_PARAMETERS_GODMODE_TITLE.getMessage());

                for(String lore : Messages.MENU_PARAMETERS_GODMODE_LORE.getLore())
                    itemBuilder.addLoreLine(lore.replace("%value%",((Vanish.godmode.contains(player.getUniqueId())) ? "&a&lOn": "&c&lOff")));

                return itemBuilder.setDurability(1).toItemStack();
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
                if(Vanish.godmode.contains(player.getUniqueId())) {
                    Vanish.godmode.remove(player.getUniqueId());
                }else{
                    Vanish.godmode.add(player.getUniqueId());
                }
            }

            @Override
            public boolean shouldUpdate(Player player, int slot, ClickType clickType) {
                return true;
            }
        });

        buttons.put(24, new Button() {
            @Override
            public ItemStack getButtonItem(Player p0) {

                ItemBuilder itemBuilder = new ItemBuilder(XMaterial.ENDER_EYE.parseItem()).setName(Messages.MENU_PARAMETERS_NIGHTVISION_TITLE.getMessage());

                for(String lore : Messages.MENU_PARAMETERS_NIGHTVISION_LORE.getLore())
                    itemBuilder.addLoreLine(lore.replace("%value%",((player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) ? "&a&lOn": "&c&lOff")));

                return itemBuilder.toItemStack();
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
                if(player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                }else{
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 254));
                }
            }

            @Override
            public boolean shouldUpdate(Player player, int slot, ClickType clickType) {
                return true;
            }
        });

        buttons.put(39, new DisplayButton(new ItemStack(Objects.requireNonNull(XMaterial.AIR.parseItem()))));

        return buttons;
    }

    @Override
    public String getTitle(Player paramPlayer) {
        return "&4&lParamètres";
    }
}
