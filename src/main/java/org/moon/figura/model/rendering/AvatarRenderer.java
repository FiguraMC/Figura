package org.moon.figura.model.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.model.FiguraModelPart;


/**
 * Mainly exists as an abstract superclass for VAO-based and
 * immediate mode avatar renderers.
 */
public abstract class AvatarRenderer {

    protected final Avatar avatar;
    protected FiguraModelPart root;

    public Entity entity;
    public float yaw, tickDelta;
    public PoseStack matrices;
    public int light;
    public MultiBufferSource bufferSource;

    public AvatarRenderer(Avatar avatar, CompoundTag avatarCompound) {
        this.avatar = avatar;
    }

    public abstract void render();

    /**
     * Returns the matrix for an entity, used to transform from entity space to world space.
     * @param e The entity to get the matrix for.
     * @return A matrix which represents the transformation from entity space to part space.
     */
    protected static FiguraMat4 entityToWorldMatrix(Entity e, float delta) {
        double yaw;
        if (e instanceof LivingEntity)
            yaw = Mth.lerp(delta, ((LivingEntity) e).yBodyRotO, ((LivingEntity) e).yBodyRot);
        else
            yaw = e.getViewYRot(Minecraft.getInstance().getFrameTime());
        FiguraMat4 result = FiguraMat4.createYRotationMatrix(180 - yaw);
        result.translate(e.getPosition(delta));
        return result;
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
