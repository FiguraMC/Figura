package org.figuramc.figura.fsb_client;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.fsb2.api.FSBConstants;
import org.figuramc.fsb2.api.PlayerSession;
import org.figuramc.fsb2.api.ProtocolSession;
import org.figuramc.fsb2.api.except.FSBArgumentException;
import org.figuramc.fsb2.api.except.FSBStateException;
import org.figuramc.fsb2.api.packets.Packet;
import org.figuramc.fsb2.api.utils.FSBLogger;
import org.figuramc.fsb2.api.utils.LoggingProxy;
import org.figuramc.fsb2.server.versioned.ServerPacketImpl;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class FSBClient {
    /**
     * FSB session information.
     */
    private static @Nullable ProtocolSession clientSession = null;
    private static final Logger LOG_BACKEND = LoggerFactory.getLogger(FSBClient.class);
    public static final FSBLogger FSB_LOGGER = new LoggingProxy(LOG_BACKEND);

    public static final ResourceLocation PACKET_ID = new ResourceLocation(
            FSBConstants.MOD_NAMESPACE,
            FSBConstants.FSB_PACKET_PATH
    );

    public static synchronized void init() {
        // Handlers
        FSBClientEvents.INSTANCE.SERVER_CONNECTED.register(FSBClient::handleInitialConnection, 0);
    }

    public static synchronized void newSession(ClientPacketListener relation) {
        if (clientSession != null) terminateSession(relation);
        FSB_LOGGER.info("Created new client FSB session @ {}", relation);
        clientSession = new ClientSession(FSB_LOGGER, relation);
        try {
            clientSession.relate(relation, PlayerSession.SERVER);
        } catch (FSBArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void terminateSession(ClientPacketListener relation) {
        if (clientSession == null) return;
        FSB_LOGGER.info("Terminated client FSB session @ {}", relation);
        clientSession.unrelate(relation);
        clientSession = null;
    }

    public static synchronized @Nullable ProtocolSession clientSession() {
        return clientSession;
    }

    public sealed interface NetworkingInterface {
        /**
         * Format & send a {@code packet} through the {@code connection}.
         */
        void sendTo(ClientPacketListener connection, Packet<?> packet);

        /**
         * Format & send a {@code packet} to the currently connected server.
         *
         * @throws FSBStateException when not connected to anything
         */
        void send(Packet<?> packet) throws FSBStateException;
    }

    public static void handleInitialConnection(FSBClientEvents.ServerID event, AtomicReference<ConnectionPolicyManager.ConnectionPolicy> out) {
        // AtomicRef is only being used as a box here, so threading issues do not apply.
        if (out.get() == null)
            out.set(ConnectionPolicyManager.get().query(event.ip));
    }

    public static final NetworkingInterface networking = new NetworkingInterfaceImpl();

    private static final class NetworkingInterfaceImpl implements NetworkingInterface {
        @Override
        public void sendTo(ClientPacketListener connection, Packet<?> packet) {
            ServerPacketImpl.Buf bufW = new ServerPacketImpl.Buf(new FriendlyByteBuf(Unpooled.buffer()));
            bufW.writeByteArray(packet.identify().netID);
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
