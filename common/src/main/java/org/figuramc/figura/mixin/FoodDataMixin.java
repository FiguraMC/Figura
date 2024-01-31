package org.figuramc.figura.mixin;

import net.minecraft.world.food.FoodData;
import org.figuramc.figura.ducks.FoodDataAccesor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public abstract class FoodDataMixin implements FoodDataAccesor {
    @Override
    @Accessor("exhaustionLevel")
    public abstract float figura$getExhaustionLevel();
}
