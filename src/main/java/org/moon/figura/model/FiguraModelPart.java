package org.moon.figura.model;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.luaj.vm2.LuaError;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.model.rendering.ImmediateAvatarRenderer;
import org.moon.figura.model.rendering.Vertex;
import org.moon.figura.model.rendering.texture.FiguraTexture;
import org.moon.figura.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.model.rendering.texture.RenderTypes;
import org.moon.figura.model.rendertasks.*;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LuaWhitelist
@LuaTypeDoc(
        name = "ModelPart",
        value = "model_part"
)
public class FiguraModelPart implements Comparable<FiguraModelPart> {

    private final Avatar owner;

    public final String name;
    public FiguraModelPart parent;

    public final PartCustomization customization;
    public PartCustomization savedCustomization;
    public ParentType parentType = ParentType.None;

    private final Map<String, FiguraModelPart> childCache = new HashMap<>();
    public final List<FiguraModelPart> children;

    public List<Integer> facesByTexture;

    public Map<String, RenderTask> renderTasks = new HashMap<>();

    public List<FiguraTextureSet> textures;
    public int textureWidth, textureHeight; //If the part has multiple textures, then these are -1.

    public boolean animated = false;
    public int animationOverride = 0;
    public int lastAnimationPriority = Integer.MIN_VALUE;

    public final FiguraMat4 savedPartToWorldMat = FiguraMat4.of().scale(1 / 16d, 1 / 16d, 1 / 16d);

    public final Map<Integer, List<Vertex>> vertices;

    public FiguraModelPart(Avatar owner, String name, PartCustomization customization, Map<Integer, List<Vertex>> vertices, List<FiguraModelPart> children) {
        this.owner = owner;
        this.name = name;
        this.customization = customization;
        this.vertices = vertices;
        this.children = children;
        for (FiguraModelPart child : children)
            child.parent = this;
    }

    public boolean pushVerticesImmediate(ImmediateAvatarRenderer avatarRenderer, int[] remainingComplexity) {
        for (int i = 0; i < facesByTexture.size(); i++) {
            if (remainingComplexity[0] <= 0)
                return false;
            remainingComplexity[0] -= facesByTexture.get(i);
            avatarRenderer.pushFaces(facesByTexture.get(i) + Math.min(remainingComplexity[0], 0), remainingComplexity, textures.get(i), vertices.get(i));
        }
        return true;
    }

    private Map<Integer, List<Vertex>> copyVertices() {
        Map<Integer, List<Vertex>> map = new HashMap<>();
        for (Map.Entry<Integer, List<Vertex>> entry : vertices.entrySet()) {
            List<Vertex> list = new ArrayList<>();
            for (Vertex vertex : entry.getValue())
                list.add(vertex.copy());
            map.put(entry.getKey(), list);
        }
        return map;
    }

    public void applyVanillaTransforms(VanillaModelData vanillaModelData) {
        if (vanillaModelData == null)
            return;

        //get part data
        VanillaModelData.PartData partData = vanillaModelData.partMap.get(this.parentType);
        if (partData == null)
            return;

        //apply vanilla transforms
        customization.vanillaVisible = partData.visible;

        FiguraVec3 defaultPivot = parentType.offset.copy();

        defaultPivot.subtract(partData.pos);

        if (!overrideVanillaScale()) {
            defaultPivot.multiply(partData.scale);
            customization.offsetScale(partData.scale);
        }

        if (!overrideVanillaPos()) {
            customization.offsetPivot(defaultPivot);
            customization.offsetPos(defaultPivot);
        }

        //customization.offsetPivot(pivot);
        if (!overrideVanillaRot())
            customization.offsetRot(partData.rot);
    }

    public void resetVanillaTransforms() {
        if (parentType.provider != null) {
            if (!overrideVanillaPos()) {
                customization.offsetPivot(0, 0, 0);
                customization.offsetPos(0, 0, 0);
            }
            if (!overrideVanillaRot())
                customization.offsetRot(0, 0, 0);
            if (!overrideVanillaScale())
                customization.offsetScale(1, 1, 1);

            customization.vanillaVisible = null;
        }
    }

