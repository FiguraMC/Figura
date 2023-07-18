package org.figuramc.figura.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
<<<<<<< HEAD:src/main/java/org/moon/figura/commands/FiguraLoadCommand.java
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.local.LocalAvatarFetcher;
import org.moon.figura.utils.FiguraText;
=======
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:common/src/main/java/org/figuramc/figura/commands/LoadCommand.java

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
            //parse path
            Path p = LocalAvatarFetcher.getLocalAvatarDirectory().resolve(Path.of(str));

            //try to load avatar
            AvatarManager.loadLocalAvatar(p);
<<<<<<< HEAD:src/main/java/org/moon/figura/commands/FiguraLoadCommand.java
            context.getSource().sendFeedback(new FiguraText("command.load.loading"));
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(new FiguraText("command.load.invalid", str));
=======
            context.getSource().figura$sendFeedback(new FiguraText("command.load.loading"));
            return 1;
        } catch (Exception e) {
            context.getSource().figura$sendError(new FiguraText("command.load.invalid", str));
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:common/src/main/java/org/figuramc/figura/commands/LoadCommand.java
        }

        return 0;
    }
}
