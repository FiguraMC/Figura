package org.figuramc.figura.mixin.compat;

import java.util.function.Function;

import org.figuramc.figura.compat.FiguraSVCIconCompat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.GroupChatManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Pseudo
@Mixin(value = GroupChatManager.class, remap = true)
public class SVCGroupChatManagerMixin {
    @Shadow
    @Final
    private static ResourceLocation TALK_OUTLINE;

    @WrapWithCondition(require = 0, method = "renderIcons(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/gui/GameProfileUtils;getSkin(Ljava/util/UUID;)Lnet/minecraft/client/resources/PlayerSkin;"), to = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/voice/common/PlayerState;isDisabled()Z")))
    private static boolean figuraSvcPortrait(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, @Local PlayerState state) {
        boolean shouldRender = FiguraSVCIconCompat.shouldRenderVanillaIcon(guiGraphics, state, 1, 1, 16, 16);

        // This part only seems to be needed in 1.20.1, 1.21.1 renders everything fine without it.
        ClientVoicechat client = ClientManager.getClient();
        if (client.getTalkCache().isTalking(state.getUuid())) {
            int posX = (Integer) VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosX.get();
            int posY = (Integer) VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosY.get();
            guiGraphics.blit(TALK_OUTLINE, posX < 0 ? -10 : 0, posY < 0 ? -10 : 0, 0.0F, 0.0F, 10, 10, 16, 16);
        }

        return shouldRender;
    }

    // >1.21.1
    @WrapWithCondition(require = 0, method = "renderIcons(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/gui/GameProfileUtils;getSkin(Ljava/util/UUID;)Lnet/minecraft/client/resources/PlayerSkin;"), to = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/voice/common/PlayerState;isDisabled()Z")))
    private static boolean figuraSvcPortrait2(GuiGraphics guiGraphics, Function<ResourceLocation, RenderType> function, ResourceLocation id, int i, int j, float f, float g, int k, int l, int m, int n, @Local PlayerState state) {
        return FiguraSVCIconCompat.shouldRenderVanillaIcon(guiGraphics, state, 1, 1, 16, 16);
    }
}