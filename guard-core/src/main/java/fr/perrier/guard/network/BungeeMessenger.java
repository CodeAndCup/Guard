package fr.perrier.guard.network;

import fr.perrier.guard.Guard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** Transport réel par canal BungeeCord « Forward ». Envoi via le premier joueur en ligne ; 0 joueur → drop silencieux. */
public class BungeeMessenger implements Messenger, PluginMessageListener {

    private final Guard plugin;
    private final Map<Byte, Consumer<GuardFrame>> handlers = new ConcurrentHashMap<>();

    public BungeeMessenger(Guard plugin) {
        this.plugin = plugin;
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, Codec.CHANNEL);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, Codec.CHANNEL, this);
    }

    @Override
    public void broadcast(byte type, String payload) {
        Player sender = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (sender == null) {
            return; // best-effort : sans joueur connecté, le canal n'a pas de transport
        }
        byte[] forward = Codec.encodeForward(Codec.encode(type, payload));
        if (forward != null) {
            sender.sendPluginMessage(plugin, Codec.CHANNEL, forward);
        }
    }

    @Override
    public void onMessage(byte type, Consumer<GuardFrame> handler) {
        handlers.put(type, handler);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!Codec.CHANNEL.equalsIgnoreCase(channel)) {
            return;
        }
        GuardFrame frame = Codec.decodeForward(message);
        if (frame == null) {
            return;
        }
        Consumer<GuardFrame> handler = handlers.get(frame.getType());
        if (handler == null) {
            return;
        }
        // dispatch main thread (contrat Messenger)
        Bukkit.getScheduler().runTask(plugin, () -> handler.accept(frame));
    }

    @Override
    public void close() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin, Codec.CHANNEL);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, Codec.CHANNEL);
    }
}
