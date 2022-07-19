package org.moon.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.moon.figura.ducks.PoseStackAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Deque;

@Mixin(PoseStack.class)
public class PoseStackMixin implements PoseStackAccessor {

    @Shadow @Final private Deque<PoseStack.Pose> poseStack;

    @Override @Intrinsic
    public void pushPose(PoseStack.Pose pose) {
        this.poseStack.addLast(pose);
    }
}
