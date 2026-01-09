package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.AtlasManager;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.utils.RenderUtils;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlameFeatureRenderer.class)
public class FlameFeatureRendererMixin {

    @Unique
    Avatar figura$avatar = null;
    @ModifyVariable(method = "renderFlame", at = @At("STORE"), ordinal = 0)
    private TextureAtlasSprite firstFireTexture(TextureAtlasSprite sprite) {
        TextureAtlasSprite s = RenderUtils.firstFireLayer(figura$avatar);
        return s != null ? s : sprite;
    }

    @ModifyVariable(method = "renderFlame", at = @At("STORE"), ordinal = 1)
    private TextureAtlasSprite secondFireTexture(TextureAtlasSprite sprite) {
        TextureAtlasSprite s = RenderUtils.secondFireLayer(figura$avatar);
        figura$avatar = null;
        return s != null ? s : sprite;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FlameFeatureRenderer;renderFlame(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lorg/joml/Quaternionf;Lnet/minecraft/client/resources/model/AtlasManager;)V"))
    private void figura$preRender(
            SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, AtlasManager atlasManager, CallbackInfo ci, @Local SubmitNodeStorage.FlameSubmit flameSubmit ) {

    }
}
