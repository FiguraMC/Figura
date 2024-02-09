package org.figuramc.figura.mixin;

import net.minecraft.client.particle.Particle;
import org.figuramc.figura.ducks.extensions.ParticleExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Particle.class)
public class ParticleMixin implements ParticleExtension {
    @Shadow protected double xd;

    @Shadow protected double yd;

    @Shadow protected double zd;

    @Override
    public void figura$setParticleSpeed(double velocityX, double velocityY, double velocityZ) {
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;
    }
}
