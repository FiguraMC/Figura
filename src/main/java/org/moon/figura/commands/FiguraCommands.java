package org.moon.figura.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import org.moon.figura.FiguraMod;
import org.moon.figura.backend.NetworkManager;
import org.moon.figura.lua.docs.FiguraDocsManager;

public class FiguraCommands {

    public static void init() {
        //root
        LiteralArgumentBuilder<FabricClientCommandSource> root = LiteralArgumentBuilder.literal(FiguraMod.MOD_ID);

        //docs
        root.then(FiguraDocsManager.getCommand());
        root.then(FiguraDocsManager.getExportCommand());

        //links
        root.then(FiguraLinkCommand.getCommand());

        //run
        root.then(FiguraRunCommand.getCommand());

        //load
        root.then(FiguraLoadCommand.getCommand());

        //load
        root.then(FiguraReloadCommand.getCommand());

        //debug
        root.then(FiguraDebugCommand.getCommand());

        //backend debug
        if (FiguraMod.DEBUG_MODE)
            root.then(NetworkManager.getCommand());

        //register
        ClientCommandManager.DISPATCHER.register(root);
    }
}
