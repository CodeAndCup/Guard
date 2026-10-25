package fr.perrier.guard.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
