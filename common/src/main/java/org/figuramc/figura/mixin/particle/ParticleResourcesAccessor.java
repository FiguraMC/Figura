package org.figuramc.figura.mixin.particle;

import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ParticleResources.class)
public interface ParticleResourcesAccessor {

    @Accessor("spriteSets")
    Map<ResourceLocation, ParticleResources.MutableSpriteSet> figura$getSpriteSets();

}
