package org.moon.figura.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.avatars.providers.LocalAvatarFetcher;
import org.moon.figura.utils.FiguraText;

import java.nio.file.Path;

public class FiguraLoadCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> get() {
        LiteralArgumentBuilder<FabricClientCommandSource> load = LiteralArgumentBuilder.literal("load");

        RequiredArgumentBuilder<FabricClientCommandSource, String> path = RequiredArgumentBuilder.argument("path", StringArgumentType.greedyString());
        path.executes(context -> {
            String str = StringArgumentType.getString(context, "path");
            try {
                //parse path
                Path p = LocalAvatarFetcher.getLocalAvatarDirectory().resolve(Path.of(str));

                //return on success
                if (AvatarManager.loadLocalAvatar(p)) {
                    context.getSource().sendFeedback(new FiguraText("command.load.success"));
                    return 1;
                }

                //send error on fail
                context.getSource().sendError(new FiguraText("command.load.error", str));
                return 0;
            } catch (Exception e) {
                context.getSource().sendError(new FiguraText("command.load.invalid", str));
            }

            return 0;
        });

        return load.then(path);
    }
}
