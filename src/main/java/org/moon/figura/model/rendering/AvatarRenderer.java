package org.moon.figura.model.rendering;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;


/**
 * Mainly exists as an abstract superclass for VAO-based and
 * immediate mode avatar renderers.
 */
public abstract class AvatarRenderer {

    protected final Avatar avatar;

    public Entity entity;
    public float yaw, tickDelta;
    public MatrixStack matrices;
    public int light;
    public VertexConsumerProvider vcp;

    public AvatarRenderer(Avatar avatar, NbtCompound avatarCompound) {
        this.avatar = avatar;
    }

    public abstract void render();

    /**
     * Returns the matrix for an entity, used to transform from entity space to world space.
     * This is inelegant, will rework later. Currently makes a distinction for LivingEntities to use body yaw,
     * and non-living entities to just lerp their yaws.
     * @param e The entity to get the matrix for.
     * @return A matrix which represents the transformation from entity space to part space.
     */
    protected static FiguraMat4 entityToWorldMatrix(Entity e, float delta) {
        double yaw;
        if (e instanceof LivingEntity)
            yaw = MathHelper.lerp(delta, ((LivingEntity) e).prevBodyYaw, ((LivingEntity) e).bodyYaw);
        else
            yaw = e.getYaw(MinecraftClient.getInstance().getTickDelta());
        FiguraMat4 result = FiguraMat4.createYRotationMatrix(180 - yaw);
        result.translate(e.getLerpedPos(delta));
        return result;
    }

    /**
     * Gets a matrix to transform from world space to part space, based on the player's camera position.
     * @return That matrix.
     */
    public static FiguraMat4 worldToViewMatrix() {
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        Matrix3f cameraMat3f = new Matrix3f(camera.getRotation());
        cameraMat3f.invert();
        FiguraMat4 result = FiguraMat4.createTranslationMatrix(camera.getPos().multiply(-1));
        FiguraMat3 cameraMat = FiguraMat3.fromMatrix3f(cameraMat3f);
        result.multiply(cameraMat.augmented());
        cameraMat.free();
        result.scale(-1, 1, -1);
        return result;
    }
}
