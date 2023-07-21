package org.figuramc.figura.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;

import java.nio.file.Path;

class LoadCommand {

    public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
        LiteralArgumentBuilder<FiguraClientCommandSource> load = LiteralArgumentBuilder.literal("load");

        RequiredArgumentBuilder<FiguraClientCommandSource, String> path = RequiredArgumentBuilder.argument("path", StringArgumentType.greedyString());
        path.executes(LoadCommand::loadAvatar);

        return load.then(path);
    }

    private static int loadAvatar(CommandContext<FiguraClientCommandSource> context) {
        String str = StringArgumentType.getString(context, "path");
        try {
            // parse path
            Path p = LocalAvatarFetcher.getLocalAvatarDirectory().resolve(Path.of(str));

            // try to load avatar
            AvatarManager.loadLocalAvatar(p);
            context.getSource().figura$sendFeedback(FiguraText.of("command.load.loading"));
            return 1;
        } catch (Exception e) {
            context.getSource().figura$sendError(FiguraText.of("command.load.invalid", str));
        }

        return 0;
    }
}
