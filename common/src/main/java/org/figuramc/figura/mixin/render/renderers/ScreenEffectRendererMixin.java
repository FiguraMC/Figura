package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    @Unique
    private static Avatar avatar;

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void renderFire(Minecraft client, PoseStack matrices, CallbackInfo ci) {
        Avatar a = AvatarManager.getAvatar(client.getCameraEntity());
        if (RenderUtils.vanillaModelAndScript(a)) {
            if (!a.luaRuntime.renderer.renderFire) {
                ci.cancel();
            } else {
                avatar = a;
            }
        }
    }

    @ModifyVariable(method = "renderFire", at = @At("STORE"), ordinal = 0)
    private static TextureAtlasSprite secondFireTexture(TextureAtlasSprite sprite) {
        TextureAtlasSprite s = RenderUtils.secondFireLayer(avatar);
        avatar = null;
        return s != null ? s : sprite;
    }
}
