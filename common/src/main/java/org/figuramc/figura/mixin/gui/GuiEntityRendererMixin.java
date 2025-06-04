package org.figuramc.figura.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.GuiEntityRenderStateExtension;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin extends PictureInPictureRenderer<GuiEntityRenderState> {

    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    protected GuiEntityRendererMixin(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @WrapMethod(method = "renderToTexture(Lnet/minecraft/client/gui/render/state/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    private void setFiguraRenderProperties(GuiEntityRenderState guiEntityRenderState, PoseStack matrices, Operation<Void> original) {
        if (!Configs.FIGURA_INVENTORY.value || AvatarManager.panic) {
            original.call(guiEntityRenderState, matrices);
            return;
        }

        GuiEntityRenderStateExtension extended = (GuiEntityRenderStateExtension) (Object) guiEntityRenderState;
        switch (extended.getRenderMode()) {
            case FIGURA_GUI -> {
                UIHelper.useFiguraLighting();
            }
            default -> {
                Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
            }
        }
        Vector3f vector3f = guiEntityRenderState.translation();
        matrices.translate(vector3f.x, vector3f.y, vector3f.z);
        matrices.mulPose(guiEntityRenderState.rotation());
        Quaternionf quaternionf = guiEntityRenderState.overrideCameraAngle();
        if (quaternionf != null) {
            this.entityRenderDispatcher.overrideCameraOrientation(quaternionf.conjugate(new Quaternionf()).rotateY(3.1415927F));
        }

        this.entityRenderDispatcher.setRenderShadow(false);
        this.entityRenderDispatcher.render(guiEntityRenderState.renderState(), extended.getXPos(), extended.getYPos(), 0.0, matrices, this.bufferSource, 15728880);
        this.entityRenderDispatcher.setRenderShadow(true);
    }
}