    public void applyExtraTransforms(PartCustomization currentTransforms) {
        if (parentType != ParentType.Camera)
            return;

        FiguraMat4 prevPartToView = currentTransforms.positionMatrix.inverted();
        double s = 1 / 16d;
        if (UIHelper.paperdoll) {
            s *= -UIHelper.dollScale;
        } else {
            prevPartToView.rightMultiply(FiguraMat4.of().rotateY(180));
        }
        FiguraVec3 scale = currentTransforms.stackScale.scaled(s);
        FiguraVec3 piv = customization.getPivot();
        FiguraVec3 piv2 = customization.getOffsetPivot().add(piv);
        prevPartToView.scale(scale);
        prevPartToView.v14 = prevPartToView.v24 = prevPartToView.v34 = 0;
        prevPartToView.translateFirst(-piv2.x, -piv2.y, -piv2.z);
        prevPartToView.translate(piv2.x, piv2.y, piv2.z);
        customization.setMatrix(prevPartToView);
    }

    // -- animations -- //

    public void animPosition(FiguraVec3 vec, boolean merge) {
        if (merge) {
            FiguraVec3 pos = customization.getAnimPos();
            pos.add(-vec.x, vec.y, vec.z);
            customization.setAnimPos(pos.x, pos.y, pos.z);
        } else {
            customization.setAnimPos(-vec.x, vec.y, vec.z);
        }
    }
    public void animRotation(FiguraVec3 vec, boolean merge) {
        if (merge) {
            FiguraVec3 rot = customization.getAnimRot();
            rot.add(-vec.x, -vec.y, vec.z);
            customization.setAnimRot(rot.x, rot.y, rot.z);
        } else {
            customization.setAnimRot(-vec.x, -vec.y, vec.z);
        }
    }
    public void globalAnimRot(FiguraVec3 vec, boolean merge) {
        /*FiguraModelPart part = parent;
        while (part != null) {
            FiguraVec3 rot = part.getAnimRot();
            vec.subtract(rot);
            part = part.parent;
        }*/
        animRotation(vec, merge);
    }
    public void animScale(FiguraVec3 vec, boolean merge) {
        if (merge) {
            FiguraVec3 scale = customization.getAnimScale();
            scale.multiply(vec);
            customization.setAnimScale(scale.x, scale.y, scale.z);
        } else {
            customization.setAnimScale(vec.x, vec.y, vec.z);
        }
    }

