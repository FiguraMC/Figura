package org.figuramc.figura.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.nbt.NbtIo;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.docs.FiguraDocsManager;
import org.figuramc.figura.model.rendering.AvatarRenderer;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;

class ExportCommand {

    public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
        LiteralArgumentBuilder<FiguraClientCommandSource> root = LiteralArgumentBuilder.literal("export");

        // texture
        root.then(exportTexture());

        // docs
        root.then(FiguraDocsManager.getExportCommand());

        // avatar
        root.then(exportAvatar());

        // return
        return root;
    }

    private static LiteralArgumentBuilder<FiguraClientCommandSource> exportTexture() {
        LiteralArgumentBuilder<FiguraClientCommandSource> run = LiteralArgumentBuilder.literal("texture");

        RequiredArgumentBuilder<FiguraClientCommandSource, String> arg = RequiredArgumentBuilder.argument("texture name", StringArgumentType.word());
        arg.executes(context -> runTextureExport(context, "exported_texture"));

        RequiredArgumentBuilder<FiguraClientCommandSource, String> name = RequiredArgumentBuilder.argument("name", StringArgumentType.greedyString());
        name.executes(context -> runTextureExport(context, StringArgumentType.getString(context, "name")));
        arg.then(name);

        run.then(arg);
        return run;
    }

    private static int runTextureExport(CommandContext<FiguraClientCommandSource> context, String filename) {
        String textureName = StringArgumentType.getString(context, "texture name");
        AvatarRenderer renderer = FiguraCommands.getRenderer(context);
        if (renderer == null)
            return 0;

        try {
            FiguraTexture texture = renderer.getTexture(textureName);
            if (texture == null)
                throw new Exception();

            texture.writeTexture(FiguraMod.getFiguraDirectory().resolve(filename + ".png"));

            context.getSource().figura$sendFeedback(FiguraText.of("command.export_texture.success"));
            return 1;
        } catch (Exception e) {
            context.getSource().figura$sendError(FiguraText.of("command.export_texture.error"));
            return 0;
        }
    }

    private static LiteralArgumentBuilder<FiguraClientCommandSource> exportAvatar() {
        LiteralArgumentBuilder<FiguraClientCommandSource> run = LiteralArgumentBuilder.literal("avatar");
        run.executes(context -> runAvatarExport(context, "exported_avatar"));

        RequiredArgumentBuilder<FiguraClientCommandSource, String> name = RequiredArgumentBuilder.argument("name", StringArgumentType.greedyString());
        name.executes(context -> runAvatarExport(context, StringArgumentType.getString(context, "name")));
        run.then(name);

        return run;
    }

    private static int runAvatarExport(CommandContext<FiguraClientCommandSource> context, String filename) {
        Avatar avatar = FiguraCommands.checkAvatar(context);
        if (avatar == null)
            return 0;

        try {
            if (avatar.nbt == null)
                throw new Exception();

            NbtIo.writeCompressed(avatar.nbt, FiguraMod.getFiguraDirectory().resolve(filename + ".moon"));

            context.getSource().figura$sendFeedback(FiguraText.of("command.export_avatar.success"));
            return 1;
        } catch (Exception e) {
            context.getSource().figura$sendError(FiguraText.of("command.export_avatar.error"));
            return 0;
        }
    }
}
