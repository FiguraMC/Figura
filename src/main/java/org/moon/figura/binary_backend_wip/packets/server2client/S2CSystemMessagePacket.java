package org.moon.figura.binary_backend_wip.packets.server2client;

import org.moon.figura.binary_backend_wip.ByteStreamConverter;
import org.moon.figura.binary_backend_wip.packets.PacketType;

import java.io.IOException;
import java.nio.ByteBuffer;

public class S2CSystemMessagePacket extends AbstractS2CPacket {

    public boolean force;
    public String message;

    @Override
    protected int getId() {
        return PacketType.S2C_SYSTEM_MESSAGE.id;
    }

    @Override
    public void fill(ByteBuffer buf) throws IOException {
        force = ByteStreamConverter.readBoolean(buf);
        message = ByteStreamConverter.readString(buf);
    }
}
