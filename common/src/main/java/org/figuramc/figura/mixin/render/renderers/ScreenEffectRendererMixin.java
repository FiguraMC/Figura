package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    @Inject(method = "submitFire", at = @At("HEAD"), cancellable = true)
    private static void renderFire(PoseStack poseStack, SubmitNodeCollector multiBufferSource, TextureAtlasSprite textureAtlasSprite, CallbackInfo ci) {
        Avatar a = AvatarManager.getAvatar(Minecraft.getInstance().getCameraEntity());
        if (RenderUtils.vanillaModelAndScript(a)) {
            if (!a.luaRuntime.renderer.renderFire) {
                ci.cancel();
            }
        }
    }

    @ModifyVariable(method = "submitFire", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static TextureAtlasSprite secondFireTexture(TextureAtlasSprite sprite) {
        TextureAtlasSprite s = RenderUtils.secondFireLayer(AvatarManager.getAvatar(Minecraft.getInstance().getCameraEntity()));
        return s != null ? s : sprite;
    }
}
