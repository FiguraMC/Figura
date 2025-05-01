// Decompiled with: Procyon 0.6.0
// Class Version: 17
package org.figuramc.figura.commands;

import net.minecraft.nbt.*;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.parsers.Buwwet.FiguraModelParser;
import net.minecraft.nbt.NbtIo;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import org.figuramc.figura.FiguraMod;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class ParseLocalCommand
{
    public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
        final LiteralArgumentBuilder<FiguraClientCommandSource> parse = (LiteralArgumentBuilder<FiguraClientCommandSource>)LiteralArgumentBuilder.literal("parse");
        final RequiredArgumentBuilder<FiguraClientCommandSource, String> path = (RequiredArgumentBuilder<FiguraClientCommandSource, String>)RequiredArgumentBuilder.argument("path", (ArgumentType)StringArgumentType.greedyString());
        path.executes(ParseLocalCommand::parseAvatar);
        return (LiteralArgumentBuilder<FiguraClientCommandSource>)parse.then((ArgumentBuilder)path);
    }
    
    private static int parseAvatar(final CommandContext<FiguraClientCommandSource> context) {
        final String str = StringArgumentType.getString((CommandContext)context, "path");
        CompletableFuture.runAsync(() -> {
            try {
                final Path moon_path = FiguraMod.getFiguraDirectory().resolve(str);
                final NbtCompound avatar = NbtIo.readCompressed(Files.newInputStream(moon_path, new OpenOption[0]));
                final String avatar_name = FiguraModelParser.parseAvatar(avatar);
                FiguraMod.LOGGER.error("Parsed " + avatar_name + " successfully.");
            }
            catch (final Exception e) {
                FiguraMod.LOGGER.error("Error while parsing local avatar: " + e);
                FiguraToast.sendToast(Text.literal(e.toString()));
            }
            return;
        });
        return 1;
    }
}
