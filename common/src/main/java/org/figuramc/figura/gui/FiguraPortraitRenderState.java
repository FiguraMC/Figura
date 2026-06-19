package org.figuramc.figura.gui;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.avatar.Avatar;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record FiguraPortraitRenderState(
        @Nullable Avatar avatar,
        @Nullable Identifier fallbackSkin,
        float modelScale,
        boolean upsideDown,
        int x0,
        int y0,
        int x1,
        int y1,
        float scale,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {

    public FiguraPortraitRenderState(
            @Nullable Avatar avatar,
            @Nullable Identifier fallbackSkin,
            float modelScale,
            boolean upsideDown,
            int x0,
            int y0,
            int x1,
            int y1,
            float scale,
            @Nullable ScreenRectangle screenRectangle
    ) {
        this(
                avatar, fallbackSkin, modelScale, upsideDown, x0, y0, x1, y1, scale, screenRectangle, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, screenRectangle)
        );
    }
}
