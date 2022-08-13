package org.moon.figura.binary_backend.packets;

import org.moon.figura.binary_backend.ByteStreamConverter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class AbstractPacket {

    public AbstractPacket() {}

    protected abstract int getId();
    protected abstract void writeData(DataOutputStream dos) throws IOException;
    public final void write(DataOutputStream dos) throws IOException {
        ByteStreamConverter.writeVarInt(dos, getId());
        writeData(dos);
    }
    public abstract void fill(ByteBuffer buf) throws IOException;


}
