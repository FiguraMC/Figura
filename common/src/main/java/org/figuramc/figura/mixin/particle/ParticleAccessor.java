package org.figuramc.figura.mixin.particle;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Particle.class)
public interface ParticleAccessor {

    @Intrinsic
    @Accessor
    void setGravity(float gravity);

    @Intrinsic
    @Accessor
    float getGravity();

    @Intrinsic
    @Accessor
    void setHasPhysics(boolean physics);

    @Intrinsic
    @Accessor
    boolean getHasPhysics();

    @Intrinsic
    @Accessor
    void setXo(double xo);

    @Intrinsic
    @Accessor
    void setYo(double yo);

    @Intrinsic
    @Accessor
    void setZo(double zo);

    @Intrinsic
    @Invoker("setAlpha")
    void setParticleAlpha(float alpha);

    @Intrinsic
    @Accessor("x")
    double getX();

    @Intrinsic
    @Accessor("y")
    double getY();

    @Intrinsic
    @Accessor("z")
    double getZ();

    @Intrinsic
    @Accessor
    float getBbWidth();

    @Intrinsic
    @Accessor
    float getRCol();

    @Intrinsic
    @Accessor
    float getGCol();

    @Intrinsic
    @Accessor
    float getBCol();

    @Intrinsic
    @Accessor
    float getAlpha();

    @Intrinsic
    @Accessor
    double getXd();

    @Intrinsic
    @Accessor
    double getYd();

    @Intrinsic
    @Accessor
    double getZd();
}
