package org.moon.figura.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.jetbrains.annotations.NotNull;
import org.moon.figura.FiguraMod;
import org.moon.figura.lua.docs.FiguraDocsManager;

import java.net.InetSocketAddress;

public class FiguraCommands {

    public static Connection authConnection;

    public static void init() {
        //root
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal(FiguraMod.MOD_ID);

        //docs
        root.then(FiguraDocsManager.get());

        //links
        root.then(FiguraLinkCommand.get());

        //run
        root.then(FiguraRunCommand.get());

        //load
        root.then(FiguraLoadCommand.get());


        //TODO: remove, connection test
        LiteralArgumentBuilder<FabricClientCommandSource> connect = LiteralArgumentBuilder.literal("connect");
        connect.executes(context -> {
            Minecraft minecraft = Minecraft.getInstance();

            int AUTH_PORT = 25565;
            String backendAddress = "79.114.8.27";
            //String backendAddress = "127.0.0.1";

            InetSocketAddress inetSocketAddress = new InetSocketAddress(backendAddress, AUTH_PORT);
            authConnection = Connection.connectToServer(inetSocketAddress, minecraft.options.useNativeTransport());
            authConnection.setListener(new ClientHandshakePacketListenerImpl(authConnection, minecraft, null, (text) -> FiguraMod.LOGGER.info(text.getString())) {
                @Override
                public void handleGameProfile(@NotNull ClientboundGameProfilePacket clientboundGameProfilePacket) {
                    //Do nothing. The superclass sets the listener to a new listener, getting rid of ours. So we set the listener back.
                    super.handleGameProfile(clientboundGameProfilePacket);
                    authConnection.setListener(new ClientPacketListener(Minecraft.getInstance(), null, authConnection, clientboundGameProfilePacket.getGameProfile(), Minecraft.getInstance().createTelemetryManager()) {
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

            return 1;
        });

        //Don't add the command. Sorry users :p backend isn't ready yet
        //root.then(connect);


        //register
        ClientCommandManager.DISPATCHER.register(root);


    }
}
