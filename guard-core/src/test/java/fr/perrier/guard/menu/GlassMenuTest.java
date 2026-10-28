package fr.perrier.guard.menu;

import fr.perrier.guard.menu.buttons.Glass;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contrat de la bordure GlassMenu : la taille DÉCLARÉE (getSize()) fait foi, et le glass
 * ne doit JAMAIS écraser un bouton de contenu.
 *
 * Bug historique : la bordure était calculée depuis le slot le plus haut des boutons.
 * Un menu de 27 slots dont les boutons s'arrêtaient en rangée 2 calculait size=18, et les
 * bords size-10 / size-2 tombaient sur les slots 10 et 16 — les tuiles de ces slots
 * disparaissaient sous le pane de verre (capture in-game du dashboard).
 */
public class GlassMenuTest {

    /** Menu 27 slots, contenu uniquement en rangée 2 (le cas du dashboard). */
    private static class Row2OnlyMenu extends GlassMenu {
        @Override
        public String getTitle(Player player) {
            return "test";
        }

        @Override
        public int getGlassColor() {
            return 9;
        }

        @Override
        public int getSize() {
            return 27;
        }

        @Override
        public Map<Integer, Button> getAllButtons(Player player) {
            Map<Integer, Button> buttons = new HashMap<>();
            for (int slot : new int[]{10, 12, 14, 16}) {
                buttons.put(slot, new StubButton());
            }
            return buttons;
        }
    }

    private static class StubButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return null;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
        }
    }

    @Test
    public void declaredSizeWinsOverHighestButton() {
        Map<Integer, Button> buttons = new Row2OnlyMenu().getButtons(null);

        // la rangée 2 garde son contenu (c'était le bug : slots 10 et 16 avalés par le glass)
        assertFalse(buttons.get(10) instanceof Glass, "slot 10 écrasé par le glass");
        assertFalse(buttons.get(12) instanceof Glass, "slot 12 écrasé par le glass");
        assertFalse(buttons.get(14) instanceof Glass, "slot 14 écrasé par le glass");
        assertFalse(buttons.get(16) instanceof Glass, "slot 16 écrasé par le glass");

        // la bordure de la DERNIÈRE rangée (size 27) est bien posée
        assertTrue(buttons.get(18) instanceof Glass, "coin bas-gauche manquant");
        assertTrue(buttons.get(25) instanceof Glass, "coin bas-droit manquant");
        assertTrue(buttons.get(26) instanceof Glass, "coin bas-droit manquant");

        // la bordure haute reste
        assertTrue(buttons.get(0) instanceof Glass);
        assertTrue(buttons.get(8) instanceof Glass);
    }
}
