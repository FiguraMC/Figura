package org.moon.figura.avatars.vanilla;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.math.vector.FiguraVec3;

public class VanillaPartOffsetManager {

    /**
     * Returns a NEW vector, so you can modify it and free it when you're done.
     * @param model
     * @param parentType
     * @return
     */
    public static FiguraVec3 getVanillaOffset(EntityModel<?> model, FiguraModelPart.ParentType parentType) {
        if (model instanceof PlayerModel<?>)
            return switch (parentType) {
                case LeftArm -> FiguraVec3.of(5, 2, 0);
                case RightArm -> FiguraVec3.of(-5, 2, 0);
                case LeftLeg -> FiguraVec3.of(1.9, 12, 0);
                case RightLeg -> FiguraVec3.of(-1.9, 12, 0);
                default -> FiguraVec3.of();
            };
        if (model instanceof ElytraModel<?>)
            return switch (parentType) {
                case LeftElytron -> FiguraVec3.of(5, 0, 0);
                case RightElytron -> FiguraVec3.of(-5, 0, 0);
                default -> FiguraVec3.of();
            };
        return FiguraVec3.of();
    }





}
