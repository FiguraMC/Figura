package org.figuramc.figura.fsb_client;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.figuramc.figura.fsb_client.FSBClientEvents.ServerID;
import org.figuramc.fsb2.api.ProtocolSession;
import org.figuramc.fsb2.api.config.ServerIdentification;
import org.figuramc.fsb2.api.except.FSBStateException;
import org.figuramc.fsb2.api.packets.Packet;
import org.figuramc.fsb2.api.packets.c2s.C2SHelloPacket;
import org.figuramc.fsb2.api.packets.s2c.S2CHelloPacket;
import org.figuramc.fsb2.api.packets.s2c.S2CReconfigurePacket;
import org.figuramc.fsb2.api.utils.FSBLogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.figuramc.figura.fsb_client.FSBClient.networking;

public class ClientSession extends ProtocolSession {
    public static final String INTEGRATED_SERVER_IP = "<local>";
    private static final Logger log = LoggerFactory.getLogger(ClientSession.class);

    private final ClientPacketListener connection;
    private final String srvIP;
    private final String srvName;

    private volatile StateMachine state = StateMachine.HANDSHAKE;
    private volatile long lastPacketTime;
    private volatile ServerIdentification serverData = null;

    public ClientSession(@NotNull FSBLogger logger, ClientPacketListener connection) {
        super(logger, connection, true);
        this.connection = connection;
        if (connection.getServerData() != null) {
            this.srvIP = connection.getServerData().ip;
            this.srvName = connection.getServerData().name;
        } else {
            this.srvIP = INTEGRATED_SERVER_IP;
            this.srvName = "integrated server";
        }
        this.configureEventHandlers();
    }

    @Override
    public <T extends Packet<?>> void handlePacket(T packet, Object context) {
        lastPacketTime = System.nanoTime();
        super.handlePacket(packet, context);
    }

    /**
     * Returns {@code true} if there has been no packets for 10+ seconds. (The heartbeat should at least cover this.)
     */
    public boolean isConnectionTimedOut() {
        return System.nanoTime() - lastPacketTime > 10_000_000_000L;
    }

    private void configureEventHandlers() {
        try {
            onReceive(S2CHelloPacket.REC, this::onHello);
            onReceive(S2CReconfigurePacket.REC, this::onReconfigure);
        } catch (FSBStateException e) {
            // We REALLY don't want these to fail. Be loud and obnoxious.
            // Mixiners: Just mixin to the handler functions please
            CrashReport oops = CrashReport.forThrowable(e, "configuring FSB session (client side)");
            throw new ReportedException(oops);
        }
    }

    public StateMachine getState() {
        return state;
    }

    public void connectAnnounce() {
        logger.info("Connecting.");
        networking.sendTo(connection, new C2SHelloPacket());
        state = StateMachine.CONNECTED;
    }

    public void disconnectAnnounce() {
        logger.info("Disconnecting.");
        // TODO: announce disconnect to server
        state = StateMachine.INVISIBLE;
    }

    private void onHello(S2CHelloPacket packet, Object ignored) {
        if (state != StateMachine.HANDSHAKE) return;
        serverData = packet.serverId;
        // TODO: Prompt user
        FSBClientEvents.INSTANCE.SERVER_CONNECTED.dispatch(new ServerID(serverData, srvIP, srvName))
                .thenAccept(policy -> {
                    if (policy == null) {
                        logger.warn("SERVER_CONNECTED event dispatch resulted in no result. defaulting to ASK, but one or more of your addons is broken");
                        policy = ConnectionPolicyManager.ConnectionPolicy.ASK;
                    }
                    logger.info("target policy for '{}' is {}", srvName, policy);
                    switch (policy) {
                        case CONNECT -> connectAnnounce();
                        case IGNORE -> state = StateMachine.INVISIBLE;
                        case ASK -> state = StateMachine.USER_REQUIRED;
                    }
                });
    }

    private void onReconfigure(S2CReconfigurePacket packet, Object ignored) {
        // We accept this packet in all states in order to display up-to-date information in the UI
        serverData = packet.serverId;
        FSBClientEvents.INSTANCE.SERVER_RECONFIGURED.dispatch(new ServerID(serverData, srvIP, srvName));
    }

    public ServerIdentification getServerData() {
        return serverData;
    }

    /**
     * <b>This is probably not what you want.</b> Use one of the capability checking methods (<code>supports<i>XYZ</i></code>) instead.
     * <p>
     * Returns if this client is in the "connected" state.
     */
    public boolean isConnected() {
        return state == StateMachine.CONNECTED;
    }

    /**
     * @return {@code true} if the server is connected and says it supports downloading avatars.
     */
    public boolean supportsDownloading() {
        return isConnected() && serverData.supportsDownloading;
    }

    /**
     * @return {@code true} if the server is connected and says it supports uploading avatars.
     */
    public boolean supportsUploading() {
        return isConnected() && serverData.supportsUploading;
    }

    /**
     * @return {@code true} if the server is connected and says it supports proxying pings.
     */
    public boolean supportsPings() {
        return isConnected() && serverData.supportsPings;
    }

    /**
     * @return {@code true} if the server is connected and says it supports server packets.
     */
    public boolean supportsCustomPackets() {
        return isConnected() && serverData.supportsCustomPackets;
    }

    public enum StateMachine {
        /**
         * Do not tell the server we're connected. Don't send packets in response to anything.
         * This leaves the connection in a half-open state, where the server doesn't think we are compatible.
         * <p>
         * This is the user rejection state (either the server is set to *deny*, or they rejected the prompt)
         */
        INVISIBLE(false, false),
        /**
         * There might not be a server connected. Wait for something to tell us that there is.
         */
        HANDSHAKE(true, false),
        /**
         * The user needs to choose what to do. Act like {@link #INVISIBLE} until then.
         */
        USER_REQUIRED(false, false),
        /**
         * Full functionality.
         */
        CONNECTED(false, true);

        public final boolean acceptConnections;
        public final boolean interactive;

        StateMachine(boolean acceptConnections, boolean interactive) {
            this.acceptConnections = acceptConnections;
            this.interactive = interactive;
        }
    }
}
