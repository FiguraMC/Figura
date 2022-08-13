package org.moon.figura.binary_backend.packets.client2server;

import org.moon.figura.binary_backend.packets.AbstractPacket;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class AbstractC2SPacket extends AbstractPacket {
    @Override
    public void fill(ByteBuffer buf) throws IOException {
        throw new IOException("Tried to fill data on a C2S packet");
    }
}
