package org.figuramc.figura.fsb_client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import org.figuramc.fsb2.api.ProtocolSession;
import org.figuramc.fsb2.api.packets.Packet;
import org.figuramc.fsb2.api.packets.Packets;
import org.figuramc.fsb2.server.versioned.ServerPacketImpl;

public class FSBClientPacketHandler {
    /**
     * Fabric PlayChannelHandler
     */
    public static void dispatchFabric(Minecraft ignored1, ClientPacketListener handler, FriendlyByteBuf buf, Object ignored2) {
        dispatch(handler, buf);
    }

    /**
     * got data? throw it at this method to dispatch it!
     * @param buf raw packet data
     */
    public static void dispatch(ClientPacketListener connection, FriendlyByteBuf buf) {
        // Re-use the buffer implementation from the server part so we don't need to copy-paste it
        ServerPacketImpl.Buf bufW = new ServerPacketImpl.Buf(buf);
        Packet<?> decode = Packets.decode(bufW, connection);
        Packets.dispatchPacket(decode, connection);
        if (ProtocolSession.lookup(connection) == null) {
            FSBClient.FSB_LOGGER.warn("throwing away {} because there is no session to receive it", decode);
        }
    }
}
