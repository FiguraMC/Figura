package org.figuramc.figura.fsb_client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import org.figuramc.fsb2.api.packets.Packet;
import org.figuramc.fsb2.api.packets.Packets;
import org.figuramc.fsb2.api.packets.c2s.C2SHelloPacket;
import org.figuramc.fsb2.api.packets.s2c.S2CHelloPacket;
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
        if (!sessionlessDispatch(decode, connection))
            Packets.dispatchPacket(decode, connection);
    }

    /**
     * Clients have the need to create and destroy sessions throughout
     * their lifetime. Since there might not be a session to dispatch the packet to,
     * this method handles creating and deleting sessions on request.
     *
     * @return {@code true} if the packet was handled by this method.
     */
    private static boolean sessionlessDispatch(Packet<?> packet, ClientPacketListener connection) {
        // Switch on packet type would be Great! We aren't at that language level, though.
        if (packet instanceof S2CHelloPacket s2cHello) {
            if (FSBClient.newSession()) {
                // TODO: Prompt user, ack, destroy session?
                FSBClient.FSB_LOGGER.info("new session created {}", FSBClient.clientSession());
                FSBClient.networking.sendTo(connection, new C2SHelloPacket());
            }
        }
        return false;
    }
}
