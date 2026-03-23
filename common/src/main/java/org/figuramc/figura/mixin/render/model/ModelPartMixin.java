
package org.figuramc.figura.mixin.render.model;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.MultiBufferSource;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.PartPoseExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

// i hate this sooo much
@Mixin(ModelPart.class)
public class ModelPartMixin implements FiguraSubmitCallBackExtension {
    @Shadow
    public boolean visible;
    @Unique
    private final List<BiFunction<MultiBufferSource, PoseStack, Boolean>> figura$preRenderingCallback = new ArrayList<>();
    @Unique
    private final List<Runnable> figura$postRenderingCallback = new ArrayList<>();

    @Override
    public void figura$addPreRenderingCallback(BiFunction<MultiBufferSource, PoseStack, Boolean> callback) {
        this.figura$preRenderingCallback.add(callback);
    }

    @Override
    public void figura$addPostRenderingCallback(Runnable callback) {
        this.figura$postRenderingCallback.add(callback);
    }

    @Override
    public List<Runnable> figura$getPostRenderingCallbacks() {
        return figura$postRenderingCallback;
    }

    @Override
    public List<BiFunction<MultiBufferSource, PoseStack, Boolean>> figura$getPreRenderingCallbacks() {
        return figura$preRenderingCallback;
    }

    // yipee store visibility in pose
    @ModifyReturnValue(method = "storePose", at = @At("TAIL"))
    public PartPose figura$storeVisibility(PartPose original) {
        ((PartPoseExtension)(Object)original).figura$setVisible(this.visible);
        return original;
    }

    @Inject(method = "loadPose", at = @At("HEAD"))
    public void figura$loadVisibility(PartPose pose, CallbackInfo ci) {
        if (((PartPoseExtension)(Object)pose).figura$isVisible() != null)
            this.visible = ((PartPoseExtension)(Object)pose).figura$isVisible();
    }
}
