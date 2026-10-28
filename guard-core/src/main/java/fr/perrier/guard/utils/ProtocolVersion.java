package fr.perrier.guard.utils;

/**
 * Table protocol Minecraft -> "1.x.y", basée sur le handshake du client.
 * Le protocol n'est pas exposé par l'API Bukkit : le source est ViaVersion
 * (réflexion, plugin optionnel) ou, à défaut, la version du serveur.
 */
public enum ProtocolVersion {
    _3("1.7-1.7.1"),
    _4("1.7.2-1.7.5"),
    _5("1.7.6-1.7.10"),
    _47("1.8"),
    _107("1.9"),
    _108("1.9.1"),
    _109("1.9.2"),
    _110("1.9.3/4"),
    _210("1.10"),
    _315("1.11"),
    _316("1.11.1"),
    _335("1.12"),
    _338("1.12.1"),
    _340("1.12.2"),
    _393("1.13"),
    _401("1.13.1"),
    _404("1.13.2"),
    _477("1.14"),
    _480("1.14.1"),
    _485("1.14.2"),
    _490("1.14.3"),
    _498("1.14.4"),
    _573("1.15"),
    _575("1.15.1"),
    _578("1.15.2"),
    _735("1.16"),
    _736("1.16.1"),
    _751("1.16.2"),
    _753("1.16.3"),
    _754("1.16.4"),
    _755("1.17"),
    _756("1.17.1"),
    _757("1.18-1.18.1"),
    _758("1.18.2"),
    _759("1.19"),
    _760("1.19.1-1.19.2"),
    _761("1.19.3"),
    _762("1.19.4"),
    _763("1.20-1.20.1"),
    _764("1.20.2"),
    _765("1.20.3-1.20.4"),
    _766("1.20.5-1.20.6"),
    _767("1.21"),
    _768("1.21.2-1.21.3"),
    _769("1.21.4"),
    _770("1.21.5");

    private final String version;

    ProtocolVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    /** Pur : protocol -> "1.x.y", ou "p<proto>" si hors table (snapshot / version inconnue). */
    public static String of(int protocol) {
        for (ProtocolVersion v : values()) {
            if (v.name().equals("_" + protocol)) {
                return v.version;
            }
        }
        return "p" + protocol;
    }
}
