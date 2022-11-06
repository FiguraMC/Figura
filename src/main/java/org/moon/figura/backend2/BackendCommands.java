package org.moon.figura.backend2;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import org.moon.figura.FiguraMod;

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

        //run
        LiteralArgumentBuilder<FabricClientCommandSource> run = LiteralArgumentBuilder.literal("run");
        run.executes(context -> runRequest(context, ""));

        RequiredArgumentBuilder<FabricClientCommandSource, String> request = RequiredArgumentBuilder.argument("request", StringArgumentType.greedyString());
        request.executes(context -> runRequest(context, StringArgumentType.getString(context, "request")));

        run.then(request);
        backend.then(run);

        //return
        return backend;
    }

    private static int runRequest(CommandContext<FabricClientCommandSource> context, String request) {
        try {
            NetworkStuff.api.runString(
                    NetworkStuff.api.header(request).build(),
                    (code, data) -> FiguraMod.sendChatMessage(Component.literal(data))
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Component.literal(e.getMessage()));
            return 0;
        }
    }
}
