package org.figuramc.figura.mixin.font;

import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Font.StringRenderOutput.class)
public interface StringRenderOutputAccessor {
    @Invoker("<init>")
    static Font.StringRenderOutput invokeNew(MultiBufferSource multiBufferSource, float f, float g, int i, boolean bl, Matrix4f matrix4f, boolean bl2, int j) {
        throw new AssertionError();
    }
}
