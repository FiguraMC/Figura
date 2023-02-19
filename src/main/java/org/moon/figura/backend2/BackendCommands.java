package org.moon.figura.backend2;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.resources.FiguraRuntimeResources;

public class BackendCommands {

    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        //root
        LiteralArgumentBuilder<FabricClientCommandSource> backend = LiteralArgumentBuilder.literal("backend2");

        //force backend connection
        LiteralArgumentBuilder<FabricClientCommandSource> connect = LiteralArgumentBuilder.literal("connect");
        connect.executes(context -> {
            NetworkStuff.reAuth();
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

        //debug mode
        LiteralArgumentBuilder<FabricClientCommandSource> debug = LiteralArgumentBuilder.literal("debug");
        debug.executes(context -> {
            NetworkStuff.debug = !NetworkStuff.debug;
            FiguraMod.sendChatMessage(new TextComponent("Backend Debug Mode set to: " + NetworkStuff.debug).withStyle(NetworkStuff.debug ? ChatFormatting.GREEN : ChatFormatting.RED));
            return 1;
        });

        backend.then(debug);

        //check resources
        LiteralArgumentBuilder<FabricClientCommandSource> resources = LiteralArgumentBuilder.literal("checkResources");
        resources.executes(context -> {
            context.getSource().sendFeedback(Component.literal("Checking for resources..."));
            FiguraRuntimeResources.init();
            return 1;
        });

        backend.then(resources);

        //return
        return backend;
    }

    private static int runRequest(CommandContext<FabricClientCommandSource> context, String request) {
        try {
            HttpAPI.runString(
                    NetworkStuff.api.header(request).build(),
                    (code, data) -> FiguraMod.sendChatMessage(new TextComponent(data))
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(new TextComponent(e.getMessage()));
            return 0;
        }
    }
}
