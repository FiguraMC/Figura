package org.figuramc.figura.mixin.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.ducks.PlayerModelAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Map;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> implements PlayerModelAccessor {

    // Fake cape ModelPart which we set rotations of.
    // This is because the internal cape renderer uses the matrix stack,
    // instead of setting rotations like every single other ModelPart they render...
    @Unique
    public ModelPart fakeCloak = new ModelPart(List.of(), Map.of());

    @Final
    @Shadow
    private ModelPart cloak;

    public PlayerModelMixin(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public ModelPart figura$getCloak() {
        return cloak;
    }

    @Override
    public ModelPart figura$getFakeCloak() {
        return fakeCloak;
    }

}
