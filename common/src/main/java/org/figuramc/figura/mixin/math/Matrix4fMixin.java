package org.figuramc.figura.mixin.math;

import com.mojang.math.Matrix4f;
import org.figuramc.figura.ducks.extensions.Matrix4fExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.FloatBuffer;

@Mixin(Matrix4f.class)
public abstract class Matrix4fMixin implements Matrix4fExtension {

    @Shadow protected float m00;

    @Shadow protected float m01;

    @Shadow protected float m02;

    @Shadow protected float m03;

    @Shadow protected float m10;

    @Shadow protected float m11;

    @Shadow protected float m12;

    @Shadow protected float m13;

    @Shadow protected float m20;

    @Shadow protected float m21;

    @Shadow protected float m22;

    @Shadow protected float m23;

    @Shadow protected float m30;

    @Shadow protected float m31;

    @Shadow protected float m32;

    @Shadow protected float m33;

    @Shadow
    private static int bufferIndex(int i, int j) {
        throw new AssertionError();
    }

    @Unique
    public void figura$load(FloatBuffer buf) {
        this.m00 = buf.get(bufferIndex(0, 0));
        this.m01 = buf.get(bufferIndex(0, 1));
        this.m02 = buf.get(bufferIndex(0, 2));
        this.m03 = buf.get(bufferIndex(0, 3));
        this.m10 = buf.get(bufferIndex(1, 0));
        this.m11 = buf.get(bufferIndex(1, 1));
        this.m12 = buf.get(bufferIndex(1, 2));
        this.m13 = buf.get(bufferIndex(1, 3));
        this.m20 = buf.get(bufferIndex(2, 0));
        this.m21 = buf.get(bufferIndex(2, 1));
        this.m22 = buf.get(bufferIndex(2, 2));
        this.m23 = buf.get(bufferIndex(2, 3));
        this.m30 = buf.get(bufferIndex(3, 0));
        this.m31 = buf.get(bufferIndex(3, 1));
        this.m32 = buf.get(bufferIndex(3, 2));
        this.m33 = buf.get(bufferIndex(3, 3));
    }
}
