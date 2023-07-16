package org.moon.figura.commands.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.moon.figura.commands.FiguraCommands;
import org.moon.figura.utils.FiguraClientCommandSource;

public class FiguraCommandsFabric {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init() {
        //register
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            CommandDispatcher<FiguraClientCommandSource> casted = (CommandDispatcher) dispatcher;
            casted.register(FiguraCommands.getCommandRoot());
        });
    }
}
