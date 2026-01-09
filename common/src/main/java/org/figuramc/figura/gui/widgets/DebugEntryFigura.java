package org.figuramc.figura.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.config.Configs;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DebugEntryFigura implements DebugScreenEntry {
    @Override
    public void display(DebugScreenDisplayer debugScreenDisplayer, @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
        if (AvatarManager.panic || !Configs.RENDER_STATS.value) return;
        List<String> lines = new ArrayList<>();
        int i = 0;

        lines.add(ChatFormatting.AQUA + "[" + FiguraMod.MOD_NAME + "]" + ChatFormatting.RESET);
        lines.add("Version: " + FiguraMod.VERSION);

        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && avatar.nbt != null) {
            lines.add(String.format("Model Complexity: %d", avatar.complexity.pre));
            lines.add(String.format("Animations Complexity: %d", avatar.animationComplexity));

            // has script
            if (avatar.luaRuntime != null || avatar.scriptError) {
                String color = (avatar.scriptError ? ChatFormatting.RED : "").toString();
                lines.add(color + String.format("Animations instructions: %d", avatar.animation.pre));
                lines.add(color + String.format("Init instructions: %d (W: %d E: %d)", avatar.init.getTotal(), avatar.init.pre, avatar.init.post) + ChatFormatting.RESET);
                lines.add(color + String.format("Tick instructions: %d (W: %d E: %d)", avatar.tick.getTotal() + avatar.worldTick.getTotal(), avatar.worldTick.pre, avatar.tick.pre)  + ChatFormatting.RESET);
                lines.add(color + String.format("Render instructions: %d (E: %d PE: %d)", avatar.render.getTotal(), avatar.render.pre, avatar.render.post) + ChatFormatting.RESET);
                lines.add(color + String.format("World Render instructions: %d (W: %d PW: %d)", avatar.worldRender.getTotal(), avatar.worldRender.pre, avatar.worldRender.post) + ChatFormatting.RESET);
            }
        }
        lines.add(String.format("Pings per second: ↑%d, ↓%d", NetworkStuff.pingsSent, NetworkStuff.pingsReceived));

        lines.add("");

        for (String line : lines) {
            debugScreenDisplayer.addLine(line);
        }
    }

    @Override
    public boolean isAllowed(boolean bl) {
        return true;
    }
}
