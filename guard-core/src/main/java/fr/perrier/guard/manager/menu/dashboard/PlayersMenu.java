package fr.perrier.guard.manager.menu.dashboard;

import com.cryptomorin.xseries.XMaterial;
import fr.perrier.guard.Guard;
import fr.perrier.guard.manager.menu.profile.ProfileMenu;
import fr.perrier.guard.menu.Button;
import fr.perrier.guard.menu.pagination.PaginatedMenu;
import fr.perrier.guard.utils.ChatUtil;
import fr.perrier.guard.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** Liste des joueurs en ligne (tuile Joueurs). Le clic est branché sur le profil à la tâche 5. */
public class PlayersMenu extends PaginatedMenu {

    @Override
    public String getPrePaginatedTitle(Player p0) {
        return ChatUtil.translate(Guard.getLangConfig().getString("menu.dashboard.players.title", "&2Joueurs"));
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player p0) {
        HashMap<Integer, Button> buttons = new HashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            buttons.put(buttons.size(), new PlayerButton(player));
        }
        return buttons;
    }

    public static class PlayerButton extends Button {
        private final Player player;

        public PlayerButton(Player player) {
            this.player = player;
        }

        @Override
        public ItemStack getButtonItem(Player p0) {
            ItemBuilder item = new ItemBuilder(XMaterial.PLAYER_HEAD.parseItem())
                    .setSkullOwner(player.getDisplayName())
                    .setName(ChatUtil.translate(Guard.getLangConfig()
                            .getString("menu.dashboard.players.name", "&f" + player.getName())
                            .replace("%player%", player.getName())));
            for (String lore : Guard.getLangConfig().getStringList("menu.dashboard.players.lore")) {
                item.addLoreLine(ChatUtil.translate(lore.replace("%player%", player.getName())));
            }
            return item.toItemStack();
        }

        @Override
        public void clicked(Player p0, int slot, ClickType clickType, int hotbarButton) {
            // profil à onglets (Sanctions / Inventaire / Historique / Info)
            Button.playNeutral(p0);
            new ProfileMenu(player).openMenu(p0);
        }
    }
}