    //-- LUA BUSINESS --//

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_name")
    public String getName() {
        return this.name;
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_parent")
    public FiguraModelPart getParent() {
        return this.parent;
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_children")
    public Map<Integer, FiguraModelPart> getChildren() {
        Map<Integer, FiguraModelPart> map = new HashMap<>();
        for (int i = 0; i < this.children.size(); i++)
            map.put(i + 1, this.children.get(i));
        return map;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "part"
            ),
            value = "model_part.is_child_of"
    )
    public boolean isChildOf(@LuaNotNil FiguraModelPart part) {
        FiguraModelPart p = parent;
        while (p != null) {
            if (p == part)
                return true;
            p = p.parent;
        }

        return false;
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_pos")
    public FiguraVec3 getPos() {
        return this.customization.getPos();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "pos",
            value = "model_part.set_pos"
    )
    public FiguraModelPart setPos(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setPos", x, y, z);
        this.customization.setPos(vec);
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart pos(Object x, Double y, Double z) {
        return setPos(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_anim_pos")
    public FiguraVec3 getAnimPos() {
        return this.customization.getAnimPos();
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_true_pos")
    public FiguraVec3 getTruePos() {
        return this.getPos().add(this.getAnimPos());
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_rot")
    public FiguraVec3 getRot() {
        return this.customization.getRot();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "rot",
            value = "model_part.set_rot"
    )
    public FiguraModelPart setRot(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setRot", x, y, z);
        this.customization.setRot(vec);
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart rot(Object x, Double y, Double z) {
        return setRot(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_offset_rot")
    public FiguraVec3 getOffsetRot() {
        return this.customization.getOffsetRot();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "offsetRot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "offsetRot",
            value = "model_part.set_offset_rot"
    )
    public FiguraModelPart setOffsetRot(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setOffsetRot", x, y, z);
        this.customization.offsetRot(vec);
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart offsetRot(Object x, Double y, Double z) {
        return setOffsetRot(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_anim_rot")
    public FiguraVec3 getAnimRot() {
        return this.customization.getAnimRot();
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_true_rot")
    public FiguraVec3 getTrueRot() {
        return this.getRot().add(this.getOffsetRot()).add(this.getAnimRot());
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_scale")
    public FiguraVec3 getScale() {
        return this.customization.getScale();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "scale"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "scale",
            value = "model_part.set_scale"
    )
    public FiguraModelPart setScale(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setScale", x, y, z, 1, 1, 1);
        this.customization.setScale(vec);
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart scale(Object x, Double y, Double z) {
        return setScale(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_offset_scale")
    public FiguraVec3 getOffsetScale() {
        return this.customization.getOffsetScale();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "offsetScale"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "offsetScale",
            value = "model_part.set_offset_scale"
    )
    public FiguraModelPart setOffsetScale(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setOffsetScale", x, y, z, 1, 1, 1);
        this.customization.offsetScale(vec);
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart offsetScale(Object x, Double y, Double z) {
        return setOffsetScale(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_anim_scale")
    public FiguraVec3 getAnimScale() {
        return this.customization.getAnimScale();
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_true_scale")
    public FiguraVec3 getTrueScale() {
        return this.getScale().multiply(this.getOffsetScale()).multiply(this.getAnimScale());
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_pivot")
    public FiguraVec3 getPivot() {
        return this.customization.getPivot();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pivot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "pivot",
            value = "model_part.set_pivot"
    )
    public FiguraModelPart setPivot(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setPivot", x, y, z);
        this.customization.setPivot(vec);
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart pivot(Object x, Double y, Double z) {
        return setPivot(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_offset_pivot")
    public FiguraVec3 getOffsetPivot() {
        return this.customization.getOffsetPivot();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "offsetPivot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "offsetPivot",
            value = "model_part.set_offset_pivot"
    )
    public FiguraModelPart setOffsetPivot(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setOffsetPivot", x, y, z);
        this.customization.offsetPivot(vec);
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart offsetPivot(Object x, Double y, Double z) {
        return setOffsetPivot(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_true_pivot")
    public FiguraVec3 getTruePivot() {
        return this.getPivot().add(this.getOffsetPivot());
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_position_matrix")
    public FiguraMat4 getPositionMatrix() {
        this.customization.recalculate();
        return this.customization.getPositionMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_position_matrix_raw")
    public FiguraMat4 getPositionMatrixRaw() {
        return this.customization.getPositionMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_normal_matrix")
    public FiguraMat3 getNormalMatrix() {
        this.customization.recalculate();
        return this.customization.getNormalMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_normal_matrix_raw")
    public FiguraMat3 getNormalMatrixRaw() {
        return this.customization.getNormalMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "matrix"
            ),
            aliases = "matrix",
            value = "model_part.set_matrix"
    )
    public FiguraModelPart setMatrix(@LuaNotNil FiguraMat4 matrix) {
        this.customization.setMatrix(matrix);
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart matrix(@LuaNotNil FiguraMat4 matrix) {
        return setMatrix(matrix);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_visible")
    public boolean getVisible() {
        FiguraModelPart part = this;
        while (part != null && part.customization.visible == null)
            part = part.parent;
        return part == null || part.customization.visible;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "visible"
            ),
            aliases = "visible",
            value = "model_part.set_visible"
    )
    public FiguraModelPart setVisible(Boolean bool) {
        this.customization.visible = bool;
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart visible(Boolean bool) {
        return setVisible(bool);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_primary_render_type")
    public String getPrimaryRenderType() {
        RenderTypes renderType = this.customization.getPrimaryRenderType();
        return renderType == null ? null : renderType.name();
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_secondary_render_type")
    public String getSecondaryRenderType() {
        RenderTypes renderType = this.customization.getSecondaryRenderType();
        return renderType == null ? null : renderType.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "renderType"
            ),
            aliases = "primaryRenderType",
            value = "model_part.set_primary_render_type"
    )
    public FiguraModelPart setPrimaryRenderType(String type) {
        try {
            this.customization.setPrimaryRenderType(type == null ? null : RenderTypes.valueOf(type.toUpperCase()));
            return this;
        } catch (Exception ignored) {
            throw new LuaError("Illegal RenderType: \"" + type + "\".");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "renderType"
            ),
            aliases = "secondaryRenderType",
            value = "model_part.set_secondary_render_type"
    )
    public FiguraModelPart setSecondaryRenderType(String type) {
        try {
            this.customization.setSecondaryRenderType(type == null ? null : RenderTypes.valueOf(type.toUpperCase()));
            return this;
        } catch (Exception ignored) {
            throw new LuaError("Illegal RenderType: \"" + type + "\".");
        }
    }

    @LuaWhitelist
    public FiguraModelPart primaryRenderType(String type) {
        return setPrimaryRenderType(type);
    }

    @LuaWhitelist
    public FiguraModelPart secondaryRenderType(String type) {
        return setSecondaryRenderType(type);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "textureType"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class},
                            argumentNames = {"resource", "path"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, FiguraTexture.class},
                            argumentNames = {"custom", "texture"}
                    )
            },
            aliases = "primaryTexture",
            value = "model_part.set_primary_texture"
    )
    public FiguraModelPart setPrimaryTexture(String type, Object x) {
        try {
            this.customization.primaryTexture = type == null ? null : Pair.of(FiguraTextureSet.OverrideType.valueOf(type.toUpperCase()), x);
            return this;
        } catch (Exception ignored) {
            throw new LuaError("Invalid texture override type: " + type);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "textureType"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class},
                            argumentNames = {"resource", "path"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, FiguraTexture.class},
                            argumentNames = {"custom", "texture"}
                    )
            },
            aliases = "secondaryTexture",
            value = "model_part.set_secondary_texture"
    )
    public FiguraModelPart setSecondaryTexture(String type, Object x) {
        try {
            this.customization.secondaryTexture = type == null ? null : Pair.of(FiguraTextureSet.OverrideType.valueOf(type.toUpperCase()), x);
            return this;
        } catch (Exception ignored) {
            throw new LuaError("Invalid texture override type: " + type);
        }
    }

    @LuaWhitelist
    public FiguraModelPart primaryTexture(String type, Object x) {
        return setPrimaryTexture(type, x);
    }

    @LuaWhitelist
    public FiguraModelPart secondaryTexture(String type, Object x) {
        return setSecondaryTexture(type, x);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_textures")
    public List<FiguraTexture> getTextures() {
        List<FiguraTexture> list = new ArrayList<>();

        for (FiguraTextureSet set : textures) {
            for (FiguraTexture texture : set.textures) {
                if (texture != null)
                    list.add(texture);
            }
        }

        return list;
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.part_to_world_matrix")
    public FiguraMat4 partToWorldMatrix() {
        return this.savedPartToWorldMat.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_texture_size")
    public FiguraVec2 getTextureSize() {
        if (this.textureWidth == -1 || this.textureHeight == -1) {
            if (this.customization.partType == PartCustomization.PartType.GROUP)
                throw new LuaError("Cannot get the texture size of groups!");
            else
                throw new LuaError("Cannot get texture size of part, it has multiple different-sized textures!");
        }

        return FiguraVec2.of(this.textureWidth, this.textureHeight);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "uv"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"u", "v"}
                    )
            },
            aliases = "uv",
            value = "model_part.set_uv")
    public FiguraModelPart setUV(Object x, Double y) {
        this.customization.uvMatrix.reset();
        FiguraVec2 uv = LuaUtils.parseVec2("setUV", x, y);
        this.customization.uvMatrix.translate(uv.x % 1, uv.y % 1);
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart uv(Object x, Double y) {
        return setUV(x, y);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_uv")
    public FiguraVec2 getUV() {
        return this.customization.uvMatrix.apply(0d, 0d);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "uv"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"u", "v"}
                    )
            },
            aliases = "uvPixels",
            value = "model_part.set_uv_pixels")
    public FiguraModelPart setUVPixels(Object x, Double y) {
        if (this.textureWidth == -1 || this.textureHeight == -1) {
            if (this.customization.partType == PartCustomization.PartType.GROUP) {
                for (FiguraModelPart child : children)
                    child.setUVPixels(x, y);
                return this;
            } else
                throw new LuaError("Cannot call setUVPixels on parts with multiple texture sizes!");
        }

        this.customization.uvMatrix.reset();
        FiguraVec2 uv = LuaUtils.parseVec2("setUVPixels", x, y);
        uv.divide(this.textureWidth, this.textureHeight);
        this.customization.uvMatrix.translate(uv.x, uv.y);

        return this;
    }

    @LuaWhitelist
    public FiguraModelPart uvPixels(Object x, Double y) {
        return setUVPixels(x, y);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_uv_pixels")
    public FiguraVec2 getUVPixels() {
        if (this.textureWidth == -1 || this.textureHeight == -1) {
            if (this.customization.partType == PartCustomization.PartType.GROUP)
                throw new LuaError("Cannot call getUVPixels on groups!");
            else
                throw new LuaError("Cannot call getUVPixels on parts with multiple texture sizes!");
        }

        return getUV().multiply(this.textureWidth, this.textureHeight);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "matrix"
            ),
            aliases = "uvMatrix",
            value = "model_part.set_uv_matrix")
    public FiguraModelPart setUVMatrix(@LuaNotNil FiguraMat3 matrix) {
        this.customization.uvMatrix.set(matrix);
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart uvMatrix(@LuaNotNil FiguraMat3 matrix) {
        return setUVMatrix(matrix);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_uv_matrix")
    public FiguraMat3 getUVMatrix() {
        return this.customization.uvMatrix;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            aliases = "color",
            value = "model_part.set_color")
    public FiguraModelPart setColor(Object r, Double g, Double b) {
        FiguraVec3 vec = LuaUtils.parseVec3("setColor", r, g, b, 1, 1, 1);
        this.customization.color.set(vec);
        this.customization.color2.set(vec);
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart color(Object r, Double g, Double b) {
        return setColor(r, g, b);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_color")
    public FiguraVec3 getColor() {
        return getPrimaryColor().add(getSecondaryColor()).scale(0.5);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            aliases = "primaryColor",
            value = "model_part.set_primary_color")
    public FiguraModelPart setPrimaryColor(Object r, Double g, Double b) {
        this.customization.color.set(LuaUtils.parseVec3("setPrimaryColor", r, g, b, 1, 1, 1));
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart primaryColor(Object r, Double g, Double b) {
        return setPrimaryColor(r, g, b);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_primary_color")
    public FiguraVec3 getPrimaryColor() {
        return this.customization.color.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            aliases = "secondaryColor",
            value = "model_part.set_secondary_color")
    public FiguraModelPart setSecondaryColor(Object r, Double g, Double b) {
        this.customization.color2.set(LuaUtils.parseVec3("setSecondaryColor", r, g, b, 1, 1, 1));
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart secondaryColor(Object r, Double g, Double b) {
        return setSecondaryColor(r, g, b);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_secondary_color")
    public FiguraVec3 getSecondaryColor() {
        return this.customization.color2.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "opacity"
            ),
            aliases = "opacity",
            value = "model_part.set_opacity")
    public FiguraModelPart setOpacity(Float opacity) {
        this.customization.alpha = opacity;
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart opacity(Float opacity) {
        return setOpacity(opacity);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_opacity")
    public Float getOpacity() {
        return this.customization.alpha;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "light"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"blockLight", "skyLight"}
                    )
            },
            aliases = "light",
            value = "model_part.set_light")
    public FiguraModelPart setLight(Object light, Double skyLight) {
        if (light == null) {
            this.customization.light = null;
            return this;
        }

        FiguraVec2 lightVec = LuaUtils.parseVec2("setLight", light, skyLight);
        this.customization.light = LightTexture.pack((int) lightVec.x, (int) lightVec.y);
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart light(Object light, Double skyLight) {
        return setLight(light, skyLight);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_light")
    public FiguraVec2 getLight() {
        Integer light = this.customization.light;
        return light == null ? null : FiguraVec2.of(LightTexture.block(light), LightTexture.sky(light));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "overlay"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"whiteOverlay", "hurtOverlay"}
                    )
            },
            aliases = "overlay",
            value = "model_part.set_overlay")
    public FiguraModelPart setOverlay(Object whiteOverlay, Double hurtOverlay) {
        if (whiteOverlay == null) {
            this.customization.overlay = null;
            return this;
        }

        FiguraVec2 overlayVec = LuaUtils.parseVec2("setOverlay", whiteOverlay, hurtOverlay);
        this.customization.overlay = OverlayTexture.pack((int) overlayVec.x, (int) overlayVec.y);
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart overlay(Object whiteOverlay, Double hurtOverlay) {
        return setOverlay(whiteOverlay, hurtOverlay);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_overlay")
    public FiguraVec2 getOverlay() {
        Integer overlay = this.customization.overlay;
        return overlay == null ? null : FiguraVec2.of(overlay & 0xFFFF, overlay >> 16);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "parentType"
            ),
            aliases = "parentType",
            value = "model_part.set_parent_type")
    public FiguraModelPart setParentType(String parent) {
        ParentType oldParent = this.parentType;
        this.parentType = ParentType.get(parent);

        if ((oldParent.isSeparate || this.parentType.isSeparate) && oldParent != this.parentType)
            owner.renderer.sortParts();

        this.customization.vanillaVisible = null;
        this.customization.needsMatrixRecalculation = true;
        return this;
    }

    @LuaWhitelist
    public FiguraModelPart parentType(String parent) {
        return setParentType(parent);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_parent_type")
    public String getParentType() {
        return this.parentType == null ? null : this.parentType.name();
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_type")
    public String getType() {
        return this.customization.partType.name();
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.override_vanilla_rot")
    public boolean overrideVanillaRot() {
        return (animationOverride & 1) == 1;
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.override_vanilla_pos")
    public boolean overrideVanillaPos() {
        return (animationOverride & 2) == 2;
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.override_vanilla_scale")
    public boolean overrideVanillaScale() {
        return (animationOverride & 4) == 4;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "taskName"
            ),
            value = "model_part.new_text")
    public TextTask newText(@LuaNotNil String name) {
        TextTask task = new TextTask(name, owner);
        this.renderTasks.put(name, task);
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "taskName"
            ),
            value = "model_part.new_item")
    public ItemTask newItem(@LuaNotNil String name) {
        ItemTask task = new ItemTask(name, owner);
        this.renderTasks.put(name, task);
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "taskName"
            ),
            value = "model_part.new_block")
    public BlockTask newBlock(@LuaNotNil String name) {
        BlockTask task = new BlockTask(name, owner);
        this.renderTasks.put(name, task);
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "taskName"
            ),
            value = "model_part.new_sprite")
    public SpriteTask newSprite(@LuaNotNil String name) {
        SpriteTask task = new SpriteTask(name, owner);
        this.renderTasks.put(name, task);
        return task;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(returnType = Map.class),
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "taskName",
                            returnType = RenderTask.class
                    )
            },
            value = "model_part.get_task")
    public Object getTask(String name) {
        if (name != null)
            return this.renderTasks.get(name);
        else
            return this.renderTasks;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "taskName"
                    )
            },
            value = "model_part.remove_task")
    public FiguraModelPart removeTask(String name) {
        if (name != null)
            this.renderTasks.remove(name);
        else
            this.renderTasks.clear();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "textureID"
            ),
            value = "model_part.get_vertices"
    )
    public List<Vertex> getVertices(@LuaNotNil String textureID) {
        int index = -1;
        for (int i = 0; i < textures.size(); i++) {
            if (textureID.equals(textures.get(i).name)) {
                index = i;
                break;
            }
        }
        return vertices.get(index);
    }

    @LuaWhitelist
    @LuaMethodDoc("model_part.get_all_vertices")
    public Map<String, List<Vertex>> getAllVertices() {
        Map<String, List<Vertex>> map = new HashMap<>();
        for (int i = 0; i < textures.size(); i++) {
            List<Vertex> list = vertices.get(i);
            if (list != null) map.put(textures.get(i).name, list);
        }
        return map;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "part"
            ),
            value = "model_part.move_to"
    )
    public FiguraModelPart moveTo(@LuaNotNil FiguraModelPart part) {
        parent.children.remove(this);
        part.children.add(this);
        this.parent = part;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "part"
            ),
            value = "model_part.add_child"
    )
    public FiguraModelPart addChild(@LuaNotNil FiguraModelPart part) {
        FiguraModelPart parent = this.parent;
        while (parent != null) {
            if (part == parent)
                throw new LuaError("Cannot add child that's already parent of this part");
            parent = parent.parent;
        }

        this.children.add(part);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "part"
            ),
            value = "model_part.remove_child"
    )
    public FiguraModelPart removeChild(@LuaNotNil FiguraModelPart part) {
        this.children.remove(part);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "name"
            ),
            value = "model_part.copy"
    )
    public FiguraModelPart copy(@LuaNotNil String name) {
        PartCustomization customization = new PartCustomization();
        this.customization.copyTo(customization);
        FiguraModelPart result = new FiguraModelPart(owner, name, customization, copyVertices(), new ArrayList<>(children));
        result.facesByTexture = new ArrayList<>(facesByTexture);
        result.textures = new ArrayList<>(textures);
        result.parentType = parentType;
        result.textureHeight = textureHeight;
        result.textureWidth = textureWidth;
        return result;
    }

    //-- METAMETHODS --//
    @LuaWhitelist
    public Object __index(String key) {
        if (key == null) return null;

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
    public int compareTo(FiguraModelPart o) {
        if (this.isChildOf(o))
            return 1;
        else if (o.isChildOf(this))
            return -1;
        else
            return 0;
    }

    @Override
    public String toString() {
        return name + " (ModelPart)";
    }
}
