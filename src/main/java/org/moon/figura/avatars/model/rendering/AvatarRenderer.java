package org.moon.figura.avatars.model.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.ParentType;
import org.moon.figura.avatars.model.Transformable;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;

import java.util.HashMap;

/**
 * Mainly exists as an abstract superclass for VAO-based and
 * immediate mode avatar renderers. (VAO-based don't exist yet)
 */
public abstract class AvatarRenderer {

    protected final Avatar avatar;
    public FiguraModelPart root;

    // -- rendering data -- //

    //entity
    public Entity entity;
    public float yaw, tickDelta;
    public int light;
    public int overlay;
    public float alpha;

    //matrices
    public PoseStack matrices;
    public MultiBufferSource bufferSource;
    public LivingEntityRenderer<?, ?> entityRenderer;
    public boolean allowMatrixUpdate = false;

    public PartFilterScheme currentFilterScheme;
    public HashMap<ParentType, Transformable> pivotCustomizations = new HashMap<>();
    protected static int shouldRenderPivots;

    public AvatarRenderer(Avatar avatar) {
        this.avatar = avatar;
    }

    public abstract void render();
    public abstract void renderSpecialParts();
    public void clean() {
        root.clean();
    }

    /**
     * Returns the matrix for an entity, used to transform from entity space to world space.
     * @param e The entity to get the matrix for.
     * @return A matrix which represents the transformation from entity space to part space.
     */
    public static FiguraMat4 entityToWorldMatrix(Entity e, float delta) {
        double yaw = e instanceof LivingEntity le ? Mth.lerp(delta, le.yBodyRotO, le.yBodyRot) : e.getViewYRot(Minecraft.getInstance().getFrameTime());
        FiguraMat4 result = FiguraMat4.createYRotationMatrix(180 - yaw);
        result.translate(e.getPosition(delta));
        return result;
    }

    public static double getYawOffsetRot(Entity e, float delta) {
        double yaw = e instanceof LivingEntity le ? Mth.lerp(delta, le.yBodyRotO, le.yBodyRot) : e.getViewYRot(Minecraft.getInstance().getFrameTime());
        return 180 - yaw;
    }

    /**
     * Gets a matrix to transform from world space to view space, based on the
     * player's camera position and orientation.
     * @return That matrix.
     */
    public static FiguraMat4 worldToViewMatrix() {
        Minecraft client = Minecraft.getInstance();
        Camera camera = client.gameRenderer.getMainCamera();
        Matrix3f cameraMat3f = new Matrix3f(camera.rotation());
        cameraMat3f.invert();
        FiguraMat4 result = FiguraMat4.createTranslationMatrix(camera.getPosition().scale(-1));
        FiguraMat3 cameraMat = FiguraMat3.fromMatrix3f(cameraMat3f);
        result.multiply(cameraMat.augmented());
        cameraMat.free();
        result.scale(-1, 1, -1);
        return result;
    }
}
