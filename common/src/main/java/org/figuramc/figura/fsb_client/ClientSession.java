package org.figuramc.figura.fsb_client;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.figuramc.figura.fsb_client.FSBClientEvents.ServerID;
import org.figuramc.fsb2.api.ProtocolSession;
import org.figuramc.fsb2.api.config.ServerIdentification;
import org.figuramc.fsb2.api.except.FSBStateException;
import org.figuramc.fsb2.api.packets.c2s.C2SHelloPacket;
import org.figuramc.fsb2.api.packets.s2c.S2CHelloPacket;
import org.figuramc.fsb2.api.packets.s2c.S2CReconfigurePacket;
import org.figuramc.fsb2.api.utils.FSBLogger;
import org.jetbrains.annotations.NotNull;

public class ClientSession extends ProtocolSession {
    public static final String INTEGRATED_SERVER_IP = "<local>";

    private final ClientPacketListener connection;
    private final String srvIP;
    private final String srvName;

    private volatile StateMachine state = StateMachine.HANDSHAKE;
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
                    logger.info("target policy for {} (named '{}') is {}", srvIP, srvName, policy);
                    state = switch (policy) {
                        case CONNECT -> StateMachine.CONNECTED;
                        case IGNORE -> StateMachine.INVISIBLE;
                        case ASK -> StateMachine.USER_REQUIRED;
                    };
                    if (policy == ConnectionPolicyManager.ConnectionPolicy.CONNECT) {
                        logger.info("Connection approved");
                        FSBClient.networking.sendTo(connection, new C2SHelloPacket());
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
