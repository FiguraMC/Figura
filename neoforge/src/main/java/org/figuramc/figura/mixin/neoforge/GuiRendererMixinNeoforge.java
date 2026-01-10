package org.figuramc.figura.mixin.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.figuramc.figura.ducks.GuiEntityRenderStateExtension;
import org.figuramc.figura.gui.FiguraGuiEntityRenderer;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(GuiRenderer.class)
public class GuiRendererMixinNeoforge {

    @Shadow
    @Final
    GuiRenderState renderState;

    @Unique
    FiguraGuiEntityRenderer figura$paperDollRenderer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializePaperDollRenderer(GuiRenderState guiRenderState, MultiBufferSource.BufferSource bufferSource, SubmitNodeCollector submitNodeCollector, FeatureRenderDispatcher featureRenderDispatcher, List<PictureInPictureRenderer<?>> list, CallbackInfo ci) {
        figura$paperDollRenderer = new FiguraGuiEntityRenderer(bufferSource, Minecraft.getInstance().getEntityRenderDispatcher());
    }

    @Inject(method = "preparePictureInPictureState", at = @At("HEAD"), cancellable = true)
    private <T extends PictureInPictureRenderState> void renderPaperDoll(PictureInPictureRenderState pictureInPictureRenderState, int i, boolean firstPass, CallbackInfoReturnable<Boolean> cir) {
        if (pictureInPictureRenderState instanceof GuiEntityRenderStateExtension extension && (extension.getRenderMode() != null && extension.getRenderMode() == EntityRenderMode.PAPERDOLL)) {
            if (pictureInPictureRenderState instanceof GuiEntityRenderState guiEntityRenderState) {
                UIHelper.paperdoll = true;
                figura$paperDollRenderer.prepare(guiEntityRenderState, this.renderState, i);
                UIHelper.paperdoll = false;
                cir.setReturnValue(true);
            }
        }
    }


    @Inject(method = "close", at = @At(value = "INVOKE", target = "Ljava/util/Collection;forEach(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void onClose(CallbackInfo ci) {
        figura$paperDollRenderer.close();
    }
}
