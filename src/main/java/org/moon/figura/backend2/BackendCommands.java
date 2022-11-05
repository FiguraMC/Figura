package org.moon.figura.backend2;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class BackendCommands {

    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        //root
        LiteralArgumentBuilder<FabricClientCommandSource> backend = LiteralArgumentBuilder.literal("backend2");

        //force backend connection
        LiteralArgumentBuilder<FabricClientCommandSource> connect = LiteralArgumentBuilder.literal("connect");
        connect.executes(context -> {
            AuthHandler.auth(true);
            return 1;
        });
        backend.then(connect);

        //get user
        LiteralArgumentBuilder<FabricClientCommandSource> getUser = LiteralArgumentBuilder.literal("getUser");
        RequiredArgumentBuilder<FabricClientCommandSource, String> user = RequiredArgumentBuilder.argument("user", StringArgumentType.greedyString());
        user.executes(context -> {
            try {
                String id = StringArgumentType.getString(context, "user");
                NetworkStuff.getUser(UUID.fromString(id));
                return 1;
            } catch (Exception e) {
                context.getSource().sendError(Component.literal(e.getMessage()));
                return 0;
            }
        });
        getUser.then(user);
        backend.then(getUser);

        //return
        return backend;
    }
}
