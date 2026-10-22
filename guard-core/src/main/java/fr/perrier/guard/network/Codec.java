package fr.perrier.guard.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Encodage/décodage des frames Guard + enveloppe « Forward » du canal BungeeCord. Toute erreur → null/drop. */
public final class Codec {

    public static final String CHANNEL = "BungeeCord";
    public static final String SUBCHANNEL = "Guard";

    public static final byte TYPE_STAFFCHAT = 1;
    public static final byte TYPE_NOTIFY = 2;
    public static final byte TYPE_VANISH = 3;

    private Codec() {
    }

    /** [type] + UTF-8(payload). */
    public static byte[] encode(byte type, String payload) {
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[1 + payloadBytes.length];
        out[0] = type;
        System.arraycopy(payloadBytes, 0, out, 1, payloadBytes.length);
        return out;
    }

    /** Enveloppe Forward/ONLINE/<sub> du protocole BungeeCord : UTF "Forward", UTF "ONLINE", UTF sub, short Longueur, data. */
    public static byte[] encodeForward(byte[] frame) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(buffer)) {
            out.writeUTF("Forward");
            out.writeUTF("ONLINE");
            out.writeUTF(SUBCHANNEL);
            out.writeShort(frame.length);
            out.write(frame);
        } catch (IOException e) {
            return null;    // ByteArrayOutputStream n'échoue pas de fait ; garde-fou
        }
        return buffer.toByteArray();
    }

    /** Parse une enveloppe entrante ; null si canal/sous-canal différents, bytes tronqués, type inconnu ou UTF invalide. */
    public static GuardFrame decodeForward(byte[] message) {
        if (message == null || message.length == 0) {
            return null;
        }
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            if (!"Forward".equals(in.readUTF())) {
                return null;
            }
            in.readUTF();   // cible ("ONLINE") — pas utile ici
            if (!SUBCHANNEL.equals(in.readUTF())) {
                return null;
            }
            short length = in.readShort();
            if (length < 1) {
                return null;
            }
            byte[] frame = new byte[length];
            in.readFully(frame);
            return decode(frame);
        } catch (IOException | IllegalArgumentException | NegativeArraySizeException e) {
            return null;
        }
    }

    /** [type] + UTF-8(payload) → frame ; type inconnu → null. */
    static GuardFrame decode(byte[] frame) {
        if (frame.length < 1) {
            return null;
        }
        byte type = frame[0];
        if (type != TYPE_STAFFCHAT && type != TYPE_NOTIFY && type != TYPE_VANISH) {
            return null;
        }
        try {
            return new GuardFrame(type, new String(frame, 1, frame.length - 1, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
