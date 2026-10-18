package org.figuramc.figura.commands.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.commands.FiguraCommands;
import org.figuramc.figura.utils.FiguraClientCommandSource;

@EventBusSubscriber(modid = FiguraMod.MOD_ID, value = Dist.CLIENT)
public class FiguraCommandsNeoForge {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @SubscribeEvent
    public static void registerCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandDispatcher<FiguraClientCommandSource> casted = (CommandDispatcher) dispatcher;
        casted.register(FiguraCommands.getCommandRoot());
    }
}
