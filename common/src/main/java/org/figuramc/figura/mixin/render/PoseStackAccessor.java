package org.figuramc.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Deque;
import java.util.List;

@Mixin(PoseStack.class)
public interface PoseStackAccessor {
    @Accessor("poses")
    @Final
    List<PoseStack.Pose> getPoseStack();

    @Accessor("lastIndex")
    int getLastIndex();
}
