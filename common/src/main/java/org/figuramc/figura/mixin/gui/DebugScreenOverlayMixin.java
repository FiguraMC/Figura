package org.figuramc.figura.mixin.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.backend2.NetworkStuff;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {

    @Inject(at = @At("RETURN"), method = "getSystemInformation")
    protected void getSystemInformation(CallbackInfoReturnable<List<String>> cir) {
        if (AvatarManager.panic) return;

        List<String> lines = cir.getReturnValue();

        int i = 0;
        for (; i < lines.size(); i++) {
            if (lines.get(i).equals(""))
                break;
        }

        lines.add(++i, ChatFormatting.AQUA + "[" + FiguraMod.MOD_NAME + "]" + ChatFormatting.RESET);
        lines.add(++i, "Version: " + FiguraMod.VERSION);

        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && avatar.nbt != null) {
            lines.add(++i, String.format("Model Complexity: %d", avatar.complexity.pre));
            lines.add(++i, String.format("Animations Complexity: %d", avatar.animationComplexity));

            // has script
            if (avatar.luaRuntime != null || avatar.scriptError) {
                String color = (avatar.scriptError ? ChatFormatting.RED : "").toString();
                lines.add(++i, color + String.format("Animations instructions: %d", avatar.animation.pre));
                lines.add(++i, color + String.format("Init instructions: %d (W: %d E: %d)", avatar.init.getTotal(), avatar.init.pre, avatar.init.post) + ChatFormatting.RESET);
                lines.add(++i, color + String.format("Tick instructions: %d (W: %d E: %d)", avatar.tick.getTotal() + avatar.worldTick.getTotal(), avatar.worldTick.pre, avatar.tick.pre)  + ChatFormatting.RESET);
                lines.add(++i, color + String.format("Render instructions: %d (E: %d PE: %d)", avatar.render.getTotal(), avatar.render.pre, avatar.render.post) + ChatFormatting.RESET);
                lines.add(++i, color + String.format("World Render instructions: %d (W: %d PW: %d)", avatar.worldRender.getTotal(), avatar.worldRender.pre, avatar.worldRender.post) + ChatFormatting.RESET);
            }
        }
        lines.add(++i, String.format("Pings per second: ↑%d, ↓%d", NetworkStuff.pingsSent, NetworkStuff.pingsReceived));

        lines.add(++i, "");
    }
}
