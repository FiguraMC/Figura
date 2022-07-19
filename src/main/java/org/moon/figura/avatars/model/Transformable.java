package org.moon.figura.avatars.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import org.moon.figura.ducks.PoseStackAccessor;
import org.moon.figura.math.vector.FiguraVec3;

public class Transformable {

    public FiguraVec3 pos, rot, scale;
    public PoseStack.Pose pose;

    public void apply(PoseStack stack) {
        if (rot != null) {
            stack.mulPose(Vector3f.XP.rotationDegrees((float) rot.x));
            stack.mulPose(Vector3f.YP.rotationDegrees((float) rot.y));
            stack.mulPose(Vector3f.ZP.rotationDegrees((float) rot.z));
        }

        if (pos != null)
            stack.translate(pos.x, pos.y, pos.z);

        if (scale != null)
            stack.scale((float) scale.x, (float) scale.y, (float) scale.z);
    }

    public PoseStack getAsStack() {
        if (pose == null)
            return null;

        PoseStack stack = new PoseStack();
        ((PoseStackAccessor) stack).pushPose(pose);
        return stack;
    }
}
