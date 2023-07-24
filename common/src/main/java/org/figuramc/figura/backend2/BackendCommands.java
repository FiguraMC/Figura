package org.figuramc.figura.backend2;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.resources.FiguraRuntimeResources;
import org.figuramc.figura.utils.FiguraClientCommandSource;

public class BackendCommands {

    public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
        // root
        LiteralArgumentBuilder<FiguraClientCommandSource> backend = LiteralArgumentBuilder.literal("backend2");

        // force backend connection
        LiteralArgumentBuilder<FiguraClientCommandSource> connect = LiteralArgumentBuilder.literal("connect");
        connect.executes(context -> {
            NetworkStuff.reAuth();
            return 1;
        });

        backend.then(connect);

        // run
        LiteralArgumentBuilder<FiguraClientCommandSource> run = LiteralArgumentBuilder.literal("run");
        run.executes(context -> runRequest(context, ""));

        RequiredArgumentBuilder<FiguraClientCommandSource, String> request = RequiredArgumentBuilder.argument("request", StringArgumentType.greedyString());
        request.executes(context -> runRequest(context, StringArgumentType.getString(context, "request")));

        run.then(request);
        backend.then(run);

        // debug mode
        LiteralArgumentBuilder<FiguraClientCommandSource> debug = LiteralArgumentBuilder.literal("debug");
        debug.executes(context -> {
            NetworkStuff.debug = !NetworkStuff.debug;
            FiguraMod.sendChatMessage(Component.literal("Backend Debug Mode set to: " + NetworkStuff.debug).withStyle(NetworkStuff.debug ? ChatFormatting.GREEN : ChatFormatting.RED));
            return 1;
        });

        backend.then(debug);

        // check resources
        LiteralArgumentBuilder<FiguraClientCommandSource> resources = LiteralArgumentBuilder.literal("checkResources");
        resources.executes(context -> {
            context.getSource().figura$sendFeedback(Component.literal("Checking for resources..."));
            FiguraRuntimeResources.init().thenRun(() -> context.getSource().figura$sendFeedback(Component.literal("Resources checked!")));
            return 1;
        });

        backend.then(resources);

        // return
        return backend;
    }

    private static int runRequest(CommandContext<FiguraClientCommandSource> context, String request) {
        try {
            HttpAPI.runString(
                    NetworkStuff.api.header(request).build(),
                    (code, data) -> FiguraMod.sendChatMessage(Component.literal(data))
            );
            return 1;
        } catch (Exception e) {
            context.getSource().figura$sendError(Component.literal(e.getMessage()));
            return 0;
        }
    }
}
