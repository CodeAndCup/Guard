package fr.perrier.guard.menu;

import com.cryptomorin.xseries.*;
import fr.perrier.guard.Guard;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.IntStream;

public abstract class Menu {
    @Getter
    public static Map<String, Menu> currentlyOpenedMenus = new HashMap<>();
    @Getter
    private Map<Integer, Button> buttons = new HashMap<>();
    @Getter
    private boolean autoUpdate = false;
    @Getter
    private boolean updateAfterClick = true;
    @Getter
    private boolean closedByMenu = false;
    @Getter
    private boolean placeholder = false;
    @Getter
    private Button placeholderButton = Button.placeholder(XMaterial.WHITE_STAINED_GLASS_PANE.parseMaterial(), (byte) 15, " ");
    @Getter
    private Inventory inventory;

    public void setButtons(Map<Integer, Button> buttons) {
        this.buttons = buttons;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public void setUpdateAfterClick(boolean updateAfterClick) {
        this.updateAfterClick = updateAfterClick;
    }

    public void setClosedByMenu(boolean closedByMenu) {
        this.closedByMenu = closedByMenu;
    }

    public void setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
    }

    public void setPlaceholderButton(Button placeholderButton) {
        this.placeholderButton = placeholderButton;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    private ItemStack createItemStack(Player player, Button button) {
        ItemStack item = button.getButtonItem(player);
        if (item.getType() != XMaterial.PLAYER_HEAD.parseMaterial()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName())
                meta.setDisplayName(meta.getDisplayName() + "§b§c§d§e");
            item.setItemMeta(meta);
        }
        return item;
    }

    public void fill(Map buttons, final ItemStack itemStack) {
        IntStream.range(0, getSize()).filter(slot -> (buttons.get(slot) == null))

                .forEach(slot -> buttons.put(slot, new Button() {
                    public ItemStack getButtonItem(Player player) {
                        return itemStack;
                    }
                }));
    }

    public void openMenu(Player player) {
        this.buttons = getButtons(player);
        Menu previousMenu = currentlyOpenedMenus.get(player.getName());
        this.inventory = null;
        int size = (getSize() == -1) ? size(this.buttons) : getSize();
        boolean update = false;
        String title = getTitle(player);
        if (title.length() > 32)
            title = title.substring(0, 32);
        if (player.getOpenInventory() != null)
            if (previousMenu == null) {
                ButtonListener.onInventoryClose(player);
            } else {
                int previousSize = player.getOpenInventory().getTopInventory().getSize();
                if (previousSize == size && player.getOpenInventory().getTitle().equalsIgnoreCase(title)) {
                    this.inventory = player.getOpenInventory().getTopInventory();
                    update = true;
                } else {
                    previousMenu.setClosedByMenu(true);
                    ButtonListener.onInventoryClose(player);
//                    player.closeInventory();
                }
            }
        if (this.inventory == null)
            this.inventory = Bukkit.createInventory(player, size, ChatColor.translateAlternateColorCodes('&', title));
        this.inventory.setContents(new ItemStack[this.inventory.getSize()]);
        currentlyOpenedMenus.put(player.getName(), this);
        for (Map.Entry<Integer, Button> buttonEntry : this.buttons.entrySet())
            try {
                this.inventory.setItem(buttonEntry.getKey(), createItemStack(player, buttonEntry.getValue()));
            } catch (Exception ignored) {
            }
        if (isPlaceholder())
            for (int index = 0; index < size; index++) {
                if (this.buttons.get(index) == null) {
                    this.buttons.put(index, this.placeholderButton);
                    this.inventory.setItem(index, this.placeholderButton.getButtonItem(player));
                }
            }
        if (update) {
            player.updateInventory();
        } else {
            if (this.inventory instanceof CraftingInventory) return;
            player.openInventory(this.inventory);
        }
        onOpen(player);
        setClosedByMenu(false);
        Bukkit.getScheduler().runTaskLaterAsynchronously(Guard.getInstance(), () -> currentlyOpenedMenus.put(player.getName(), this), 1L);
    }

    public static int size(Map<Integer, Button> buttons) {
        int highest = 0;
        for (Iterator<Integer> iterator = buttons.keySet().iterator(); iterator.hasNext(); ) {
            int buttonValue = iterator.next();
            if (buttonValue > highest)
                highest = buttonValue;
        }
        return (int) (Math.ceil((highest + 1) / 9.0D) * 9.0D);
    }

    public int getSlot(int x, int y) {
        return 9 * y + x;
    }

    public int getSize() {
        return -1;
    }

    public void onOpen(Player player) {
    }

    public void onClose(Player player) {
    }

    protected void surroundButtons(boolean full, Map buttons, final ItemStack itemStack) {
        IntStream.range(0, getSize()).filter(slot -> (buttons.get(slot) == null)).forEach(slot -> {
            if (slot < 9 || slot > getSize() - 10 || (full && (slot % 9 == 0 || (slot + 1) % 9 == 0)))
                buttons.put(slot, new Button() {
                    public ItemStack getButtonItem(Player player) {
                        return itemStack;
                    }
                });
        });
    }

    public abstract String getTitle(Player paramPlayer);

    public abstract Map<Integer, Button> getButtons(Player paramPlayer);
}
