package fr.perrier.guard.utils.item;

import fr.perrier.guard.utils.*;
import lombok.*;
import org.bukkit.*;
import org.bukkit.inventory.*;

import java.util.*;
import java.util.function.*;

@Getter
public class CustomItem {

    @Getter
    private static final List<CustomItem> customItems = new ArrayList<>();

    private final Material material;
    private final ItemStack itemStack;
    private final String name;
    private final boolean interactItem;
    private final Consumer<CustomItemEvent> callable;
    
    public CustomItem(Material material, String name) {
        this.material = material;
        this.name = name;
        this.interactItem = true;
        this.callable = null;
        itemStack = null;
        if(!customItems.contains(this)) customItems.add(this);
    }

    public CustomItem(Material material, String name, boolean interactItem) {
        this.material = material;
        this.name = name;
        this.interactItem = interactItem;
        this.callable = null;
        itemStack = null;
        if(!customItems.contains(this)) customItems.add(this);
    }

    public CustomItem(Material material, String name, Consumer<CustomItemEvent> event) {
        this.material = material;
        this.name = name;
        this.interactItem = true;
        this.callable = event;
        itemStack = null;
        if(!customItems.contains(this)) customItems.add(this);
    }

    public CustomItem(ItemStack is, String name, Consumer<CustomItemEvent> event) {
        this.material = null;
        this.name = name;
        this.interactItem = true;
        this.callable = event;
        itemStack = is;
        if(!customItems.contains(this)) customItems.add(this);
    }

    public CustomItem(Material material, String name, boolean interactItem, Consumer<CustomItemEvent> event) {
        this.material = material;
        this.name = name;
        this.interactItem = interactItem;
        this.callable = event;
        itemStack = null;
        if(!customItems.contains(this)) customItems.add(this);
    }

    public ItemStack toItemStack() {
        if(itemStack != null) {
            return new ItemBuilder(itemStack).setName("&f&l" + name).toItemStack();
        }
        return new ItemBuilder(material).setName("&f&l" + name ).toItemStack();
    }

    public static CustomItem getCustomItem(ItemStack itemStack) {
        return getCustomItems().stream().filter(c -> itemStack.isSimilar(c.toItemStack())).findFirst().orElse(null);
    }
}
