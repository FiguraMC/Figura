package org.moon.figura.mixin.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.moon.figura.ducks.ParticleEngineAccessor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin implements ParticleEngineAccessor {

    @Final @Shadow private Map<ResourceLocation, SpriteSet> spriteSets;

    @Shadow @Nullable protected abstract <T extends ParticleOptions> Particle makeParticle(T parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ);

    @Shadow public abstract void add(Particle particle);

    @Unique private final HashMap<Particle, UUID> particleMap = new HashMap<>();

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;tickParticle(Lnet/minecraft/client/particle/Particle;)V"), method = "tickParticleList", ordinal = 0)
    private Particle tickParticleList(Particle particle) {
        particleMap.remove(particle);
        return particle;
    }

    @Override @Intrinsic
    public <T extends ParticleOptions> Particle figura$makeParticle(T parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        return this.makeParticle(parameters, x, y, z, velocityX, velocityY, velocityZ);
    }

    @Override @Intrinsic
    public void figura$spawnParticle(Particle particle, UUID owner) {
        particleMap.put(particle, owner);
        this.add(particle);
    }

    @Override @Intrinsic
    public void figura$clearParticles(UUID owner) {
        Iterator<Map.Entry<Particle, UUID>> iterator = particleMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Particle, UUID> entry = iterator.next();

            if ((owner == null || entry.getValue().equals(owner))) {
                if (entry.getKey() != null)
                    entry.getKey().remove();
                iterator.remove();
            }
        }
    }

    @Override @Intrinsic
    public SpriteSet figura$getParticleSprite(ResourceLocation particleID) {
        return spriteSets.get(particleID);
    }
}
