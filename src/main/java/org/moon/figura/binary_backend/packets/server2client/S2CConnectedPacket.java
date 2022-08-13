package org.moon.figura.binary_backend.packets.server2client;

import org.moon.figura.binary_backend.ByteStreamConverter;
import org.moon.figura.binary_backend.packets.PacketType;

import java.io.IOException;
import java.nio.ByteBuffer;

public class S2CConnectedPacket extends AbstractS2CPacket {

    public int maxAvatarSize, maxAvatars, pingSize, pingRate, equip, upload, download;

    @Override
    protected int getId() {
        return PacketType.S2C_CONNECTED.id;
    }

    @Override
    public void fill(ByteBuffer buf) throws IOException {

        //String json = ByteStreamConverter.readString(buf);
        //Option is there to still use json in certain places
        //especially ones like this, where it only gets sent once per connection,
        //and adding new settings later might be important!

        //For now though, not going to use json. Just remember it's always possible still.

        maxAvatarSize = ByteStreamConverter.readVarInt(buf);
        maxAvatars = ByteStreamConverter.readVarInt(buf);

        pingSize = ByteStreamConverter.readVarInt(buf);
        pingRate = ByteStreamConverter.readVarInt(buf);

        equip = ByteStreamConverter.readVarInt(buf);
        upload = ByteStreamConverter.readVarInt(buf);
        download = ByteStreamConverter.readVarInt(buf);
    }
}
