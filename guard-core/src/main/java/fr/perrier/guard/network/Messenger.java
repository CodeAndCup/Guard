package fr.perrier.guard.network;

import java.util.function.Consumer;

/** Transport des messages InterGuard (canal BungeeCord). Best-effort : aucun ack, aucun état. */
public interface Messenger {

    void broadcast(byte type, String payload);

    /** Le handler est invoqué sur le main thread ; seul le dernier handler par type est conservé. */
    void onMessage(byte type, Consumer<GuardFrame> handler);

    void close();
}
