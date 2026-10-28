package fr.perrier.guard.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import fr.perrier.guard.Guard;
import lombok.Getter;
import lombok.Setter;

/**
 * AntiCheat packet obfuscation : rewrites the actionNumber field of the
 * {@code TRANSACTION} (window confirmation) packet so that cheater scanners
 * (Polar, Vulcan, ...) cannot fingerprint the server's internal window IDs.
 *
 * <p>Requires ProtocolLib to be installed on the server. When the plugin is
 * enabled and the feature is active in {@code config.yml}, Guard requests
 * ProtocolLib if it is not already loaded.</p>
 */
public class EncryptionAC {

    @Getter
    @Setter
    private static ProtocolManager protocolManager;

    /**
     * Registers the packet listener. No-op if ProtocolLib is unavailable.
     */
    public void registerPacketListener() {
        if (protocolManager == null) {
            return;
        }

        protocolManager.addPacketListener(
                new PacketAdapter(Guard.getInstance(), ListenerPriority.MONITOR, PacketType.Play.Client.TRANSACTION) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        PacketContainer packet = event.getPacket();
                        int windowId = packet.getIntegers().read(0);
                        int actionNumber = packet.getShorts().read(0);
                        boolean accepted = packet.getBooleans().read(0);

                        if (Guard.getDefaultConfig().getBoolean("anticheat_encryption.enabled")) {
                            short obfuscatedPolarID = obfuscatePolarID(actionNumber);
                            packet.getShorts().write(0, obfuscatedPolarID);
                            PacketContainer packetPolarContainer = protocolManager.createPacket(PacketType.Play.Server.TRANSACTION);
                            packetPolarContainer.getIntegers().write(0, windowId);
                            packetPolarContainer.getShorts().write(0, obfuscatedPolarID);
                            packetPolarContainer.getBooleans().write(0, accepted);
                            try {
                                protocolManager.sendServerPacket(event.getPlayer(), packetPolarContainer);
                            } catch (java.lang.reflect.InvocationTargetException ignored) {
                                // joueur déconnecté entre-temps : rien à faire
                            }

                            short obfuscatedVulcanID = obfuscateVulcanID(actionNumber);
                            packet.getShorts().write(0, obfuscatedVulcanID);
                            PacketContainer packetVulcanContainer = protocolManager.createPacket(PacketType.Play.Server.TRANSACTION);
                            packetVulcanContainer.getIntegers().write(0, windowId);
                            packetVulcanContainer.getShorts().write(0, obfuscatedVulcanID);
                            packetVulcanContainer.getBooleans().write(0, accepted);
                            try {
                                protocolManager.sendServerPacket(event.getPlayer(), packetVulcanContainer);
                            } catch (java.lang.reflect.InvocationTargetException ignored) {
                                // joueur déconnecté entre-temps : rien à faire
                            }
                        }
                    }
                });
    }

    private static short obfuscatePolarID(int originalID) {
        return (short) -((19999 + originalID) + 600);
    }

    private static short obfuscateVulcanID(int originalID) {
        return (short) ((19999 + originalID) - 29767);
    }

    /**
     * Resolves the ProtocolLib {@link ProtocolManager} if ProtocolLib is
     * installed on the server. Returns {@code null} when unavailable.
     */
    public static ProtocolManager resolveProtocolManager() {
        try {
            Class<?> clazz = Class.forName("com.comphenix.protocol.ProtocolManager");
            java.lang.reflect.Method fromPlugin = clazz.getMethod("fromPlugin", org.bukkit.plugin.Plugin.class);
            Object instance = fromPlugin.invoke(null, Guard.getInstance());
            return (ProtocolManager) instance;
        } catch (Exception ignored) {
            return null;
        }
    }
}