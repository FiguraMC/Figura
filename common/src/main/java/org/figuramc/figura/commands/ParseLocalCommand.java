package org.figuramc.figura.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.parsers.Buwwet.FiguraModelParser;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ParseLocalCommand {
    // Parse a local file
    public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
        LiteralArgumentBuilder<FiguraClientCommandSource> parse = LiteralArgumentBuilder.literal("parse");

        RequiredArgumentBuilder<FiguraClientCommandSource, String> path = RequiredArgumentBuilder.argument("path", StringArgumentType.greedyString());
        path.executes(ParseLocalCommand::parseAvatar);

        return parse.then(path);
    }

    private static int parseAvatar(CommandContext<FiguraClientCommandSource> context) {
        String str = StringArgumentType.getString(context, "path");


        CompletableFuture.runAsync(() -> {
            try {
                Path moon_path = FiguraMod.getFiguraDirectory().resolve(str);

                // Read and parse
                CompoundTag avatar = NbtIo.readCompressed(Files.newInputStream(moon_path));
                String avatar_name = FiguraModelParser.parseAvatar(avatar);

                FiguraToast.sendToast(Component.literal("Parsed " + avatar_name + " successfully."));


                } catch (Exception e) {
                FiguraMod.LOGGER.error("Error while parsing local avatar: " + e);
                FiguraToast.sendToast(Component.literal(e.toString()));
            }

        });

        return 1;
    }
}
