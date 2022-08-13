package org.moon.figura.binary_backend.packets.server2client;

import org.moon.figura.binary_backend.packets.AbstractPacket;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractS2CPacket extends AbstractPacket {
    @Override
    protected void writeData(DataOutputStream dos) throws IOException {
        throw new IOException("Tried to write data on an S2C packet");
    }
}
