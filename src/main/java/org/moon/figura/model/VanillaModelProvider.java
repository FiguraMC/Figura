package org.moon.figura.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.moon.figura.ducks.PlayerModelAccessor;
import org.moon.figura.mixin.render.layers.elytra.ElytraModelAccessor;

import java.util.function.Function;

public enum VanillaModelProvider {
    HEAD(model -> ((HumanoidModel<?>) model).head),
    BODY(model -> ((HumanoidModel<?>) model).body),
    LEFT_ARM(model -> ((HumanoidModel<?>) model).leftArm),
    RIGHT_ARM(model -> ((HumanoidModel<?>) model).rightArm),
    LEFT_LEG(model -> ((HumanoidModel<?>) model).leftLeg),
    RIGHT_LEG(model -> ((HumanoidModel<?>) model).rightLeg),

    HAT(model -> ((HumanoidModel<?>) model).hat),
    JACKET(model -> ((PlayerModel<?>) model).jacket),
    LEFT_SLEEVE(model -> ((PlayerModel<?>) model).leftSleeve),
    RIGHT_SLEEVE(model -> ((PlayerModel<?>) model).rightSleeve),
    LEFT_PANTS(model -> ((PlayerModel<?>) model).leftPants),
    RIGHT_PANTS(model -> ((PlayerModel<?>) model).rightPants),
    CAPE(model -> ((PlayerModelAccessor) model).figura$getCloak()),
    FAKE_CAPE(model -> ((PlayerModelAccessor) model).figura$getFakeCloak()),

    LEFT_ELYTRON(model -> model instanceof ElytraModelAccessor m ? m.getLeftWing() : null),
    RIGHT_ELYTRON(model -> model instanceof ElytraModelAccessor m ? m.getRightWing() : null);

    public final Function<EntityModel<?>, ModelPart> func;

    VanillaModelProvider(Function<EntityModel<?>, ModelPart> func) {
        this.func = func;
    }
}