package org.figuramc.figura.mixin.compat;

import java.util.function.Function;

import org.figuramc.figura.compat.FiguraSVCIconCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;

import de.maxhenkel.voicechat.gui.group.JoinGroupEntry;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Pseudo
@Mixin(value = JoinGroupEntry.class, remap = true)
public class SVCJoinGroupEntryMixin {

    @WrapWithCondition(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIIIIIZF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/gui/GameProfileUtils;getSkin(Ljava/util/UUID;)Lnet/minecraft/resources/ResourceLocation;"), to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V")))
    private boolean figuraSvcPortrait(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, @Local PlayerState state) {
        return FiguraSVCIconCompat.shouldRenderVanillaIcon(guiGraphics, state, 0, 0, 12, 16);
    }
}
