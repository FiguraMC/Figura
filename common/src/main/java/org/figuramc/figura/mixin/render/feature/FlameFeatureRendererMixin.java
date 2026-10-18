package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FlameFeatureRenderer.class)
public class FlameFeatureRendererMixin {
    @WrapOperation(method = "buildGroup", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/renderer/feature/FlameFeatureRenderer;prepare(Lnet/minecraft/client/renderer/feature/FlameFeatureRenderer$Submit;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private void figura$fireTextures(FlameFeatureRenderer renderer, FlameFeatureRenderer.Submit submit,
                                    VertexConsumer consumer, TextureAtlasSprite first, TextureAtlasSprite second,
                                    Operation<Void> original) {
        var avatar = AvatarManager.getAvatar(submit.entityRenderState());
        TextureAtlasSprite customFirst = RenderUtils.firstFireLayer(avatar);
        TextureAtlasSprite customSecond = RenderUtils.secondFireLayer(avatar);
        original.call(renderer, submit, consumer, customFirst == null ? first : customFirst,
                customSecond == null ? second : customSecond);
    }
}