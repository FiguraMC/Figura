package org.moon.figura.binary_backend.packets.server2client;

import org.moon.figura.binary_backend.ByteStreamConverter;
import org.moon.figura.binary_backend.packets.PacketType;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Sent from server to client when the client authenticates with the backend.
 * Contains a String authentication token.
 */
public class S2CAuthPacket extends AbstractS2CPacket {

    public String token;

    @Override
    protected int getId() {
        return PacketType.S2C_AUTH.id; //figura out these codes eventually
    }

    @Override
    public void fill(ByteBuffer buf) throws IOException {
        token = ByteStreamConverter.readString(buf);
    }
}
