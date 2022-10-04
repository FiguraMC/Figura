package org.moon.figura.avatars.model.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.ParentType;
import org.moon.figura.avatars.model.VanillaModelData;
import org.moon.figura.avatars.model.rendering.texture.FiguraTexture;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Mainly exists as an abstract superclass for VAO-based and
 * immediate mode avatar renderers. (VAO-based don't exist yet)
 */
public abstract class AvatarRenderer {

    protected final Avatar avatar;
    public FiguraModelPart root;

    protected boolean isRendering, dirty;

    // -- rendering data -- //

    //entity
    public Entity entity;
    public float yaw, tickDelta;
    public int light;
    public int overlay;
    public float alpha;
    public boolean translucent, glowing;

    //matrices
    public PoseStack matrices;
    public MultiBufferSource bufferSource;
    public VanillaModelData vanillaModelData = new VanillaModelData();
    public boolean allowMatrixUpdate = false;

    public PartFilterScheme currentFilterScheme;
    public final HashMap<ParentType, ConcurrentLinkedQueue<Pair<FiguraMat4, FiguraMat3>>> pivotCustomizations = new HashMap<>();
    public final List<FiguraTextureSet> textureSets = new ArrayList<>();
    public final HashMap<String, FiguraTexture> customTextures = new HashMap<>();
    protected static int shouldRenderPivots;
    public boolean allowHiddenTransforms = true;
    public boolean allowRenderTasks = true;
    public boolean allowSkullRendering = true;
    public boolean allowPivotParts = true;

    public AvatarRenderer(Avatar avatar) {
        this.avatar = avatar;

        //Textures
        ListTag texturesList = avatar.nbt.getList("textures", Tag.TAG_COMPOUND);
        for (int i = 0; i < texturesList.size(); i++) {
            CompoundTag tag = texturesList.getCompound(i);

            String name = tag.getString("name");

            byte[] mainData = tag.getByteArray("default");
            mainData = mainData.length == 0 ? null : mainData;

            byte[] emissiveData = tag.getByteArray("emissive");
            emissiveData = emissiveData.length == 0 ? null : emissiveData;

            textureSets.add(new FiguraTextureSet(avatar, name, mainData, emissiveData));
        }

        avatar.hasTexture = !texturesList.isEmpty();
    }

    public abstract int render();
    public abstract int renderSpecialParts();
    protected void clean() {
        root.clean();
        for (FiguraTexture texture : customTextures.values())
            texture.close();
    }

    public void invalidate() {
        this.dirty = true;
        if (!this.isRendering)
            clean();
    }

    /**
     * Returns the matrix for an entity, used to transform from entity space to world space.
     * @param e The entity to get the matrix for.
     * @return A matrix which represents the transformation from entity space to part space.
     */
    public static FiguraMat4 entityToWorldMatrix(Entity e, float delta) {
        double yaw = e instanceof LivingEntity le ? Mth.lerp(delta, le.yBodyRotO, le.yBodyRot) : e.getViewYRot(Minecraft.getInstance().getFrameTime());
        FiguraMat4 result = FiguraMat4.of();
        result.rotateX(180 - yaw);
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
        FiguraMat4 result = FiguraMat4.of();
        Vec3 cameraPos = camera.getPosition().scale(-1);
        result.translate(cameraPos.x, cameraPos.y, cameraPos.z);
        FiguraMat3 cameraMat = FiguraMat3.fromMatrix3f(cameraMat3f);
        result.multiply(cameraMat.augmented());
        cameraMat.free();
        result.scale(-1, 1, -1);
        return result;
    }
}
