package org.moon.figura.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.nbt.NbtIo;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.docs.FiguraDocsManager;
import org.moon.figura.model.rendering.AvatarRenderer;
import org.moon.figura.model.rendering.texture.FiguraTexture;
import org.moon.figura.utils.FiguraText;

class ExportCommand {

    public static LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal("export");

        //texture
        root.then(exportTexture());

        //docs
        root.then(FiguraDocsManager.getExportCommand());

        //avatar
        root.then(exportAvatar());

        //return
        return root;
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> exportTexture() {
        LiteralArgumentBuilder<FabricClientCommandSource> run = LiteralArgumentBuilder.literal("texture");

        RequiredArgumentBuilder<FabricClientCommandSource, String> arg = RequiredArgumentBuilder.argument("texture name", StringArgumentType.word());
        arg.executes(context -> runTextureExport(context, "exported_texture"));

        RequiredArgumentBuilder<FabricClientCommandSource, String> name = RequiredArgumentBuilder.argument("name", StringArgumentType.greedyString());
        name.executes(context -> runTextureExport(context, StringArgumentType.getString(context, "name")));
        arg.then(name);

        run.then(arg);
        return run;
    }

    private static int runTextureExport(CommandContext<FabricClientCommandSource> context, String filename) {
        String textureName = StringArgumentType.getString(context, "texture name");
        AvatarRenderer renderer = FiguraCommands.getRenderer(context);
        if (renderer == null)
            return 0;

        try {
            FiguraTexture texture = renderer.getTexture(textureName);
            if (texture == null)
                throw new Exception();

            texture.writeTexture(FiguraMod.getFiguraDirectory().resolve(filename + ".png"));

            context.getSource().sendFeedback(FiguraText.of("command.export_texture.success"));
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(FiguraText.of("command.export_texture.error"));
            return 0;
        }
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> exportAvatar() {
        LiteralArgumentBuilder<FabricClientCommandSource> run = LiteralArgumentBuilder.literal("avatar");
        run.executes(context -> runAvatarExport(context, "exported_avatar"));

        RequiredArgumentBuilder<FabricClientCommandSource, String> name = RequiredArgumentBuilder.argument("name", StringArgumentType.greedyString());
        name.executes(context -> runAvatarExport(context, StringArgumentType.getString(context, "name")));
        run.then(name);

        return run;
    }

    private static int runAvatarExport(CommandContext<FabricClientCommandSource> context, String filename) {
        Avatar avatar = FiguraCommands.checkAvatar(context);
        if (avatar == null)
            return 0;

        try {
            if (avatar.nbt == null)
                throw new Exception();

            NbtIo.writeCompressed(avatar.nbt, FiguraMod.getFiguraDirectory().resolve(filename + ".moon").toFile());

            context.getSource().sendFeedback(FiguraText.of("command.export_avatar.success"));
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(FiguraText.of("command.export_avatar.error"));
            return 0;
        }
    }
}
