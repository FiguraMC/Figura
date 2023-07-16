package org.figuramc.figura.mixin.particle;

import net.minecraft.client.particle.SingleQuadParticle;
import org.figuramc.figura.ducks.SingleQuadParticleAccessor;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SingleQuadParticle.class)
public abstract class SingleQuadParticleMixin implements SingleQuadParticleAccessor {

    @Shadow protected float quadSize;

    @Override
    @Intrinsic
    public void figura$fixQuadSize() {
        this.quadSize = 0.2f;
    }
}
