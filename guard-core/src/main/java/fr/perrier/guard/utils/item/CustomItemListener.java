package fr.perrier.guard.utils.item;

import lombok.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.*;

@RequiredArgsConstructor
public class CustomItemListener implements Listener {

    private final Plugin plugin;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        CustomItem customItem = CustomItem.getCustomItem(event.getItem());
        if (customItem == null) return;
        if (customItem.getCallable() == null) return;
        boolean rightClick = (event.getAction().name().contains("RIGHT"));
        customItem.getCallable().accept(new CustomItemEvent(event.getPlayer(), event.getItem(), rightClick));
    }

}
