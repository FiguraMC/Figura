package org.moon.figura.backend;

import com.google.gson.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.jetbrains.annotations.NotNull;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.config.Config;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public class NetworkManager {

    public static final int AUTH_PORT = 25565;
    public static final int BACKEND_PORT = 25500;
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public static int backendStatus = 1;
    public static String disconnectedReason;
    public static boolean banned = false;

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
        if (banned) return;

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
                        disconnectedReason = reason.getString();
                        backendStatus = 1;
                        MessageHandler.handleMessage(reason.getString());
                    }
                });
            }
        });

        backendStatus = 2;
        authConnection.send(new ClientIntentionPacket(inetSocketAddress.getHostName(), inetSocketAddress.getPort(), ConnectionProtocol.LOGIN));
        authConnection.send(new ServerboundHelloPacket(minecraft.getUser().getName(), minecraft.getProfileKeyPairManager().profilePublicKeyData()));
    }

    //TODO
    public static void reAuth() {

    }

    public static boolean uploadAvatar(Avatar avatar, UUID id) {
        if (backend == null || !backend.isOpen())
            return false;

        JsonObject json = new JsonObject();
        json.addProperty("type", "upload");
        json.addProperty("id", id == null ? "avatar" : id.toString()); //todo - change to a random UUID when the multiple avatar system is done

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(avatar.nbt, baos);

            String bytes = Base64.getEncoder().encodeToString(baos.toByteArray());
            json.addProperty("data", bytes);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to upload avatar!", e);
            return false;
        }

        backend.send(GSON.toJson(json));
        return true;
    }

    public static boolean getAvatar(UUID id) {
        if (backend == null || !backend.isOpen())
            return false;

        JsonObject json = new JsonObject();
        json.addProperty("type", "download");
        json.addProperty("owner", id.toString());
        json.addProperty("id", "avatar"); // TODO

        backend.send(GSON.toJson(json));
        return true;
    }

    //command
    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        //root
        LiteralArgumentBuilder<FabricClientCommandSource> backend = LiteralArgumentBuilder.literal("backend");

        //force backend connection
        LiteralArgumentBuilder<FabricClientCommandSource> connect = LiteralArgumentBuilder.literal("connect");
        connect.executes(context -> {
            NetworkManager.auth();
            return 1;
        });

        //token
        RequiredArgumentBuilder<FabricClientCommandSource, String> token = RequiredArgumentBuilder.argument("token", StringArgumentType.word());
        token.executes(context -> {
            JsonObject json = new JsonObject();
            json.addProperty("type", "auth");
            json.addProperty("token", StringArgumentType.getString(context, "token"));
            MessageHandler.handleMessage(GSON.toJson(json));
            return 1;
        });

        //add arguments
        connect.then(token);

        //message sender

        //root
        LiteralArgumentBuilder<FabricClientCommandSource> message = LiteralArgumentBuilder.literal("message");

        //type argument
        RequiredArgumentBuilder<FabricClientCommandSource, String> messageType = RequiredArgumentBuilder.argument("messageType", StringArgumentType.word());
        messageType.executes(context -> {
            JsonObject json = new JsonObject();
            json.addProperty("type", StringArgumentType.getString(context, "messageType"));
            NetworkManager.backend.send(GSON.toJson(json));
            return 1;
        });

        //value argument
        RequiredArgumentBuilder<FabricClientCommandSource, String> value = RequiredArgumentBuilder.argument("value", StringArgumentType.greedyString());
        value.executes(context -> {
            String t = StringArgumentType.getString(context, "messageType");
            String v = StringArgumentType.getString(context, "value");

            JsonObject json = new JsonObject();
            json.addProperty("type", t);

            JsonObject object = JsonParser.parseString(v).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet())
                json.add(entry.getKey(), entry.getValue());

            NetworkManager.backend.send(GSON.toJson(json));
            return 1;
        });

        //add arguments
        messageType.then(value);
        message.then(messageType);

        //add commands to root
        backend.then(connect);
        backend.then(message);

        return backend;
    }
}
