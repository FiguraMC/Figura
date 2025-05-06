package org.figuramc.figura.mixin.compat;

import java.util.function.Function;

import org.figuramc.figura.compat.FiguraSVCIconCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import de.maxhenkel.voicechat.voice.client.GroupChatManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Pseudo
@Mixin(value = GroupChatManager.class, remap = true)
public class SVCGroupChatManagerMixin {
    @WrapWithCondition(require = 0, method = "renderIcons(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/gui/GameProfileUtils;getSkin(Ljava/util/UUID;)Lnet/minecraft/client/resources/PlayerSkin;"), to = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/voice/common/PlayerState;isDisabled()Z")))
    private static boolean figuraSvcPortrait(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, @Local PlayerState state) {
        return FiguraSVCIconCompat.shouldRenderVanillaIcon(guiGraphics, state, 1, 1, 16, 16);
    }

    // >1.21.1
    @WrapWithCondition(require = 0, method = "renderIcons(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/gui/GameProfileUtils;getSkin(Ljava/util/UUID;)Lnet/minecraft/client/resources/PlayerSkin;"), to = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/voice/common/PlayerState;isDisabled()Z")))
    private static boolean figuraSvcPortrait2(GuiGraphics guiGraphics, Function<ResourceLocation, RenderType> function, ResourceLocation id, int i, int j, float f, float g, int k, int l, int m, int n, @Local PlayerState state) {
        return FiguraSVCIconCompat.shouldRenderVanillaIcon(guiGraphics, state, 1, 1, 16, 16);
    }
}