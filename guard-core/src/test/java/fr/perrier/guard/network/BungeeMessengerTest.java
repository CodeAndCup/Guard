package fr.perrier.guard.network;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Enveloppe Forward (golden bytes) + contrat du NOOP messenger. */
public class BungeeMessengerTest {

    @Test
    public void forwardEnvelopeGoldenBytes() {
        byte[] out = Codec.encodeForward(Codec.encode(Codec.TYPE_STAFFCHAT, "x"));
        byte[] expected = {
                0x00, 0x07, 0x46, 0x6F, 0x72, 0x77, 0x61, 0x72, 0x64,   // UTF "Forward"
                0x00, 0x06, 0x4F, 0x4E, 0x4C, 0x49, 0x4E, 0x45,         // UTF "ONLINE"
                0x00, 0x05, 0x47, 0x75, 0x61, 0x72, 0x64,               // UTF "Guard"
                0x00, 0x02, 0x01, 0x78                                  // short len + frame [0x01,'x']
        };
        assertArrayEquals(expected, out);
    }

    @Test
    public void noopBroadcastNeverCallsHandlersAndNeverThrows() {
        Messenger m = new NoopMessenger();
        AtomicBoolean called = new AtomicBoolean(false);
        m.onMessage(Codec.TYPE_STAFFCHAT, fr -> called.set(true));
        m.broadcast(Codec.TYPE_STAFFCHAT, "salut");
        m.close();
        assertFalse(called.get());
    }
}
