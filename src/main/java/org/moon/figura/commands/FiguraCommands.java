package org.moon.figura.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.lua.docs.FiguraDocsManager;
import org.moon.figura.utils.ColorUtils.Colors;
import org.moon.figura.utils.FiguraText;

public class FiguraCommands {

    public static void init() {
        //root
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal(FiguraMod.MOD_ID);

        //docs
        root.then(FiguraDocsManager.generateCommand());

        //links
        root.then(FiguraLinks.generateLinks());

        //run
        root.then(FiguraRunCommand.generateRunCommand());

        //register
        ClientCommandManager.DISPATCHER.register(root);
    }


}
