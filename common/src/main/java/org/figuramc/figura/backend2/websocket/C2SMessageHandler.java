package org.figuramc.figura.backend2.websocket;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class C2SMessageHandler {

    // ids
    public static final byte
            TOKEN = 0,
            PING = 1,
            SUB = 2, // owo
            UNSUB = 3;

    public static ByteBuffer auth(String token) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeByte(TOKEN);
        dos.write(token.getBytes(StandardCharsets.UTF_8));
        dos.close();

        return ByteBuffer.wrap(baos.toByteArray());
    }

    public static ByteBuffer ping(int id, boolean sync, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeByte(PING);
        dos.writeInt(id);
        dos.writeBoolean(sync);
        dos.write(data);
        dos.close();

        return ByteBuffer.wrap(baos.toByteArray());
    }

    public static ByteBuffer sub(UUID id) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeByte(SUB);
        writeUUID(id, dos);
        dos.close();

        return ByteBuffer.wrap(baos.toByteArray());
    }

    public static ByteBuffer unsub(UUID id) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeByte(UNSUB);
        writeUUID(id, dos);
        dos.close();

        return ByteBuffer.wrap(baos.toByteArray());
    }

    public static void writeUUID(UUID id, DataOutputStream dos) throws IOException {
        dos.writeLong(id.getMostSignificantBits());
        dos.writeLong(id.getLeastSignificantBits());
    }
}
