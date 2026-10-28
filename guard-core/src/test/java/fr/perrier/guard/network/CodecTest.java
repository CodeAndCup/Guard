package fr.perrier.guard.network;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Roundtrip + robustesse du codec des frames Guard sur le canal BungeeCord. */
public class CodecTest {

    @Test
    public void roundtripStaffChat() {
        byte[] frame = Codec.encode(Codec.TYPE_STAFFCHAT, "Alex\u0000salut");
        GuardFrame out = Codec.decodeForward(Codec.encodeForward(frame));
        assertEquals(Codec.TYPE_STAFFCHAT, out.getType());
        assertEquals("Alex\u0000salut", out.getPayload());
    }

    @Test
    public void roundtripVanishWithAccents() {
        byte[] frame = Codec.encode(Codec.TYPE_VANISH, "069a79c4-ed6e-4a33-8c24-d7283a2bcb21\u0000true");
        GuardFrame out = Codec.decodeForward(Codec.encodeForward(frame));
        assertEquals(Codec.TYPE_VANISH, out.getType());
        assertEquals("069a79c4-ed6e-4a33-8c24-d7283a2bcb21\u0000true", out.getPayload());
    }

    @Test
    public void nonAsciiSurvivesRoundtrip() {
        byte[] frame = Codec.encode(Codec.TYPE_NOTIFY, "REPORT\u0000lobby\u0000#12 Alex→Steve: §3hacks");
        GuardFrame out = Codec.decodeForward(Codec.encodeForward(frame));
        assertEquals("REPORT\u0000lobby\u0000#12 Alex→Steve: §3hacks", out.getPayload());
    }

    @Test
    public void truncatedBytesAreDropped() {
        byte[] full = Codec.encodeForward(Codec.encode(Codec.TYPE_STAFFCHAT, "x"));
        assertNull(Codec.decodeForward(Arrays.copyOf(full, full.length - 2)));
        assertNull(Codec.decodeForward(Arrays.copyOf(full, 3)));
        assertNull(Codec.decodeForward(new byte[0]));
    }

    @Test
    public void wrongChannelOrSubchannelIsDropped() {
        assertNull(Codec.decodeForward("notbungee".getBytes()));
        assertNull(Codec.decodeForward(null));
    }

    @Test
    public void wrongSubchannelIsDropped() {
        // enveloppe forgée avec un autre sous-canal : la porte réellement contrôlable par un client
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        try (java.io.DataOutputStream out = new java.io.DataOutputStream(buffer)) {
            out.writeUTF("Forward");
            out.writeUTF("ONLINE");
            out.writeUTF("Other");   // sous-canal != "Guard"
            out.writeShort(1);
            out.write(Codec.TYPE_STAFFCHAT);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        assertNull(Codec.decodeForward(buffer.toByteArray()));
    }
}
