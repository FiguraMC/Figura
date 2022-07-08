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
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;

import java.util.function.BiPredicate;


/**
 * Mainly exists as an abstract superclass for VAO-based and
 * immediate mode avatar renderers. (VAO-based don't exist yet)
 */
public abstract class AvatarRenderer {

    protected final Avatar avatar;
    public FiguraModelPart root;

    public Entity entity;
    public float yaw, tickDelta, alpha;
    public PoseStack matrices;
    public int light;
    public int overlay;
    public MultiBufferSource bufferSource;
    public LivingEntityRenderer<?, ?> entityRenderer;
    public PartFilterScheme currentFilterScheme;

    public boolean allowMatrixUpdate = false;

    /**
     * FiguraModelPart: The current model part.
     * Boolean input: The result of the predicate from the previous part.
     * Boolean output: The result for the current part.
     */
    public enum PartFilterScheme {
        MODEL(true, (part, previousPassed) -> {
            if (ParentType.SPECIAL_PARTS.contains(part.parentType))
                return false;
            return previousPassed;
        }),
        WORLD(false, (part, previousPassed) -> {
            if (part.parentType == ParentType.World)
                return true;
            return previousPassed;
        }),
        HEAD(false, (part, previousPassed) -> {
            if (part.parentType == ParentType.Head)
                return true;
            return previousPassed;
        }),
        LEFT_ARM(false, (part, previousPassed) -> {
            if (part.parentType == ParentType.LeftArm)
                return true;
            return previousPassed;
        }),
        RIGHT_ARM(false, (part, previousPassed) -> {
            if (part.parentType == ParentType.RightArm)
                return true;
            return previousPassed;
        }),
        HUD(false, (part, previousPassed) -> {
            if (part.parentType == ParentType.Hud)
                return true;
            return previousPassed;
        });

        public final boolean initialValue;
        public final BiPredicate<FiguraModelPart, Boolean> predicate;

        PartFilterScheme(boolean initialValue, BiPredicate<FiguraModelPart, Boolean> predicate) {
            this.initialValue = initialValue;
            this.predicate = predicate;
        }
    }

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
        double yaw;
        if (e instanceof LivingEntity)
            yaw = Mth.lerp(delta, ((LivingEntity) e).yBodyRotO, ((LivingEntity) e).yBodyRot);
        else
            yaw = e.getViewYRot(Minecraft.getInstance().getFrameTime());
        FiguraMat4 result = FiguraMat4.createYRotationMatrix(180 - yaw);
        result.translate(e.getPosition(delta));
        return result;
    }

    public static double getYawOffsetRot(Entity e, float delta) {
        double yaw;
        if (e instanceof LivingEntity)
            yaw = Mth.lerp(delta, ((LivingEntity) e).yBodyRotO, ((LivingEntity) e).yBodyRot);
        else
            yaw = e.getViewYRot(Minecraft.getInstance().getFrameTime());
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
