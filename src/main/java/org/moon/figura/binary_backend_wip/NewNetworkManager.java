package org.moon.figura.binary_backend_wip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.ClientTelemetryManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.moon.figura.FiguraMod;
import org.moon.figura.binary_backend_wip.packets.AbstractPacket;
import org.moon.figura.config.Config;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NewNetworkManager {

    private static BackendConnection currentConnection;
    private static BlockingQueue<Runnable> networkerActionQueue = new LinkedBlockingQueue<>();

    private static ConnectionStatus connectionStatus;
    private static String disconnectedReason;
    private static boolean banned = false;


    public static String getBackendAddress() {
        ServerAddress backendIP = ServerAddress.parseString(Config.BACKEND_IP.asString());
        return "ws://" + backendIP.getHost() + ":" + backendIP.getPort();
    }

    public static boolean hasConnection() {
        return currentConnection != null && currentConnection.isOpen();
    }

    public static void setConnection(BackendConnection connection) {
        //Ensure that all queued actions in currentConnection are completed before changing the connection object
        networkerActionQueue.add(() -> NewNetworkManager.currentConnection = connection);
    }

    public static void sendPacket(AbstractPacket packet) {
        if (hasConnection())
            currentConnection.sendPacket(packet);
        else
            FiguraMod.LOGGER.warn("Unable to send packet " + packet.getClass().getSimpleName() + " because there is no backend connection.");
    }

    public static void tick() {
        if (currentConnection != null)
            currentConnection.messageHandler.flushActionQueue();
        while (!networkerActionQueue.isEmpty())
            networkerActionQueue.poll().run();
    }

    public static void disconnectAuth(String reason) {
        networkerActionQueue.add(() -> {
            if (reason != null)
                disconnectedReason = reason;
            connectionStatus = ConnectionStatus.DISCONNECTED;
            authConnection = null;
        });
    }

    public static void disconnectBackend(String reason) {
        networkerActionQueue.add(() -> {
            if (reason != null)
                disconnectedReason = reason;
            connectionStatus = ConnectionStatus.DISCONNECTED;
            currentConnection = null;
        });
    }

    public static void closeBackend() {
        networkerActionQueue.add(() -> {
            if (currentConnection == null)
                return;

            if (!currentConnection.isOpen()) {
                currentConnection = null;
                return;
            }

            currentConnection.close();
            currentConnection = null;
        });
    }

    public static void openBackend() {
        auth(false);
        networkerActionQueue.add(() -> {
            if (authToken == null || hasConnection())
                return;

            currentConnection = new BackendConnection(URI.create(getBackendAddress()));
            currentConnection.connect();
        });
    }

    public static void assertBackend() {
        networkerActionQueue.add(() -> {
            if (!hasConnection())
                openBackend();
        });
    }


    private static final int RECONNECT = 5 * 60 * 20; //5 minutes
    private static int timeSinceLastAuth = 0;
    private static String authToken;
    public static final Gson NETWORK_GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static Connection authConnection;
    public static void auth(boolean force) {
        networkerActionQueue.add(() -> {
            try {
                timeSinceLastAuth = (int) (Math.random() * 300) - 150; //between -15 and +15 seconds

                if (!force && authToken != null || banned)
                    return;

                if (authConnection != null && !authConnection.isConnected())
                    authConnection.handleDisconnection();

                FiguraMod.LOGGER.info("Authenticating with " + FiguraMod.MOD_NAME + " server...");
                connectionStatus = ConnectionStatus.CONNECTING;

                Minecraft minecraft = Minecraft.getInstance();
                ClientTelemetryManager telemetryManager = minecraft.createTelemetryManager();

                ServerAddress authServer = ServerAddress.parseString(Config.AUTH_SERVER_IP.asString());
                InetSocketAddress inetSocketAddress = new InetSocketAddress(authServer.getHost(), authServer.getPort());
                Connection connection = Connection.connectToServer(inetSocketAddress, minecraft.options.useNativeTransport());

                connection.setListener(new ClientHandshakePacketListenerImpl(connection, minecraft, null, (text) -> FiguraMod.LOGGER.info(text.getString())) {
                    @Override
                    public void handleGameProfile(ClientboundGameProfilePacket clientboundGameProfilePacket) {
                        super.handleGameProfile(clientboundGameProfilePacket);
                        connection.setListener(new ClientPacketListener(minecraft, null, connection, clientboundGameProfilePacket.getGameProfile(), telemetryManager) {
                            @Override
                            public void onDisconnect(Component reason) {
                                telemetryManager.onDisconnect();
                                authConnection = null;
                                String reasonStr = reason.getString();

                                //parse token
                                String[] split = reasonStr.split("<", 2);
                                if (split.length < 2) {
                                    disconnectAuth(reasonStr);
                                    return;
                                }

                                split = split[1].split(">", 2);
                                if (split.length < 2) {
                                    disconnectAuth(reasonStr);
                                    return;
                                }

                                JsonObject token = new JsonObject();
                                token.addProperty("type", "auth");
                                token.addProperty("token", split[0]);

                                authToken = split[0];
                                closeBackend();
                                openBackend();
                            }
                        });
                    }

                    @Override
                    public void onDisconnect(Component reason) {
                        authConnection = null;
                        disconnectAuth(reason.getString());
                    }
                });

                connection.send(new ClientIntentionPacket(inetSocketAddress.getHostName(), inetSocketAddress.getPort(), ConnectionProtocol.LOGIN));
                connection.send(new ServerboundHelloPacket(minecraft.getUser().getName(), minecraft.getProfileKeyPairManager().preparePublicKey().join(), Optional.ofNullable(minecraft.getUser().getProfileId())));

                authConnection = connection;
            } catch (Exception e) {
                authConnection = null;
                disconnectAuth(e.getMessage());
            }
        });
    }

    public enum ConnectionStatus {
        DISCONNECTED(1),
        CONNECTING(2),
        CONNECTED(3);

        public final int val;
        ConnectionStatus(int val) {
            this.val = val;
        }
    }

}
