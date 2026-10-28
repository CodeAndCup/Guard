package fr.perrier.guard.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Pur : index du paramètre en cours de fraite (meme formule que CupCodeAPI, pattern du mainteneur). */
public class CommandHandlerTest {

    @Test
    public void simpleCommandFirstParameter() {
        // "/ss <player>" : cmdParts = ["ss"], args = ["Ste"] -> param 1
        assertEquals(1, CommandHandler.parameterIndexFor(new String[]{"ss"}, new String[]{"Ste"}));
    }

    @Test
    public void simpleCommandEmptyArg() {
        assertEquals(1, CommandHandler.parameterIndexFor(new String[]{"ss"}, new String[]{""}));
    }

    @Test
    public void subCommandParameterIndex() {
        // "/guard admin edit <floor>" : cmdParts = ["guard","admin","edit"], args = ["admin","edit","Over"] -> param 1
        assertEquals(1, CommandHandler.parameterIndexFor(
                new String[]{"guard", "admin", "edit"}, new String[]{"admin", "edit", "Over"}));
    }

    @Test
    public void secondParameterOfSubCommand() {
        // cmdParts = ["dungeon","admin","edit"], args = ["admin","edit","floor1","12"] -> param 2
        assertEquals(2, CommandHandler.parameterIndexFor(
                new String[]{"dungeon", "admin", "edit"}, new String[]{"admin", "edit", "floor1", "12"}));
    }

    @Test
    public void noArgsYetMeansNoParameterIndex() {
        // "/report" sans espace : args = [] -> index 0 (Bukkit ne complète aucun paramètre) ;
        // dès que le staff tape un espace, args = [""] -> index 1 (cf. simpleCommandEmptyArg)
        assertEquals(0, CommandHandler.parameterIndexFor(new String[]{"report"}, new String[]{}));
    }

    @Test
    public void rootCommandMatchesWhileTypingParameter() {
        // LE bug du mainteneur : "/ss Ste" doit matcher pour que le parametre se complète
        assertEquals("ss", CommandHandler.matchedName(new String[]{"ss"}, "ss", new String[]{"Ste"}));
        assertEquals("ss", CommandHandler.matchedName(new String[]{"ss"}, "ss", new String[]{""}));
    }

    @Test
    public void subCommandMatchesOnlyWhenWordsTyped() {
        String[] names = {"guard admin edit"};
        assertEquals("guard admin edit", CommandHandler.matchedName(names, "guard", new String[]{"admin", "edit", "Over"}));
        // args incomplets -> pas de match (la completion de sous-commandes prend le relais)
        assertNull(CommandHandler.matchedName(names, "guard", new String[]{"admin"}));
        assertNull(CommandHandler.matchedName(names, "guard admin", new String[]{"Over"}));
    }
}
