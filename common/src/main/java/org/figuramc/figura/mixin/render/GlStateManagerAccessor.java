package org.figuramc.figura.mixin.render;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.FloatBuffer;

@Mixin(GlStateManager.class)
public interface GlStateManagerAccessor {
    @Intrinsic
    @Invoker("getBuffer")
    static FloatBuffer invokeGetFloatBuffer(float f, float g, float h, float i) {
        throw new AssertionError();
    };
}
