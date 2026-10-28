package fr.perrier.guard.compat;

/** Rendu d'une chaîne de lang pour l'ère du serveur courant. */
public interface Compat {

    /**
     * Rend une string de lang vers l'ère du serveur : codes & et #RRGGBB ;
     * legacy → palette §, modern → hex §x natif.
     */
    String colorize(String raw);
}