package org.moon.figura.backend2;

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
import org.moon.figura.config.Config;

import java.net.InetSocketAddress;
import java.util.Optional;

public class AuthHandler {

    private static final int RECONNECT = 6000; //5 min

    private static int lastAuth = 0;
    private static Connection authConnection;

    public static void tick() {
        //auth ticking
        if (authConnection != null) {
            if (authConnection.isConnected())
                authConnection.tick();
            else {
                authConnection.handleDisconnection();
                authConnection = null;
            }
        }

        //re auth
        lastAuth++;
        if (lastAuth >= RECONNECT)
            auth(false);
    }

    public static void auth(boolean reAuth) {
        NetworkStuff.async(() -> {
            try {
                lastAuth = (int) (Math.random() * 600) - 300; //between -15 and +15 seconds

                if (!reAuth && NetworkStuff.backendStatus == 3)
                    return;

                if (authConnection != null && !authConnection.isConnected())
                    authConnection.handleDisconnection();

                FiguraMod.LOGGER.info("Authenticating with " + FiguraMod.MOD_NAME + " server...");
                NetworkStuff.backendStatus = 2;

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
                                String dc = reason.getString();

                                //parse token
                                String[] split = dc.split("<", 2);
                                if (split.length < 2) {
                                    handleDc(dc);
                                    return;
                                }

                                split = split[1].split(">", 2);
                                if (split.length < 2) {
                                    handleDc(dc);
                                    return;
                                }

                                authConnection = null;
                                NetworkStuff.disconnectedReason = null;
                                NetworkStuff.api = new API(split[0]);
                                NetworkStuff.backendStatus = 3;
                                FiguraMod.debug("Successfully created backend Http API!");
                            }
                        });
                    }

                    @Override
                    public void onDisconnect(Component reason) {
                        handleDc(reason.getString());
                    }
                });

                connection.send(new ClientIntentionPacket(inetSocketAddress.getHostName(), inetSocketAddress.getPort(), ConnectionProtocol.LOGIN));
                connection.send(new ServerboundHelloPacket(minecraft.getUser().getName(), minecraft.getProfileKeyPairManager().preparePublicKey().join(), Optional.ofNullable(minecraft.getUser().getProfileId())));

                authConnection = connection;
            } catch (Exception e) {
                handleDc(e.getMessage());
            }
        });
    }

    private static void handleDc(String reason) {
        authConnection = null;
        NetworkStuff.disconnectedReason = reason;
        NetworkStuff.backendStatus = 1;
        NetworkStuff.api = null;
        FiguraMod.debug("Failed to create the backend Http API!");
    }
}
