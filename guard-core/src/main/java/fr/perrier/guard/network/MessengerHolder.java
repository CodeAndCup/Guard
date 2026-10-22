package fr.perrier.guard.network;

import fr.perrier.guard.Guard;

/** Calque CompatHolder : accès statique au transport réseau, initialisé une fois au boot. */
public final class MessengerHolder {

    private static volatile Messenger messenger;

    private MessengerHolder() {
    }

    public static void init(Guard plugin) {
        messenger = Guard.getDefaultConfig().getBoolean("network.enable", true)
                ? new BungeeMessenger(plugin)
                : new NoopMessenger();
        Guard.getInstance().getLogger().info("Messenger: " + messenger.getClass().getSimpleName());
    }

    public static Messenger get() {
        return messenger;
    }
}
