package org.moon.figura.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import org.moon.figura.FiguraMod;
import org.moon.figura.lua.docs.FiguraDocsManager;

public class FiguraCommands {

    public static void init() {
        //root
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal(FiguraMod.MOD_ID);

        //docs
        root.then(FiguraDocsManager.get());

        //links
        root.then(FiguraLinks.get());

        //run
        root.then(FiguraRunCommand.get());

        //load
        root.then(FiguraLoadCommand.get());

        //register
        ClientCommandManager.DISPATCHER.register(root);
    }
}
