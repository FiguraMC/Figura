package org.moon.figura.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.util.UUIDTypeAdapter;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;

import java.util.UUID;

public class FiguraRunCommand {

    private static final UUID clientUUID = UUIDTypeAdapter.fromString(Minecraft.getInstance().getUser().getUuid());

    public static LiteralArgumentBuilder<FabricClientCommandSource> generateRunCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> run = LiteralArgumentBuilder.literal("run");
        RequiredArgumentBuilder<FabricClientCommandSource, String> arg =
                RequiredArgumentBuilder.argument("code", StringArgumentType.greedyString());
        arg.executes(FiguraRunCommand::executeCode);
        run.then(arg);
        return run;
    }

    private static int executeCode(CommandContext<FabricClientCommandSource> context) {
        String lua = StringArgumentType.getString(context, "code");
        Avatar localAvatar = AvatarManager.getAvatarForPlayer(clientUUID);
        if (localAvatar == null) {
            FiguraMod.sendChatMessage(new TextComponent("No local avatar equipped!"));
            return 0;
        }
        if (localAvatar.luaState == null || localAvatar.scriptError) {
            FiguraMod.sendChatMessage(new TextComponent("No active script to run code in!"));
            return 0;
        }
        localAvatar.luaState.runScript(lua, "runCommand");
        return 1;
    }

}
