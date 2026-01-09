package org.figuramc.figura.mixin.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleGroup;
import org.figuramc.figura.ducks.ParticleEngineAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ParticleGroup.class)
public class ParticleGroupMixin {
    // This fixes a conflict with Optifine having slightly different args + it should be more stable in general, capturing Locals is bad practice
    @ModifyVariable(method = "tickParticles", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V", ordinal = 0))
    private Particle tickParticleList(Particle particle) {
        ((ParticleEngineAccessor)Minecraft.getInstance().particleEngine).figura$removeParticle(particle);
        return particle;
    }
}
