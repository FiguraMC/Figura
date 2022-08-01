package org.moon.figura.avatars.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.rendering.ImmediateAvatarRenderer;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.avatars.model.rendering.texture.RenderTypes;
import org.moon.figura.avatars.model.rendertasks.BlockTask;
import org.moon.figura.avatars.model.rendertasks.ItemTask;
import org.moon.figura.avatars.model.rendertasks.RenderTask;
import org.moon.figura.avatars.model.rendertasks.TextTask;
import org.moon.figura.ducks.LivingEntityRendererAccessor;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaTypeManager;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.MathUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LuaType(typeName = "model_part")
@LuaTypeDoc(
        name = "ModelPart",
        description = "model_part"
)
public class FiguraModelPart {

    private final Avatar owner;
    public final String name;
    public FiguraModelPart parent;

    public final PartCustomization customization;
    public ParentType parentType = ParentType.None;

    private final Map<String, FiguraModelPart> childCache = new HashMap<>();
    public final List<FiguraModelPart> children;

    public List<Integer> facesByTexture;

    public Map<String, RenderTask> renderTasks = new HashMap<>();

    public int textureWidth, textureHeight; //If the part has multiple textures, then these are -1.

    public boolean animated = false;
    public int animationOverride = 0;
    public int lastAnimationPriority = Integer.MIN_VALUE;

    public final FiguraMat4 savedPartToWorldMat = FiguraMat4.of();

    public FiguraModelPart(Avatar owner, String name, PartCustomization customization, List<FiguraModelPart> children) {
        this.owner = owner;
        this.name = name;
        this.customization = customization;
        this.children = children;
        for (FiguraModelPart child : children)
            child.parent = this;
    }

    public void pushVerticesImmediate(ImmediateAvatarRenderer avatarRenderer, int[] remainingComplexity) {
        for (int i = 0; i < facesByTexture.size(); i++) {
            if (remainingComplexity[0] <= 0)
                return;
            remainingComplexity[0] -= facesByTexture.get(i);
            avatarRenderer.pushFaces(i, facesByTexture.get(i) + Math.min(remainingComplexity[0], 0), remainingComplexity);
        }
    }

    public void applyVanillaTransforms(LivingEntityRenderer<?, ?> entityRenderer) {
        if (entityRenderer == null)
            return;

        //get model
        EntityModel<?> vanillaModel;
        if (parentType == ParentType.LeftElytra || parentType == ParentType.RightElytra)
            vanillaModel = ((LivingEntityRendererAccessor<?>) entityRenderer).figura$getElytraModel();
        else
            vanillaModel = entityRenderer.getModel();

        if (vanillaModel == null || parentType.provider == null)
            return;

        ModelPart part = parentType.provider.func.apply(vanillaModel);
        if (part == null)
            return;

        //apply vanilla transforms
        FiguraVec3 defaultPivot = parentType.offset.copy();

        defaultPivot.subtract(part.x, part.y, part.z);

        if ((animationOverride & 4) != 4)
            defaultPivot.multiply(part.xScale, part.yScale, -part.zScale);

        if ((animationOverride & 2) != 2) {
            //customization.offsetPivot(defaultPivot);
            customization.offsetPos(defaultPivot);
        }

        //customization.setBonusPivot(pivot);
        if ((animationOverride & 1) != 1)
            customization.offsetRot(Math.toDegrees(-part.xRot), Math.toDegrees(-part.yRot), Math.toDegrees(part.zRot));

        defaultPivot.free();
    }

    public void resetVanillaTransforms() {
        if (parentType.provider != null) {
            if ((animationOverride & 2) != 2) {
                customization.offsetPivot(0, 0, 0);
                customization.offsetPos(0, 0, 0);
            }
            if ((animationOverride & 1) != 1)
                customization.offsetRot(0, 0, 0);
        }
    }

    public void applyExtraTransforms() {
        if (parentType == ParentType.Camera) {
            if ((animationOverride & 1) != 1) {
                FiguraVec3 vec = MathUtils.quaternionToYXZ(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation()).toDeg();
                customization.offsetRot(vec);
                vec.free();
            }
        }
    }

    public void clean() {
        customization.free();
        for (FiguraModelPart child : children)
            child.clean();
    }

    // -- animations -- //

    public void animPosition(FiguraVec3 vec, boolean merge) {
        if (merge) {
            FiguraVec3 pos = customization.getAnimPos();
            pos.add(vec);
            customization.setAnimPos(pos);
            pos.free();
        } else {
            customization.setAnimPos(vec);
        }
    }
    public void animRotation(FiguraVec3 vec, boolean merge) {
        if (merge) {
            FiguraVec3 rot = customization.getAnimRot();
            rot.add(vec);
            customization.setAnimRot(rot);
            rot.free();
        } else {
            customization.setAnimRot(vec);
        }
    }
    public void animScale(FiguraVec3 vec, boolean merge) {
        if (merge) {
            FiguraVec3 scale = customization.getAnimScale();
            scale.multiply(vec);
            customization.setAnimScale(scale);
            scale.free();
        } else {
            customization.setAnimScale(vec);
        }
    }

