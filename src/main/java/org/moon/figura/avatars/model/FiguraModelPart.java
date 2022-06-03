package org.moon.figura.avatars.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.rendering.FiguraImmediateBuffer;
import org.moon.figura.avatars.model.rendering.ImmediateAvatarRenderer;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.avatars.vanilla.VanillaPartOffsetManager;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaTable;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.ducks.PlayerModelAccessor;
import org.moon.figura.mixin.render.layers.elytra.ElytraModelAccessor;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.*;

@LuaWhitelist
@LuaTypeDoc(
        name = "ModelPart",
        description = "model_part"
)
public class FiguraModelPart {

    private final Avatar owner;

    @LuaWhitelist
    @LuaFieldDoc(description = "model_part.name")
    public final String name;
    public FiguraModelPart parent;

    public final PartCustomization customization;
    public ParentType parentType = ParentType.None;
    public final int index;

    private final Map<String, FiguraModelPart> childCache = new HashMap<>();
    public final List<FiguraModelPart> children;

    public List<Integer> facesByTexture;

    public int textureWidth, textureHeight; //If the part has multiple textures, then these are -1.

    public void pushVerticesImmediate(ImmediateAvatarRenderer avatarRenderer, int[] remainingComplexity) {
        for (int i = 0; i < facesByTexture.size(); i++) {
            if (remainingComplexity[0] <= 0)
                return;
            remainingComplexity[0] -= facesByTexture.get(i);
            avatarRenderer.pushFaces(i, facesByTexture.get(i) + Math.min(remainingComplexity[0], 0), remainingComplexity);
        }
    }

    public void applyVanillaTransforms(EntityModel<?> vanillaModel) {
        if (!parentType.vanilla) return;
        if (vanillaModel instanceof PlayerModel<?> player) {
            applyVanillaTransform(vanillaModel, parentType, switch (parentType) {
                case Cape -> ((PlayerModelAccessor) player).figura$getFakeCloak();
                default -> null;
            });
        }
        if (vanillaModel instanceof HumanoidModel<?> humanoid) {
            applyVanillaTransform(vanillaModel, parentType, switch (parentType) {
                case Head -> humanoid.head;
                case Body -> humanoid.body;
                case LeftArm -> humanoid.leftArm;
                case RightArm -> humanoid.rightArm;
                case LeftLeg -> humanoid.leftLeg;
                case RightLeg -> humanoid.rightLeg;
                default -> null;
            });
        }
        if (vanillaModel instanceof ElytraModel<?> elytra) {
            applyVanillaTransform(vanillaModel, parentType, switch (parentType) {
                case LeftElytra -> ((ElytraModelAccessor) elytra).getLeftWing();
                case RightElytra -> ((ElytraModelAccessor) elytra).getRightWing();
                default -> null;
            });
        }
    }

    @Override
    public String toString() {
        return name + " (ModelPart)";
    }

    public void applyVanillaTransform(EntityModel<?> vanillaModel, ParentType parentType, ModelPart part) {
        if (part == null)
            return;

        FiguraVec3 defaultPivot = VanillaPartOffsetManager.getVanillaOffset(vanillaModel, parentType);
        defaultPivot.subtract(part.x, part.y, part.z);
        defaultPivot.multiply(1, 1, -1);

        customization.setBonusPivot(defaultPivot);
        customization.setBonusPos(defaultPivot);

        //customization.setBonusPivot(pivot);
        customization.setBonusRot(Math.toDegrees(-part.xRot), Math.toDegrees(-part.yRot), Math.toDegrees(part.zRot));

        defaultPivot.free();
    }

    public void resetVanillaTransforms() {
        if (parentType.vanilla) {
            customization.setBonusPivot(0, 0, 0);
            customization.setBonusPos(0, 0, 0);
            customization.setBonusRot(0, 0, 0);
        }
    }

    public void clean() {
        customization.free();
        for (FiguraModelPart child : children)
            child.clean();
    }

    public enum ParentType {
        None(false, "NONE"),

        Head("HEAD"),
        Body("BODY"),
        LeftArm("LEFT_ARM"),
        RightArm("RIGHT_ARM"),
        LeftLeg("LEFT_LEG"),
        RightLeg("RIGHT_LEG"),

        LeftElytra("LeftElytron, LEFT_ELYTRON, LEFT_ELYTRA"),
        RightElytra("RightElytron, RIGHT_ELYTRON, RIGHT_ELYTRA"),

        Cape("CAPE"),

        World(false, "WORLD");

        public final boolean vanilla;
        public final String[] aliases;

