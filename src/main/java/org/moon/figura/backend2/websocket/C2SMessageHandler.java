package org.moon.figura.backend2.websocket;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class C2SMessageHandler {

    //ids
    public static final byte
            TOKEN = 0,
            PING = 1,
            SUB = 2, //owo
            UNSUB = 3;

    public static ByteBuffer parseAuthToken(String token) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeByte(TOKEN);
        dos.write(token.getBytes(StandardCharsets.UTF_8));

        return ByteBuffer.wrap(baos.toByteArray());
    }
}
