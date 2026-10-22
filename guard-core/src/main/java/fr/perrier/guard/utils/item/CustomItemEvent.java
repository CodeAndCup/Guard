package fr.perrier.guard.utils.item;

import lombok.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

@Getter
@RequiredArgsConstructor
public class CustomItemEvent {

    private final Player player;
    private final ItemStack itemStack;
    private final boolean rightClick;

}
