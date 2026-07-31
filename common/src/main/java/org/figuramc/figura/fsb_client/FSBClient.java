package org.figuramc.figura.fsb_client;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.fsb2.api.FSBConstants;
import org.figuramc.fsb2.api.ProtocolSession;
import org.figuramc.fsb2.api.except.FSBStateException;
import org.figuramc.fsb2.api.packets.Packet;
import org.figuramc.fsb2.api.utils.FSBLogger;
import org.figuramc.fsb2.api.utils.LoggingProxy;
import org.figuramc.fsb2.server.versioned.ServerPacketImpl;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FSBClient {
    /**
     * FSB session information.
     */
    private static @Nullable ProtocolSession clientSession = null;
    private static final Logger LOG_BACKEND = LoggerFactory.getLogger(FSBClient.class);
    public static final FSBLogger FSB_LOGGER = new LoggingProxy(LOG_BACKEND);

    public static final ResourceLocation PACKET_ID = new ResourceLocation(FSBConstants.MOD_NAMESPACE, FSBConstants.FSB_PACKET_PATH);

    @CheckReturnValue
    public static synchronized boolean newSession() {
        if (clientSession != null) return false;
        clientSession = new ProtocolSession(FSB_LOGGER, Minecraft.getInstance(), true);
        return true;
    }

    public static synchronized boolean terminateSession() {
        if (clientSession == null) return false;
        clientSession.unrelate(Minecraft.getInstance());
        clientSession = null;
        return true;
    }

    public static synchronized @Nullable ProtocolSession clientSession() {
        return clientSession;
    }

    public sealed interface NetworkingInterface {
        void sendTo(ClientPacketListener connection, Packet<?> packet);

        /**
         * Send a packet to the connected server.
         * @throws FSBStateException when not connected to anything
         */
        void send(Packet<?> packet) throws FSBStateException;

        /**
         * {@link #send} but it blows up in your face if there's no connection
         */
        default void assertSend(Packet<?> packet) {
            try {
                send(packet);
            } catch (FSBStateException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static final NetworkingInterface networking = new NetworkingInterfaceImpl();

    private static final class NetworkingInterfaceImpl implements NetworkingInterface {
        @Override
        public void sendTo(ClientPacketListener connection, Packet<?> packet) {
            ServerPacketImpl.Buf bufW = new ServerPacketImpl.Buf(new FriendlyByteBuf(Unpooled.buffer()));
            packet.write(bufW);
            connection.send(new ServerboundCustomPayloadPacket(PACKET_ID, bufW.actual()));
        }

        @Override
        public void send(Packet<?> packet) throws FSBStateException {
            Minecraft instance = Minecraft.getInstance();
            ClientPacketListener connection = instance.getConnection();
            if (connection == null) throw new FSBStateException("Not connected to anything for sending that packet");
            sendTo(connection, packet);
        }
    }

}
