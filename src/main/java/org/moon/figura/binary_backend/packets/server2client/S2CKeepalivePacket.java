package org.moon.figura.binary_backend.packets.server2client;

import org.moon.figura.binary_backend.ByteStreamConverter;
import org.moon.figura.binary_backend.packets.PacketType;

import java.io.IOException;
import java.nio.ByteBuffer;

public class S2CKeepalivePacket extends AbstractS2CPacket {

    String msg;

    @Override
    protected int getId() {
        return PacketType.S2C_KEEPALIVE.id;
    }

    @Override
    public void fill(ByteBuffer buf) throws IOException {
        msg = ByteStreamConverter.readString(buf);
    }
}
