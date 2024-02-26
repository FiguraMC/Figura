package org.figuramc.figura.model.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
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
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.model.VanillaModelData;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.model.rendering.texture.FiguraTextureSet;
import org.joml.Matrix3f;
import org.joml.Matrix4d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Mainly exists as an abstract superclass for VAO-based and
 * immediate mode avatar renderers. (VAO-based don't exist yet)
 */
public abstract class AvatarRenderer {

    protected final Avatar avatar;
    public FiguraModelPart root;

    protected final Map<ParentType, List<FiguraModelPart>> separatedParts = new ConcurrentHashMap<>();

    protected boolean isRendering, dirty;

    // -- rendering data -- // 

    // entity
    public Entity entity;
    public float yaw, tickDelta;
    public int light;
    public int overlay;
    public float alpha;
    public boolean translucent, glowing;
    public FiguraMat4 posMat = FiguraMat4.of();
    public FiguraMat3 normalMat = FiguraMat3.of();

    // matrices
    public MultiBufferSource bufferSource;
    public VanillaModelData vanillaModelData = new VanillaModelData();

    public PartFilterScheme currentFilterScheme;
    public final HashMap<ParentType, ConcurrentLinkedQueue<Pair<FiguraMat4, FiguraMat3>>> pivotCustomizations = new HashMap<>(ParentType.values().length);
    protected final List<FiguraTextureSet> textureSets = new ArrayList<>();
    public final HashMap<String, FiguraTexture> textures = new HashMap<>();
    public final HashMap<String, FiguraTexture> customTextures = new HashMap<>();
    protected static int shouldRenderPivots;
    public boolean allowMatrixUpdate = false;
    public boolean allowHiddenTransforms = true;
    public boolean interceptRendersIntoFigura = true;
    public boolean allowPivotParts = true;
    public boolean updateLight = false;
    public boolean doIrisEmissiveFix = false;
    public boolean offsetRenderLayers = false;
    public boolean ignoreVanillaVisibility = false;
    public FiguraModelPart itemToRender;

    public AvatarRenderer(Avatar avatar) {
        this.avatar = avatar;

        // textures

        CompoundTag nbt = avatar.nbt.getCompound("textures");
        CompoundTag src = nbt.getCompound("src");

        // src files
        for (String key : src.getAllKeys()) {
            byte[] bytes = src.getByteArray(key);
            if (bytes.length > 0) {
                textures.put(key, new FiguraTexture(avatar, key, bytes));
            } else {
                ListTag size = src.getList(key, Tag.TAG_INT);
                textures.put(key, new FiguraTexture(avatar, key, size.getInt(0), size.getInt(1)));
            }
        }

        // data files
        ListTag texturesList = nbt.getList("data", Tag.TAG_COMPOUND);
        for (Tag t : texturesList) {
            CompoundTag tag = (CompoundTag) t;
            textureSets.add(new FiguraTextureSet(
                    getTextureName(tag),
                    textures.get(tag.getString("d")),
                    textures.get(tag.getString("e")),
                    textures.get(tag.getString("s")),
                    textures.get(tag.getString("n"))
            ));
        }

        avatar.hasTexture = !texturesList.isEmpty();
    }

    private String getTextureName(CompoundTag tag) {
        String s = tag.getString("d");
        if (!s.isEmpty()) return s;
        s = tag.getString("e");
        if (!s.isEmpty()) return s.substring(0, s.length() - 2);
        s = tag.getString("s");
        if (!s.isEmpty()) return s.substring(0, s.length() - 2);
        s = tag.getString("n");
        if (!s.isEmpty()) return s.substring(0, s.length() - 2);
        return "";
    }

    public FiguraTexture getTexture(String name) {
        FiguraTexture texture = customTextures.get(name);
        if (texture != null)
            return texture;

        for (Map.Entry<String, FiguraTexture> entry : textures.entrySet()) {
            if (entry.getKey().equals(name))
                return entry.getValue();
        }

        return null;
    }

    public abstract int render();
    public abstract int renderSpecialParts();
    public abstract void updateMatrices();

    protected void clean() {
        for (FiguraTextureSet set : textureSets)
            set.clean();
        for (FiguraTexture texture : customTextures.values())
            texture.close();
    }

    public void invalidate() {
        this.dirty = true;
        if (!this.isRendering)
            clean();
    }

    public void sortParts() {
        separatedParts.clear();
        _sortParts(root);
    }

    private void _sortParts(FiguraModelPart part) {
        if (part.parentType.isSeparate) {
            List<FiguraModelPart> list = separatedParts.computeIfAbsent(part.parentType, parentType -> new ArrayList<>());
            list.add(part);
        }

        for (FiguraModelPart child : part.children)
            _sortParts(child);
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
        Matrix3f cameraMat3f = new Matrix3f().rotation(camera.rotation());
        cameraMat3f.invert();
        FiguraMat4 result = FiguraMat4.of();
        Vec3 cameraPos = camera.getPosition().scale(-1);
        result.translate(cameraPos.x, cameraPos.y, cameraPos.z);
        FiguraMat3 cameraMat = FiguraMat3.of().set(cameraMat3f);
        result.multiply(cameraMat.augmented());
        result.scale(-1, 1, -1);
        return result;
    }

    public void setupRenderer(PartFilterScheme currentFilterScheme, MultiBufferSource bufferSource, PoseStack matrices, float tickDelta, int light, float alpha, int overlay, boolean translucent, boolean glowing) {
        this.setupRenderer(currentFilterScheme, bufferSource, tickDelta, light, alpha, overlay, translucent, glowing);
        this.setMatrices(matrices);
    }

    public void setupRenderer(PartFilterScheme currentFilterScheme, MultiBufferSource bufferSource, PoseStack matrices, float tickDelta, int light, float alpha, int overlay, boolean translucent, boolean glowing, double camX, double camY, double camZ) {
        this.setupRenderer(currentFilterScheme, bufferSource, tickDelta, light, alpha, overlay, translucent, glowing);
        this.setMatrices(camX, camY, camZ, matrices);
    }

    private void setupRenderer(PartFilterScheme currentFilterScheme, MultiBufferSource bufferSource, float tickDelta, int light, float alpha, int overlay, boolean translucent, boolean glowing) {
        this.currentFilterScheme = currentFilterScheme;
        this.bufferSource = bufferSource;
        this.tickDelta = tickDelta;
        this.light = light;
        this.alpha = alpha;
        this.overlay = overlay;
        this.translucent = translucent;
        this.glowing = glowing;
    }

    public void setMatrices(PoseStack matrices) {
        PoseStack.Pose pose = matrices.last();
        this.posMat.set(pose.pose());
        this.normalMat.set(pose.normal());
    }

    public void setMatrices(double camX, double camY, double camZ, PoseStack matrices) {
        PoseStack.Pose pose = matrices.last();

        // pos
        Matrix4d posMat = new Matrix4d(pose.pose());
        posMat.translate(-camX, -camY, -camZ);
        posMat.scale(-1, -1, 1);
        this.posMat.set(posMat);

        // normal
        Matrix3f normalMat = new Matrix3f(pose.normal());
        normalMat.scale(-1, -1, 1);
        this.normalMat.set(normalMat);
    }
}
