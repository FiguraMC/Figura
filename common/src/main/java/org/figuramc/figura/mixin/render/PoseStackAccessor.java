package org.figuramc.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Deque;

@Mixin(PoseStack.class)
public interface PoseStackAccessor {
    @Accessor("poseStack")
    @Final
    Deque<PoseStack.Pose> getPoseStack();
}