        ParentType(String... aliases) {
            this(true, aliases);
        }

        ParentType(Boolean vanilla, String... aliases) {
            this.vanilla = vanilla;
            this.aliases = aliases;
        }

        public static ParentType get(String name) {
            for (ParentType parentType : values()) {
                if (parentType.name().startsWith(name))
                    return parentType;
                for (String alias : parentType.aliases) {
                    if (alias.startsWith(name))
                        return parentType;
                }
            }
            return None;
        }
    }

    //-- LUA BUSINESS --//

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_parent"
    )
    public static FiguraModelPart getParent(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.parent;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_children"
    )
    public static LuaTable getChildren(@LuaNotNil FiguraModelPart modelPart) {
        LuaTable table = new LuaTable();
        int i = 1;
        for (FiguraModelPart child : modelPart.children)
            table.put(i++, child);
        return table;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_pos"
    )
    public static FiguraVec3 getPos(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.customization.getPos();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, FiguraVec3.class},
                            argumentNames = {"modelPart", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, Double.class, Double.class, Double.class},
                            argumentNames = {"modelPart", "x", "y", "z"}
                    )
            },
            description = "model_part.set_pos"
    )
    public static void setPos(@LuaNotNil FiguraModelPart modelPart, Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setPos", x, y, z);
        modelPart.customization.setPos(vec);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_rot"
    )
    public static FiguraVec3 getRot(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.customization.getRot();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, FiguraVec3.class},
                            argumentNames = {"modelPart", "rot"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, Double.class, Double.class, Double.class},
                            argumentNames = {"modelPart", "x", "y", "z"}
                    )
            },
            description = "model_part.set_rot"
    )
    public static void setRot(@LuaNotNil FiguraModelPart modelPart, Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setRot", x, y, z);
        modelPart.customization.setRot(vec);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_bonus_rot"
    )
    public static FiguraVec3 getBonusRot(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.customization.getBonusRot();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, FiguraVec3.class},
                            argumentNames = {"modelPart", "bonusRot"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, Double.class, Double.class, Double.class},
                            argumentNames = {"modelPart", "x", "y", "z"}
                    )
            },
            description = "model_part.set_bonus_rot"
    )
    public static void setBonusRot(@LuaNotNil FiguraModelPart modelPart, Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setBonusRot", x, y, z);
        modelPart.customization.setBonusRot(vec);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_scale"
    )
    public static FiguraVec3 getScale(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.customization.getScale();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, FiguraVec3.class},
                            argumentNames = {"modelPart", "scale"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, Double.class, Double.class, Double.class},
                            argumentNames = {"modelPart", "x", "y", "z"}
                    )
            },
            description = "model_part.set_scale"
    )
    public static void setScale(@LuaNotNil FiguraModelPart modelPart, Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setScale", x, y, z, 1, 1, 1);
        modelPart.customization.setScale(vec);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_pivot"
    )
    public static FiguraVec3 getPivot(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.customization.getPivot();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, FiguraVec3.class},
                            argumentNames = {"modelPart", "pivot"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, Double.class, Double.class, Double.class},
                            argumentNames = {"modelPart", "x", "y", "z"}
                    )
            },
            description = "model_part.set_pivot"
    )
    public static void setPivot(@LuaNotNil FiguraModelPart modelPart, Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setPivot", x, y, z);
        modelPart.customization.setPivot(vec);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_bonus_pivot"
    )
    public static FiguraVec3 getBonusPivot(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.customization.getBonusPivot();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, FiguraVec3.class},
                            argumentNames = {"modelPart", "bonusPivot"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, Double.class, Double.class, Double.class},
                            argumentNames = {"modelPart", "x", "y", "z"}
                    )
            },
            description = "model_part.set_bonus_pivot"
    )
    public static void setBonusPivot(@LuaNotNil FiguraModelPart modelPart, Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setBonusPivot", x, y, z);
        modelPart.customization.setBonusPivot(vec);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_position_matrix"
    )
    public static FiguraMat4 getPositionMatrix(@LuaNotNil FiguraModelPart modelPart) {
        modelPart.customization.recalculate();
        return modelPart.customization.getPositionMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_position_matrix_raw"
    )
    public static FiguraMat4 getPositionMatrixRaw(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.customization.getPositionMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_normal_matrix"
    )
    public static FiguraMat3 getNormalMatrix(@LuaNotNil FiguraModelPart modelPart) {
        modelPart.customization.recalculate();
        return modelPart.customization.getNormalMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_normal_matrix_raw"
    )
    public static FiguraMat3 getNormalMatrixRaw(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.customization.getNormalMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraModelPart.class, FiguraMat4.class},
                    argumentNames = {"modelPart", "matrix"}
            ),
            description = "model_part.set_matrix"
    )
    public static void setMatrix(@LuaNotNil FiguraModelPart modelPart, @LuaNotNil FiguraMat4 matrix) {
        modelPart.customization.setMatrix(matrix);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_visible"
    )
    public static boolean getVisible(@LuaNotNil FiguraModelPart modelPart) {
        while (modelPart != null && modelPart.customization.visible == null)
            modelPart = modelPart.parent;
        return modelPart == null || modelPart.customization.visible;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraModelPart.class, Boolean.class},
                    argumentNames = {"modelPart", "visible"}
            ),
            description = "model_part.set_visible"
    )
    public static void setVisible(@LuaNotNil FiguraModelPart modelPart, Boolean bool) {
        modelPart.customization.visible = bool;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.reset_visible"
    )
    public static void resetVisible(@LuaNotNil FiguraModelPart modelPart) {
        modelPart.customization.visible = null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_primary_render_type"
    )
    public static String getPrimaryRenderType(@LuaNotNil FiguraModelPart modelPart) {
        FiguraTextureSet.RenderTypes renderType = modelPart.customization.getPrimaryRenderType();
        return renderType == null ? null : renderType.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_secondary_render_type"
    )
    public static String getSecondaryRenderType(@LuaNotNil FiguraModelPart modelPart) {
        FiguraTextureSet.RenderTypes renderType = modelPart.customization.getSecondaryRenderType();
        return renderType == null ? null : renderType.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraModelPart.class, String.class},
                    argumentNames = {"modelPart", "renderType"}
            ),
            description = "model_part.set_primary_render_type"
    )
    public static void setPrimaryRenderType(@LuaNotNil FiguraModelPart modelPart, @LuaNotNil String type) {
        try {
            modelPart.customization.setPrimaryRenderType(FiguraTextureSet.RenderTypes.valueOf(type));
        } catch (Exception ignored) {
            throw new LuaRuntimeException("Illegal RenderType: \"" + type + "\".");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraModelPart.class, String.class},
                    argumentNames = {"modelPart", "renderType"}
            ),
            description = "model_part.set_secondary_render_type"
    )
    public static void setSecondaryRenderType(@LuaNotNil FiguraModelPart modelPart, @LuaNotNil String type) {
        try {
            modelPart.customization.setSecondaryRenderType(FiguraTextureSet.RenderTypes.valueOf(type));
        } catch (Exception ignored) {
            throw new LuaRuntimeException("Illegal RenderType: \"" + type + "\".");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, String.class},
                            argumentNames = {"modelPart", "textureType"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, String.class, String.class},
                            argumentNames = {"modelPart", "textureType", "path"}
                    )
            },
            description = "model_part.set_primary_texture"
    )
    public static void setPrimaryTexture(@LuaNotNil FiguraModelPart modelPart, String type, String path) {
        modelPart.customization.primaryTexture = FiguraTextureSet.getOverrideTexture(modelPart.owner, type, path);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, String.class},
                            argumentNames = {"modelPart", "textureType"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, String.class, String.class},
                            argumentNames = {"modelPart", "textureType", "path"}
                    )
            },
            description = "model_part.set_secondary_texture"
    )
    public static void setSecondaryTexture(@LuaNotNil FiguraModelPart modelPart, String type, String path) {
        modelPart.customization.secondaryTexture = FiguraTextureSet.getOverrideTexture(modelPart.owner, type, path);
    }

    public final FiguraMat4 savedPartToWorldMat = FiguraMat4.of();
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.part_to_world_matrix"
    )
    public static FiguraMat4 partToWorldMatrix(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.savedPartToWorldMat.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_texture_size"
    )
    public static FiguraVec2 getTextureSize(@LuaNotNil FiguraModelPart modelPart) {
        if (modelPart.textureWidth == -1 || modelPart.textureHeight == -1)
            throw new LuaRuntimeException("Cannot get texture size of part, it has multiple different-sized textures!");
        return FiguraVec2.of(modelPart.textureWidth, modelPart.textureHeight);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, FiguraVec2.class},
                            argumentNames = {"modelPart", "uv"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, Double.class, Double.class},
                            argumentNames = {"modelPart", "u", "v"}
                    )
            },
            description = "model_part.set_uv"
    )
    public static void setUV(@LuaNotNil FiguraModelPart modelPart, Object x, Double y) {
        modelPart.customization.uvMatrix.reset();
        FiguraVec2 uv = LuaUtils.parseVec2("setUV", x, y);
        modelPart.customization.uvMatrix.translate(uv.x, uv.y);
        uv.free();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, FiguraVec2.class},
                            argumentNames = {"modelPart", "uv"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, Double.class, Double.class},
                            argumentNames = {"modelPart", "u", "v"}
                    )
            },
            description = "model_part.set_uv_pixels"
    )
    public static void setUVPixels(@LuaNotNil FiguraModelPart modelPart, Object x, Double y) {
        if (modelPart.textureWidth == -1 || modelPart.textureHeight == -1)
            throw new LuaRuntimeException("Cannot call setUVPixels on a part with multiple texture sizes!");

        modelPart.customization.uvMatrix.reset();
        FiguraVec2 uv = LuaUtils.parseVec2("setUVPixels", x, y);
        uv.divide(modelPart.textureWidth, modelPart.textureHeight);
        modelPart.customization.uvMatrix.translate(uv.x, uv.y);
        uv.free();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraModelPart.class, FiguraMat3.class},
                    argumentNames = {"modelPart", "matrix"}
            ),
            description = "model_part.set_uv_matrix"
    )
    public static void setUVMatrix(@LuaNotNil FiguraModelPart modelPart, @LuaNotNil FiguraMat3 matrix) {
        modelPart.customization.uvMatrix.set(matrix);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, FiguraVec3.class},
                            argumentNames = {"modelPart", "color"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, Double.class, Double.class, Double.class},
                            argumentNames = {"modelPart", "r", "g", "b"}
                    )
            },
            description = "model_part.set_color"
    )
    public static void setColor(@LuaNotNil FiguraModelPart modelPart, Object r, Double g, Double b) {
        modelPart.customization.color = LuaUtils.parseVec3("setColor", r, g, b, 1, 1, 1);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_color"
    )
    public static FiguraVec3 getColor(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.customization.color.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraModelPart.class, Float.class},
                    argumentNames = {"modelPart", "opacity"}
            ),
            description = "model_part.set_opacity"
    )
    public static void setOpacity(@LuaNotNil FiguraModelPart modelPart, @LuaNotNil Float opacity) {
        modelPart.customization.alpha = opacity;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_opacity"
    )
    public static Float getOpacity(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.customization.alpha;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, FiguraVec2.class},
                            argumentNames = {"modelPart", "light"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraModelPart.class, Integer.class, Integer.class},
                            argumentNames = {"modelPart", "blockLight", "skyLight"}
                    )
            },
            description = "model_part.set_light"
    )
    public static void setLight(@LuaNotNil FiguraModelPart modelPart, Object light, Double skyLight) {
        FiguraVec2 lightVec = LuaUtils.parseVec2("setLight", light, skyLight);
        modelPart.customization.light = LightTexture.pack((int) lightVec.x, (int) lightVec.y);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_light"
    )
    public static FiguraVec2 getLight(@LuaNotNil FiguraModelPart modelPart) {
        int light = modelPart.customization.light;
        return FiguraVec2.of(LightTexture.block(light), LightTexture.sky(light));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraModelPart.class, String.class},
                    argumentNames = {"modelPart", "parentType"}
            ),
            description = "model_part.set_parent_type"
    )
    public static void setParentType(@LuaNotNil FiguraModelPart modelPart, @LuaNotNil String parent) {
        modelPart.parentType = ParentType.get(parent);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_parent_type"
    )
    public static String getParentType(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.parentType == null ? null : modelPart.parentType.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraModelPart.class,
                    argumentNames = "modelPart"
            ),
            description = "model_part.get_type"
    )
    public static String getType(@LuaNotNil FiguraModelPart modelPart) {
        return modelPart.customization.partType.name();
    }

    //-- METAMETHODS --//
    @LuaWhitelist
    public static Object __index(@LuaNotNil FiguraModelPart modelPart, @LuaNotNil String key) {
        if (modelPart.childCache.containsKey(key))
            return modelPart.childCache.get(key);
        for (FiguraModelPart child : modelPart.children) {
            if (child.name.equals(key)) {
                modelPart.childCache.put(key, child);
                return child;
            }
        }
        modelPart.childCache.put(key, null);
        return null;
    }

    //-- READING METHODS FROM NBT --//

    public FiguraModelPart(Avatar owner, String name, PartCustomization customization, int index, List<FiguraModelPart> children) {
        this.owner = owner;
        this.name = name;
        this.customization = customization;
        this.index = index;
        this.children = children;
        for (FiguraModelPart child : children)
            child.parent = this;
    }



}
