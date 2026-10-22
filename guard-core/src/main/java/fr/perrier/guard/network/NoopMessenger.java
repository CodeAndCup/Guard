package fr.perrier.guard.network;

import java.util.function.Consumer;

/** Transport inactif (network.enable=false ou mono-serveur sans proxy) : silence garanti. */
public class NoopMessenger implements Messenger {

    @Override
    public void broadcast(byte type, String payload) {
    }

    @Override
    public void onMessage(byte type, Consumer<GuardFrame> handler) {
    }

    @Override
    public void close() {
    }
}
