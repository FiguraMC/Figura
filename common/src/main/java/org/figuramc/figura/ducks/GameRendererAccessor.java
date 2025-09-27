package org.figuramc.figura.ducks;

import org.joml.Matrix4f;

import net.minecraft.client.Camera;

public interface GameRendererAccessor {
    Matrix4f figura$getBobbingMatrix();

    double figura$getFov(Camera camera, float tickDelta, boolean changingFov);
}
