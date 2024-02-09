package org.figuramc.figura.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.figuramc.figura.ducks.PlayerModelAccessor;
import org.figuramc.figura.mixin.render.layers.elytra.ElytraModelAccessor;

import java.util.function.Function;

public enum VanillaModelProvider {
    HEAD(model -> model instanceof HumanoidModel<?> ? ((HumanoidModel<?>) model).head : null),
    BODY(model -> model instanceof HumanoidModel<?> ? ((HumanoidModel<?>) model).body : null),
    LEFT_ARM(model -> model instanceof HumanoidModel<?> ? ((HumanoidModel<?>) model).leftArm : null),
    RIGHT_ARM(model -> model instanceof HumanoidModel<?> ? ((HumanoidModel<?>) model).rightArm : null),
    LEFT_LEG(model -> model instanceof HumanoidModel<?> ? ((HumanoidModel<?>) model).leftLeg : null),
    RIGHT_LEG(model -> model instanceof HumanoidModel<?> ? ((HumanoidModel<?>) model).rightLeg : null),
    HAT(model -> model instanceof HumanoidModel<?> ? ((HumanoidModel<?>) model).hat : null),

    JACKET(model -> model instanceof PlayerModel<?> ? ((PlayerModel<?>) model).jacket : null),
    LEFT_SLEEVE(model -> model instanceof PlayerModel<?> ? ((PlayerModel<?>) model).leftSleeve : null),
    RIGHT_SLEEVE(model -> model instanceof PlayerModel<?> ? ((PlayerModel<?>) model).rightSleeve : null),
    LEFT_PANTS(model -> model instanceof PlayerModel<?> ? ((PlayerModel<?>) model).leftPants : null),
    RIGHT_PANTS(model -> model instanceof PlayerModel<?> ? ((PlayerModel<?>) model).rightPants : null),

    CAPE(model -> model instanceof PlayerModel<?> ? ((PlayerModelAccessor) model).figura$getCloak() : null),
    FAKE_CAPE(model -> model instanceof PlayerModel<?> ? ((PlayerModelAccessor) model).figura$getFakeCloak() : null),

    LEFT_ELYTRON(model -> model instanceof ElytraModelAccessor ? ((ElytraModelAccessor) model).getLeftWing() : null),
    RIGHT_ELYTRON(model -> model instanceof ElytraModelAccessor ? ((ElytraModelAccessor) model).getRightWing() : null);

    public final Function<EntityModel<?>, ModelPart> func;

    VanillaModelProvider(Function<EntityModel<?>, ModelPart> func) {
        this.func = func;
    }
}