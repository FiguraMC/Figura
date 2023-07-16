package org.figuramc.figura.mixin.render.layers.elytra;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ElytraModel.class)
public interface ElytraModelAccessor {
    @Intrinsic
    @Accessor
    ModelPart getLeftWing();

    @Intrinsic
    @Accessor
    ModelPart getRightWing();
}
