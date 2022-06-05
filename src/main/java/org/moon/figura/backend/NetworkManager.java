package org.moon.figura.backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
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
import org.moon.figura.config.Config;

import java.net.InetSocketAddress;

public class NetworkManager {

    public static final int AUTH_PORT = 25565;
    public static final int BACKEND_PORT = 25500;
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    protected static Connection authConnection;
    protected static WebsocketManager backend;

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

    public static void auth() {
        Minecraft minecraft = Minecraft.getInstance();

        InetSocketAddress inetSocketAddress = new InetSocketAddress((String) Config.BACKEND.value, AUTH_PORT);
        authConnection = Connection.connectToServer(inetSocketAddress, minecraft.options.useNativeTransport());
        authConnection.setListener(new ClientHandshakePacketListenerImpl(authConnection, minecraft, null, (text) -> FiguraMod.LOGGER.info(text.getString())) {
            @Override
            public void handleGameProfile(@NotNull ClientboundGameProfilePacket clientboundGameProfilePacket) {
                //Do nothing. The superclass sets the listener to a new listener, getting rid of ours. So we set the listener back.
                super.handleGameProfile(clientboundGameProfilePacket);
                authConnection.setListener(new ClientPacketListener(minecraft, null, authConnection, clientboundGameProfilePacket.getGameProfile(), minecraft.createTelemetryManager()) {
                    @Override
                    public void onDisconnect(@NotNull Component reason) {
                        MessageHandler.handleMessage(reason.getString());
                    }
                });
            }
        });

        authConnection.send(new ClientIntentionPacket(inetSocketAddress.getHostName(), inetSocketAddress.getPort(), ConnectionProtocol.LOGIN));
        authConnection.send(new ServerboundHelloPacket(minecraft.getUser().getName(), minecraft.getProfileKeyPairManager().profilePublicKeyData()));
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        //root
        LiteralArgumentBuilder<FabricClientCommandSource> backend = LiteralArgumentBuilder.literal("backend");

        //force backend connection
        LiteralArgumentBuilder<FabricClientCommandSource> connect = LiteralArgumentBuilder.literal("connect");
        connect.executes(context -> {
            NetworkManager.auth();
            return 1;
        });

        //message sender

        //root
        LiteralArgumentBuilder<FabricClientCommandSource> message = LiteralArgumentBuilder.literal("message");

        //type argument
        RequiredArgumentBuilder<FabricClientCommandSource, String> messageType = RequiredArgumentBuilder.argument("messageType", StringArgumentType.word());
        messageType.executes(context -> {
            String t = StringArgumentType.getString(context, "messageType");
            return messageCommand(t, null, null);
        });

        //value argument
        RequiredArgumentBuilder<FabricClientCommandSource, String> valueType = RequiredArgumentBuilder.argument("valueType", StringArgumentType.word());

        RequiredArgumentBuilder<FabricClientCommandSource, String> value = RequiredArgumentBuilder.argument("value", StringArgumentType.greedyString());
        value.executes(context -> {
            String t = StringArgumentType.getString(context, "messageType");
            String vt = StringArgumentType.getString(context, "valueType");
            String v = StringArgumentType.getString(context, "value");
            return messageCommand(t, vt, v);
        });

        //add arguments
        valueType.then(value);
        messageType.then(valueType);
        message.then(messageType);

        //add commands to root
        backend.then(connect);
        backend.then(message);

        return backend;
    }

    private static int messageCommand(String type, String valueType, String value) {
        if (NetworkManager.backend == null || !NetworkManager.backend.isOpen())
            return 0;

        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        if (valueType != null && value != null)
            json.addProperty(valueType, value);

        NetworkManager.backend.send(GSON.toJson(json));
        return 1;
    }
}
