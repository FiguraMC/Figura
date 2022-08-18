package org.moon.figura.binary_backend_wip.packets.client2server;

import org.moon.figura.binary_backend_wip.ByteStreamConverter;
import org.moon.figura.binary_backend_wip.packets.PacketType;

import java.io.DataOutputStream;
import java.io.IOException;

public class C2SAuthTokenPacket extends AbstractC2SPacket {

    public String authToken;

    @Override
    protected int getId() {
        return PacketType.C2S_AUTH_TOKEN.id;
    }

    @Override
    protected void writeData(DataOutputStream dos) throws IOException {
        ByteStreamConverter.writeString(dos, authToken);
    }
}