    //-- LUA BUSINESS --//

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_name")
    public String getName() {
        return this.name;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_parent")
    public FiguraModelPart getParent() {
        return this.parent;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_children")
    public LuaTable getChildren() {
        LuaTable table = new LuaTable();
        int i = 1;
        for (FiguraModelPart child : this.children)
            table.set(i++, LuaTypeManager.wrap(child));
        return table;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_pos")
    public FiguraVec3 getPos() {
        return this.customization.getPos();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "model_part.set_pos"
    )
    public void setPos(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setPos", x, y, z);
        this.customization.setPos(vec);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_anim_pos")
    public FiguraVec3 getAnimPos() {
        return this.customization.getAnimPos();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_rot")
    public FiguraVec3 getRot() {
        return this.customization.getRot();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rot"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "model_part.set_rot"
    )
    public void setRot(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setRot", x, y, z);
        this.customization.setRot(vec);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_offset_rot")
    public FiguraVec3 getOffsetRot() {
        return this.customization.getOffsetRot();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "offsetRot"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "model_part.offset_rot"
    )
    public void offsetRot(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("offsetRot", x, y, z);
        this.customization.offsetRot(vec);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_anim_rot")
    public FiguraVec3 getAnimRot() {
        return this.customization.getAnimRot();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_scale")
    public FiguraVec3 getScale() {
        return this.customization.getScale();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "scale"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "model_part.set_scale"
    )
    public void setScale(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setScale", x, y, z, 1, 1, 1);
        this.customization.setScale(vec);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_anim_scale")
    public FiguraVec3 getAnimScale() {
        return this.customization.getAnimScale();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_pivot")
    public FiguraVec3 getPivot() {
        return this.customization.getPivot();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pivot"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "model_part.set_pivot"
    )
    public void setPivot(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setPivot", x, y, z);
        this.customization.setPivot(vec);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_offset_pivot")
    public FiguraVec3 getOffsetPivot() {
        return this.customization.getOffsetPivot();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "offsetPivot"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "model_part.offset_pivot"
    )
    public void offsetPivot(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("offsetPivot", x, y, z);
        this.customization.offsetPivot(vec);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_position_matrix")
    public FiguraMat4 getPositionMatrix() {
        this.customization.recalculate();
        return this.customization.getPositionMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_position_matrix_raw")
    public FiguraMat4 getPositionMatrixRaw() {
        return this.customization.getPositionMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_normal_matrix")
    public FiguraMat3 getNormalMatrix() {
        this.customization.recalculate();
        return this.customization.getNormalMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_normal_matrix_raw")
    public FiguraMat3 getNormalMatrixRaw() {
        return this.customization.getNormalMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "matrix"
            ),
            description = "model_part.set_matrix"
    )
    public void setMatrix(@LuaNotNil FiguraMat4 matrix) {
        this.customization.setMatrix(matrix);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_visible")
    public boolean getVisible() {
        FiguraModelPart part = this;
        while (part != null && part.customization.visible == null)
            part = part.parent;
        return part == null || part.customization.visible;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "visible"
            ),
            description = "model_part.set_visible"
    )
    public void setVisible(Boolean bool) {
        this.customization.visible = bool;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.reset_visible")
    public void resetVisible() {
        this.customization.visible = null;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_primary_render_type")
    public String getPrimaryRenderType() {
        RenderTypes renderType = this.customization.getPrimaryRenderType();
        return renderType == null ? null : renderType.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_secondary_render_type")
    public String getSecondaryRenderType() {
        RenderTypes renderType = this.customization.getSecondaryRenderType();
        return renderType == null ? null : renderType.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "renderType"
            ),
            description = "model_part.set_primary_render_type"
    )
    public void setPrimaryRenderType(String type) {
        try {
            this.customization.setPrimaryRenderType(type == null ? null : RenderTypes.valueOf(type));
        } catch (Exception ignored) {
            throw new LuaError("Illegal RenderType: \"" + type + "\".");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "renderType"
            ),
            description = "model_part.set_secondary_render_type"
    )
    public void setSecondaryRenderType(String type) {
        try {
            this.customization.setSecondaryRenderType(type == null ? null : RenderTypes.valueOf(type));
        } catch (Exception ignored) {
            throw new LuaError("Illegal RenderType: \"" + type + "\".");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = String.class,
                            argumentNames = "textureType"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, String.class},
                            argumentNames = {"resource", "path"}
                    )
            },
            description = "model_part.set_primary_texture"
    )
    public void setPrimaryTexture(String type, String path) {
        this.customization.primaryTexture = FiguraTextureSet.getOverrideTexture(this.owner, type, path);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = String.class,
                            argumentNames = "textureType"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, String.class},
                            argumentNames = {"resource", "path"}
                    )
            },
            description = "model_part.set_secondary_texture"
    )
    public void setSecondaryTexture(String type, String path) {
        this.customization.secondaryTexture = FiguraTextureSet.getOverrideTexture(this.owner, type, path);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.part_to_world_matrix")
    public FiguraMat4 partToWorldMatrix() {
        return this.savedPartToWorldMat.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_texture_size")
    public FiguraVec2 getTextureSize() {
        if (this.textureWidth == -1 || this.textureHeight == -1)
            throw new LuaError("Cannot get texture size of part, it has multiple different-sized textures!");
        return FiguraVec2.of(this.textureWidth, this.textureHeight);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "uv"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"u", "v"}
                    )
            },
            description = "model_part.set_uv")
    public void setUV(Object x, Double y) {
        this.customization.uvMatrix.reset();
        FiguraVec2 uv = LuaUtils.parseVec2("setUV", x, y);
        this.customization.uvMatrix.translate(uv.x, uv.y);
        uv.free();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "uv"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"u", "v"}
                    )
            },
            description = "model_part.set_uv_pixels")
    public void setUVPixels(Object x, Double y) {
        if (this.textureWidth == -1 || this.textureHeight == -1)
            throw new LuaError("Cannot call setUVPixels on a part with multiple texture sizes!");

        this.customization.uvMatrix.reset();
        FiguraVec2 uv = LuaUtils.parseVec2("setUVPixels", x, y);
        uv.divide(this.textureWidth, this.textureHeight);
        this.customization.uvMatrix.translate(uv.x, uv.y);
        uv.free();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "matrix"
            ),
            description = "model_part.set_uv_matrix")
    public void setUVMatrix(@LuaNotNil FiguraMat3 matrix) {
        this.customization.uvMatrix.set(matrix);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            description = "model_part.set_color")
    public void setColor(Object r, Double g, Double b) {
        this.customization.color = LuaUtils.parseVec3("setColor", r, g, b, 1, 1, 1);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_color")
    public FiguraVec3 getColor() {
        return this.customization.color.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Float.class,
                    argumentNames = "opacity"
            ),
            description = "model_part.set_opacity")
    public void setOpacity(Float opacity) {
        this.customization.alpha = opacity;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_opacity")
    public Float getOpacity() {
        return this.customization.alpha;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "light"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"blockLight", "skyLight"}
                    )
            },
            description = "model_part.set_light")
    public void setLight(Object light, Double skyLight) {
        if (light == null) {
            this.customization.light = null;
            return;
        }

        FiguraVec2 lightVec = LuaUtils.parseVec2("setLight", light, skyLight);
        this.customization.light = LightTexture.pack((int) lightVec.x, (int) lightVec.y);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_light")
    public FiguraVec2 getLight() {
        Integer light = this.customization.light;
        return light == null ? null : FiguraVec2.of(LightTexture.block(light), LightTexture.sky(light));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "parentType"
            ),
            description = "model_part.set_parent_type")
    public void setParentType(@LuaNotNil String parent) {
        this.parentType = ParentType.get(parent);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_parent_type")
    public String getParentType() {
        return this.parentType == null ? null : this.parentType.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "model_part.get_type")
    public String getType() {
        return this.customization.partType.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "taskName"
            ),
            description = "model_part.add_text")
    public RenderTask addText(@LuaNotNil String name) {
        RenderTask task = new TextTask();
        this.renderTasks.put(name, task);
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "taskName"
            ),
            description = "model_part.add_item")
    public RenderTask addItem(@LuaNotNil String name) {
        RenderTask task = new ItemTask();
        this.renderTasks.put(name, task);
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "taskName"
            ),
            description = "model_part.add_block")
    public RenderTask addBlock(@LuaNotNil String name) {
        RenderTask task = new BlockTask();
        this.renderTasks.put(name, task);
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "taskName"
            ),
            description = "model_part.get_task")
    public RenderTask getTask(@LuaNotNil String name) {
        return this.renderTasks.get(name);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "taskName"
            ),
            description = "model_part.remove_task")
    public void removeTask(@LuaNotNil String name) {
        this.renderTasks.remove(name);
    }

    //-- METAMETHODS --//
    @LuaWhitelist
    public Object __index(@LuaNotNil String key) {
        if (this.childCache.containsKey(key))
            return this.childCache.get(key);

        for (FiguraModelPart child : this.children)
            if (child.name.equals(key)) {
                this.childCache.put(key, child);
                return child;
            }

        this.childCache.put(key, null);
        return null;
    }

    @Override
    public String toString() {
        return name + " (ModelPart)";
    }
}
