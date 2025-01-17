package org.figuramc.figura.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;

class ReloadCommand {

    public static LiteralArgumentBuilder<FiguraClientCommandSource> getCommand() {
        LiteralArgumentBuilder<FiguraClientCommandSource> cmd = LiteralArgumentBuilder.literal("reload");
        cmd.executes(context -> {
            AvatarManager.reloadAvatar(FiguraMod.getLocalPlayerUUID());
            FiguraToast.sendToast(FiguraText.of("toast.reload"));
            return 1;
        });
        return cmd;
    }
}
