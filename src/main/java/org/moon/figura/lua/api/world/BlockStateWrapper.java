package org.moon.figura.lua.api.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.state.BlockState;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

import java.lang.ref.WeakReference;

@LuaWhitelist
@LuaTypeDoc(
        name = "BlockState",
        description = "blockstate"
)
public class BlockStateWrapper {

    private final WeakReference<BlockState> blockState;

    @LuaWhitelist
    @LuaFieldDoc(description = "blockstate.id")
    public final String id;

    public BlockStateWrapper(BlockState wrapped) {
        blockState = new WeakReference<>(wrapped);
        id = Registry.BLOCK.getKey(wrapped.getBlock()).toString();
    }

    protected static BlockState getState(BlockStateWrapper blockState) {
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        return blockState.blockState.get();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.exists"
    )
    public static boolean exists(BlockStateWrapper blockState) {
        return blockState != null && blockState.blockState.get() != null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"}
                    )
            },
            description = "blockstate.is_translucent"
    )
    public static boolean isTranslucent(BlockStateWrapper blockState, Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("isTranslucent", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return getState(blockState).propagatesSkylightDown(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"}
                    )
            },
            description = "blockstate.get_opacity"
    )
    public static int getOpacity(BlockStateWrapper blockState, Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getOpacity", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return getState(blockState).getLightBlock(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"}
                    )
            },
            description = "blockstate.get_map_color"
    )
    public static FiguraVec3 getMapColor(BlockStateWrapper blockState, Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getMapColor", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return ColorUtils.intToRGB(getState(blockState).getMapColor(WorldAPI.getCurrentWorld(), blockPos).col);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"}
                    )
            },
            description = "blockstate.is_solid_block"
    )
    public static boolean isSolidBlock(BlockStateWrapper blockState, Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("isSolidBlock", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return getState(blockState).isRedstoneConductor(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"}
                    )
            },
            description = "blockstate.is_full_cube"
    )
    public static boolean isFullCube(BlockStateWrapper blockState, Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("isFullCube", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return getState(blockState).isCollisionShapeFullBlock(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"}
                    )
            },
            description = "blockstate.has_emissive_lightning"
    )
    public static boolean hasEmissiveLighting(BlockStateWrapper blockState, Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("hasEmissiveLighting", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return getState(blockState).emissiveRendering(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"}
                    )
            },
            description = "blockstate.get_hardness"
    )
    public static float getHardness(BlockStateWrapper blockState, Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getHardness", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return getState(blockState).getDestroySpeed(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"}
                    )
            },
            description = "blockstate.get_comparator_output"
    )
    public static int getComparatorOutput(BlockStateWrapper blockState, Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getComparatorOutput", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return getState(blockState).getAnalogOutputSignal(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.has_block_entity"
    )
    public static boolean hasBlockEntity(BlockStateWrapper blockState) {
        return getState(blockState).hasBlockEntity();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.is_opaque"
    )
    public static boolean isOpaque(BlockStateWrapper blockState) {
        return getState(blockState).canOcclude();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.emits_redstone_power"
    )
    public static boolean emitsRedstonePower(BlockStateWrapper blockState) {
        return getState(blockState).isSignalSource();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_luminance"
    )
    public static int getLuminance(BlockStateWrapper blockState) {
        return getState(blockState).getLightEmission();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_friction"
    )
    public static float getFriction(BlockStateWrapper blockState) {
        return getState(blockState).getBlock().getFriction();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_velocity_multiplier"
    )
    public static float getVelocityMultiplier(BlockStateWrapper blockState) {
        return getState(blockState).getBlock().getSpeedFactor();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_jump_velocity_multiplier"
    )
    public static float getJumpVelocityMultiplier(BlockStateWrapper blockState) {
        return getState(blockState).getBlock().getJumpFactor();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.get_blast_resistance"
    )
    public static float getBlastResistance(BlockStateWrapper blockState) {
        return getState(blockState).getBlock().getExplosionResistance();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState"
            ),
            description = "blockstate.as_item"
    )
    public static ItemStackWrapper asItem(BlockStateWrapper blockState) {
        return new ItemStackWrapper(getState(blockState).getBlock().asItem().getDefaultInstance());
    }

    @Override
    public String toString() {
        return id + " (BlockState)";
    }
}
