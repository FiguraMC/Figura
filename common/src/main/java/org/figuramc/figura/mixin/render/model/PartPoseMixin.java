package org.figuramc.figura.mixin.render.model;

import net.minecraft.client.model.geom.PartPose;
import org.figuramc.figura.ducks.PartPoseExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PartPose.class)
public class PartPoseMixin implements PartPoseExtension {
    @Unique
    Boolean figura$isVisible = null;

    @Override
    public Boolean figura$isVisible() {
        return figura$isVisible;
    }

    @Override
    public void figura$setVisible(Boolean visible) {
        this.figura$isVisible = visible;
    }

    @Inject(method = "equals", at = @At("TAIL"), cancellable = true)
    public void figura$equals(Object o, CallbackInfoReturnable<Boolean> cir) {
        if (o instanceof PartPoseExtension other && cir.getReturnValue()) {
            cir.setReturnValue(other.figura$isVisible() == null || this.figura$isVisible == null || other.figura$isVisible().equals(this.figura$isVisible));
        }
    }
}
