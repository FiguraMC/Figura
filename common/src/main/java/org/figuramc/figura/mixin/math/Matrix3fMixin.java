package org.figuramc.figura.mixin.math;

import com.mojang.math.Matrix3f;
import org.figuramc.figura.ducks.extensions.Matrix3fExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.FloatBuffer;

@Mixin(Matrix3f.class)
public class Matrix3fMixin implements Matrix3fExtension {
    @Shadow protected float m00;

    @Shadow protected float m22;

    @Shadow protected float m21;

    @Shadow protected float m20;

    @Shadow protected float m12;

    @Shadow protected float m11;

    @Shadow protected float m10;

    @Shadow protected float m02;

    @Shadow protected float m01;

    @Unique
    private static int figura$bufferIndex(int x, int y) {
        return y * 3 + x;
    }

    @Unique
    public void figura$store(FloatBuffer buf) {
        buf.put(figura$bufferIndex(0, 0), this.m00);
        buf.put(figura$bufferIndex(0, 1), this.m01);
        buf.put(figura$bufferIndex(0, 2), this.m02);
        buf.put(figura$bufferIndex(1, 0), this.m10);
        buf.put(figura$bufferIndex(1, 1), this.m11);
        buf.put(figura$bufferIndex(1, 2), this.m12);
        buf.put(figura$bufferIndex(2, 0), this.m20);
        buf.put(figura$bufferIndex(2, 1), this.m21);
        buf.put(figura$bufferIndex(2, 2), this.m22);
    }

    @Unique
    public void figura$load(FloatBuffer buf) {
        this.m00 = buf.get(figura$bufferIndex(0, 0));
        this.m01 = buf.get(figura$bufferIndex(0, 1));
        this.m02 = buf.get(figura$bufferIndex(0, 2));
        this.m10 = buf.get(figura$bufferIndex(1, 0));
        this.m11 = buf.get(figura$bufferIndex(1, 1));
        this.m12 = buf.get(figura$bufferIndex(1, 2));
        this.m20 = buf.get(figura$bufferIndex(2, 0));
        this.m21 = buf.get(figura$bufferIndex(2, 1));
        this.m22 = buf.get(figura$bufferIndex(2, 2));
    }
}
