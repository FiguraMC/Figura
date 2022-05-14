package org.moon.figura.avatars.model.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;

import java.util.function.BiPredicate;


/**
 * Mainly exists as an abstract superclass for VAO-based and
 * immediate mode avatar renderers.
 */
public abstract class AvatarRenderer {

    protected final Avatar avatar;
    public FiguraModelPart root;

    public Entity entity;
    public float yaw, tickDelta;
    public PoseStack matrices;
    public int light;
    public MultiBufferSource bufferSource;
    public LivingEntityRenderer<?, ?> entityRenderer;
    public ElytraModel<?> elytraModel;
    public PartFilterScheme currentFilterScheme;

    /**
     * FiguraModelPart: The current model part.
     * Boolean input: The result of the predicate from the previous part.
     * Boolean output: The result for the current part.
     */
    public static final PartFilterScheme RENDER_REGULAR = new PartFilterScheme(true, (part, previousPassed) -> {
        //Allow everything except descendants of WORLD parts.
        if (part.parentType == FiguraModelPart.ParentType.World)
            return false;
        return previousPassed;
    });
    public static final PartFilterScheme RENDER_WORLD = new PartFilterScheme(false, (part, previousPassed) -> {
        //Allow nothing except descendants of WORLD parts.
        if (part.parentType == FiguraModelPart.ParentType.World)
            return true;
        return previousPassed;
    });
    public static final PartFilterScheme RENDER_HEAD = new PartFilterScheme(false, (part, previousPassed) -> {
        //Allow nothing except descendants of HEAD parts.
        if (part.parentType == FiguraModelPart.ParentType.Head)
            return true;
        return previousPassed;
    });
    public static final PartFilterScheme RENDER_LEFT_ARM = new PartFilterScheme(false, (part, previousPassed) -> {
        //Allow nothing except descendants of LEFT_ARM parts.
        if (part.parentType == FiguraModelPart.ParentType.LeftArm)
            return true;
        return previousPassed;
    });
    public static final PartFilterScheme RENDER_RIGHT_ARM = new PartFilterScheme(false, (part, previousPassed) -> {
        //Allow nothing except descendants of LEFT_ARM parts.
        if (part.parentType == FiguraModelPart.ParentType.RightArm)
            return true;
        return previousPassed;
    });

    public record PartFilterScheme(boolean initialValue, BiPredicate<FiguraModelPart, Boolean> predicate) {}

    public AvatarRenderer(Avatar avatar) {
        this.avatar = avatar;
    }

    public abstract void render();
    public abstract void renderWorldParts();
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
