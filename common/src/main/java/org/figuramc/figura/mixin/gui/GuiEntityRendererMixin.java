package org.figuramc.figura.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.GuiEntityRenderStateExtension;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static org.figuramc.figura.model.rendering.EntityRenderMode.FIGURA_GUI;

@Mixin(value = GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin extends PictureInPictureRenderer<GuiEntityRenderState> {

    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    protected GuiEntityRendererMixin() {
        super();
    }

    @WrapOperation(method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
    private <S extends EntityRenderState> void setFiguraRenderProperties(EntityRenderDispatcher instance, S entityRenderState, CameraRenderState cameraRenderState, double d, double e, double f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Operation<Void> original, @Local(argsOnly = true) GuiEntityRenderState guiEntityRenderState) {
        GuiEntityRenderStateExtension extended = (GuiEntityRenderStateExtension) (Object) guiEntityRenderState;
        if (AvatarManager.getAvatar(entityRenderState) != null && extended.getRenderMode() != null) {
            AvatarManager.getAvatar(entityRenderState).renderMode = extended.getRenderMode();
        }
        original.call(instance, entityRenderState, cameraRenderState, d+extended.getXPos(), e+extended.getYPos(), f, poseStack, submitNodeCollector);
    }

    @WrapOperation(method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Lighting;setupFor(Lcom/mojang/blaze3d/platform/Lighting$Entry;)V"))
    private <S extends EntityRenderState> void setFiguraRenderProperties(Lighting instance, Lighting.Entry entry, Operation<Void> original, @Local(argsOnly = true) GuiEntityRenderState guiEntityRenderState) {
        EntityRenderMode mode = ((GuiEntityRenderStateExtension) (Object) guiEntityRenderState).getRenderMode();
        if (mode == FIGURA_GUI) {
            UIHelper.useFiguraLighting();
        } else {
            original.call(instance, entry);
        }
    }

    @Override
    public void prepare(GuiEntityRenderState state, GuiRenderState guiState, FeatureRenderDispatcher dispatcher, int scale) {
        boolean previousPaperdoll = UIHelper.paperdoll;
        UIHelper.paperdoll = true;
        try {
            super.prepare(state, guiState, dispatcher, scale);
        } finally {
            UIHelper.paperdoll = previousPaperdoll;
        }
    }
}