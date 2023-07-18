package org.figuramc.figura.commands.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import org.figuramc.figura.commands.FiguraCommands;
import org.figuramc.figura.utils.FiguraClientCommandSource;

public class FiguraCommandsFabric {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init() {
        //register
        CommandDispatcher<FiguraClientCommandSource> casted = (CommandDispatcher)ClientCommandManager.DISPATCHER;
        casted.register(FiguraCommands.getCommandRoot());

    }
}
