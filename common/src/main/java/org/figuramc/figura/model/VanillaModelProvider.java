package org.figuramc.figura.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.figuramc.figura.ducks.PlayerModelAccessor;
import org.figuramc.figura.mixin.render.layers.elytra.ElytraModelAccessor;

import java.util.function.Function;

public enum VanillaModelProvider {
    HEAD(model -> model instanceof HumanoidModel<?> m ? m.head : null),
    BODY(model -> model instanceof HumanoidModel<?> m ? m.body : null),
    LEFT_ARM(model -> model instanceof HumanoidModel<?> m ? m.leftArm : null),
    RIGHT_ARM(model -> model instanceof HumanoidModel<?> m ? m.rightArm : null),
    LEFT_LEG(model -> model instanceof HumanoidModel<?> m ? m.leftLeg : null),
    RIGHT_LEG(model -> model instanceof HumanoidModel<?> m ? m.rightLeg : null),
    HAT(model -> model instanceof HumanoidModel<?> m ? m.hat : null),

    JACKET(model -> model instanceof PlayerModel<?> m ? m.jacket : null),
    LEFT_SLEEVE(model -> model instanceof PlayerModel<?> m ? m.leftSleeve : null),
    RIGHT_SLEEVE(model -> model instanceof PlayerModel<?> m ? m.rightSleeve : null),
    LEFT_PANTS(model -> model instanceof PlayerModel<?> m ? m.leftPants : null),
    RIGHT_PANTS(model -> model instanceof PlayerModel<?> m ? m.rightPants : null),

    CAPE(model -> model instanceof PlayerModel<?> m ? ((PlayerModelAccessor) m).figura$getCloak() : null),
    FAKE_CAPE(model -> model instanceof PlayerModel<?> m ? ((PlayerModelAccessor) m).figura$getFakeCloak() : null),

    LEFT_ELYTRON(model -> model instanceof ElytraModelAccessor m ? m.getLeftWing() : null),
    RIGHT_ELYTRON(model -> model instanceof ElytraModelAccessor m ? m.getRightWing() : null);

    public final Function<EntityModel<?>, ModelPart> func;

    VanillaModelProvider(Function<EntityModel<?>, ModelPart> func) {
        this.func = func;
    }
}