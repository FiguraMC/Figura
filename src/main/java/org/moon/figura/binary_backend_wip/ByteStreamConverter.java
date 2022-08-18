package org.moon.figura.binary_backend_wip;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ByteStreamConverter {

    //https://wiki.vg/Data_types#VarInt_and_VarLong
    private static final int SEGMENT_BITS = 0b01111111;
    private static final int CONTINUE_BIT = 0b10000000;

    public static final int MAX_BYTE_ARR_SIZE = 1 << 20; //1 MiB

    public static void writeVarInt(DataOutputStream dos, int val) throws IOException {
        while (true) {
            if ((val & ~SEGMENT_BITS) == 0) {
                dos.writeByte(val);
                return;
            }
            dos.writeByte((val & SEGMENT_BITS) | CONTINUE_BIT);
            val >>>= 7;
        }
    }

    public static int readVarInt(ByteBuffer buffer) throws IOException {
        int result = 0;
        int position = 0;
        byte nextByte;

        do {
            if (position >= 32)
                throw new IOException("VarInt was too big!");
            nextByte = buffer.get();
            //Use the last 7 bytes of the number.
            result |= (nextByte & SEGMENT_BITS) << position;
            position += 7;
        } while ((nextByte & CONTINUE_BIT) != 0);

        return result;
    }

    public static void writeUUID(DataOutputStream dos, UUID uuid) throws IOException {
        dos.writeLong(uuid.getMostSignificantBits());
        dos.writeLong(uuid.getLeastSignificantBits());
    }

    public static UUID readUUID(ByteBuffer buf) throws IOException {
        try {
            return new UUID(buf.getLong(), buf.getLong());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static void writeString(DataOutputStream dos, String str) throws IOException {
        writeByteArray(dos, str.getBytes(StandardCharsets.UTF_8));
    }

    public static String readString(ByteBuffer buf) throws IOException {
        try {
            return new String(readByteArray(buf), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static void writeByteArray(DataOutputStream dos, byte[] bytes) throws IOException {
        writeVarInt(dos, bytes.length);
        dos.write(bytes);
    }

    public static byte[] readByteArray(ByteBuffer buf) throws IOException {
        int len = readVarInt(buf);
        if (len > MAX_BYTE_ARR_SIZE)
            throw new IOException("Byte Array too big (" + len + " bytes)");
        byte[] bytes = new byte[len];
        buf.get(bytes);
        return bytes;
    }

    public static void writeBoolean(DataOutputStream dos, boolean bool) throws IOException {
        dos.write(bool ? 1 : 0);
    }

    public static boolean readBoolean(ByteBuffer buf) throws IOException {
        byte val = buf.get();
        if (val == 1) return true;
        if (val == 0) return false;
        throw new IOException("Attempt to read boolean failed: expected 0 or 1, got " + val);
    }



}
