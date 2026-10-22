package fr.perrier.guard.utils.commands;

import fr.perrier.guard.Guard;
import lombok.Getter;

@Getter
public enum CommandsUtils {
    CHATCOLOR("commands.chatcolor.name",Guard.getDefaultConfig().getStringList("commands.chatcolor.name").toArray(new String[0])),
    CHATLOCK("commands.chatlock.name",Guard.getDefaultConfig().getStringList("commands.chatlock.name").toArray(new String[0])),
    CPS("commands.cps.name",Guard.getDefaultConfig().getStringList("commands.cps.name").toArray(new String[0])),
    FREEZE("commands.freeze.name",Guard.getDefaultConfig().getStringList("commands.freeze.name").toArray(new String[0])),
    INVENTORY_VIEW("commands.inventory.name",Guard.getDefaultConfig().getStringList("commands.inventory.name").toArray(new String[0])),
    MOD_LIST("commands.modlist.name",Guard.getDefaultConfig().getStringList("commands.modlist.name").toArray(new String[0])),
    NOCLIP("commands.noclip.name",Guard.getDefaultConfig().getStringList("commands.noclip.name").toArray(new String[0])),
    SS("commands.sanctionset.name",Guard.getDefaultConfig().getStringList("commands.sanctionset.name").toArray(new String[0])),
    TP("commands.teleport.name",Guard.getDefaultConfig().getStringList("commands.teleport.name").toArray(new String[0])),
    TPHERE("commands.teleporthere.name",Guard.getDefaultConfig().getStringList("commands.teleporthere.name").toArray(new String[0])),
    VANISH("commands.vanish.name",Guard.getDefaultConfig().getStringList("commands.vanish.name").toArray(new String[0])),
    XRAY("commands.xray.name",Guard.getDefaultConfig().getStringList("commands.xray.name").toArray(new String[0])),
    LOOKUP("commands.lookup.name",Guard.getDefaultConfig().getStringList("commands.lookup.name").toArray(new String[0])),
    HELP(null,new String[]{"guard","hguard","helpguard","guard:help","guard:guard"}),
    ;

    private final String path;
    private String[] names;

    CommandsUtils(String path, String[] names) {
        this.path = path;
        this.names = names;
    }

    public static void refreshAll() {
        for(CommandsUtils c : values()) {
            if(c.path != null)
                c.names = Guard.getDefaultConfig().getStringList(c.path).toArray(new String[0]);
        }
    }
}
