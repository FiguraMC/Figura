package org.figuramc.figura.ducks;

import net.minecraft.client.Camera;

public interface GameRendererAccessor {

    double figura$getFov(Camera camera, float tickDelta, boolean changingFov);
}
