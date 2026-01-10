package org.figuramc.figura.mixin.neoforge;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration;
import org.figuramc.figura.gui.FiguraPortraitRenderState;
import org.figuramc.figura.gui.FiguraPortraitRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @ModifyArg(method = "<init>", index = 4,
            at = @At(value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/ClientHooks;gatherPictureInPictureRenderers(Ljava/util/List;)Ljava/util/List;"))
    private List<PictureInPictureRendererRegistration<?>> addPortraitRenderer(List<PictureInPictureRendererRegistration<?>> list, @Local MultiBufferSource.BufferSource source) {
        //Don't forget Neoforge is put those list to gatherPictureInPictureRenderers
        List<PictureInPictureRendererRegistration<?>> newList = new ArrayList<>(list);
        newList.add(new PictureInPictureRendererRegistration<>(FiguraPortraitRenderState.class, FiguraPortraitRenderer::new));
        return newList;
    }
}
