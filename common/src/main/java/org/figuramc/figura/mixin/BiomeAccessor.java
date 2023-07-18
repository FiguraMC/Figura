package org.figuramc.figura.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Biome.class)
public interface BiomeAccessor {

    @Intrinsic
    @Invoker("getTemperature")
    float getTheTemperature(BlockPos blockPos);

    @Intrinsic
    @Accessor("climateSettings")
    Biome.ClimateSettings getClimateSettings();
}