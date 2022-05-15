package org.moon.figura.backend;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.jetbrains.annotations.NotNull;
import org.moon.figura.FiguraMod;

import java.net.InetSocketAddress;

public class NetworkManager {

    private static final int AUTH_PORT = 25565;

    private static Connection authConnection;

    public static void tick() {
        if (authConnection != null) {
            if (authConnection.isConnected())
                authConnection.tick();
            else {
                authConnection.handleDisconnection();
                authConnection = null;
            }
        }
    }

    //TODO - config
    public static String getBackendAddress() {
        return "79.114.8.27"; //"127.0.0.1";
    }

    public static void auth() {
        Minecraft minecraft = Minecraft.getInstance();

        InetSocketAddress inetSocketAddress = new InetSocketAddress(getBackendAddress(), AUTH_PORT);
        authConnection = Connection.connectToServer(inetSocketAddress, minecraft.options.useNativeTransport());
        authConnection.setListener(new ClientHandshakePacketListenerImpl(authConnection, minecraft, null, (text) -> FiguraMod.LOGGER.info(text.getString())) {
            @Override
            public void handleGameProfile(@NotNull ClientboundGameProfilePacket clientboundGameProfilePacket) {
                //Do nothing. The superclass sets the listener to a new listener, getting rid of ours. So we set the listener back.
                super.handleGameProfile(clientboundGameProfilePacket);
                authConnection.setListener(new ClientPacketListener(minecraft, null, authConnection, clientboundGameProfilePacket.getGameProfile(), minecraft.createTelemetryManager()) {
                    @Override
                    public void onDisconnect(@NotNull Component reason) {
                        FiguraMod.sendChatMessage(reason);
                        System.out.println(reason.getString());
                    }
                });
            }
        });

        authConnection.send(new ClientIntentionPacket(inetSocketAddress.getHostName(), inetSocketAddress.getPort(), ConnectionProtocol.LOGIN));
        authConnection.send(new ServerboundHelloPacket(minecraft.getUser().getGameProfile()));
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> connect = LiteralArgumentBuilder.literal("connect");
        connect.executes(context -> {
            NetworkManager.auth();
            return 1;
        });
        return connect;
    }
}
