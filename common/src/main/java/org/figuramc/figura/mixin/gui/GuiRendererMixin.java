package org.figuramc.figura.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.figuramc.figura.ducks.GuiEntityRenderStateExtension;
import org.figuramc.figura.ducks.GuiMessageAccessor;
import org.figuramc.figura.gui.FiguraGuiEntityRenderer;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.ui.UIHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @Shadow @Final
    GuiRenderState renderState;

    @Unique
    FiguraGuiEntityRenderer figura$paperDollRenderer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializePaperDollRenderer(CallbackInfo ci) {
        figura$paperDollRenderer = new FiguraGuiEntityRenderer(Minecraft.getInstance().getEntityRenderDispatcher());
    }

    @Group(name = "figura$paperDoll", min = 1)
    @Inject(method = "preparePictureInPictureState(Lnet/minecraft/client/renderer/state/gui/pip/PictureInPictureRenderState;I)V", at = @At("HEAD"), cancellable = true, require = 0)
    private <T extends PictureInPictureRenderState> void renderPaperDoll(T pictureInPictureRenderState, int i, CallbackInfo ci) {
        if (figura$preparePaperDoll(pictureInPictureRenderState, i))
            ci.cancel();
    }

    @Group(name = "figura$paperDoll", min = 1)
    @Inject(method = "preparePictureInPictureState(Lnet/minecraft/client/renderer/state/gui/pip/PictureInPictureRenderState;IZ)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private <T extends PictureInPictureRenderState> void renderPaperDollNeoForge(T state, int scale, boolean firstPass, CallbackInfoReturnable<Boolean> cir) {
        if (figura$preparePaperDoll(state, scale))
            cir.setReturnValue(true);
    }

    @Unique
    private boolean figura$preparePaperDoll(PictureInPictureRenderState pictureInPictureRenderState, int i) {
        if (pictureInPictureRenderState instanceof GuiEntityRenderStateExtension extension && (extension.getRenderMode() != null && extension.getRenderMode() == EntityRenderMode.PAPERDOLL)) {
            figura$paperDollRenderer.prepare((GuiEntityRenderState) pictureInPictureRenderState, this.renderState, Minecraft.getInstance().gameRenderer.featureRenderDispatcher(), i);
            return true;
        }
        return false;
    }


    @Inject(method = "close", at = @At(value = "INVOKE", target = "Ljava/util/Collection;forEach(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void onClose(CallbackInfo ci) {
        figura$paperDollRenderer.close();
    }
}
