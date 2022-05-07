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
        description = "A proxy for a block state from Minecraft. " +
                "Instances are obtained through the WorldAPI."
)
public class BlockStateWrapper {

    private final WeakReference<BlockState> blockState;

    public BlockStateWrapper(BlockState wrapped) {
        blockState = new WeakReference<>(wrapped);
        id = Registry.BLOCK.getKey(wrapped.getBlock()).toString();
    }

    @LuaWhitelist
    @LuaFieldDoc(
            canEdit = false,
            description = "The identifier of the block this BlockState comes from."
    )
    public final String id;

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState",
                    returnType = Boolean.class
            ),
            description = "Checks whether or not this BlockState exists. I can't think of a " +
                    "situation where it wouldn't exist, but this function is provided for you " +
                    "to check just in case."
    )
    public static boolean exists(BlockStateWrapper blockState) {
        LuaUtils.nullCheck("exists", "blockState", blockState);
        return blockState.blockState.get() != null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"},
                            returnType = Boolean.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"},
                            returnType = Boolean.class
                    )
            },
            description = "Gets whether or not the BlockState, at this position, would propagate sky light " +
                    "downwards."
    )
    public static boolean isTranslucent(BlockStateWrapper blockState, Object x, Double y, Double z) {
        if (!WorldAPI.exists()) throw new LuaRuntimeException("World does not exist!");
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("isTranslucent", "blockState", blockState);
        FiguraVec3 pos = LuaUtils.parseVec3("isTranslucent", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return blockState.blockState.get().propagatesSkylightDown(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"},
                            returnType = Integer.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"},
                            returnType = Integer.class
                    )
            },
            description = "Gets the opacity of the BlockState, in terms of how much it affects light levels."
    )
    public static int getOpacity(BlockStateWrapper blockState, Object x, Double y, Double z) {
        if (!WorldAPI.exists()) throw new LuaRuntimeException("World does not exist!");
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("getOpacity", "blockState", blockState);
        FiguraVec3 pos = LuaUtils.parseVec3("getOpacity", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return blockState.blockState.get().getLightBlock(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"},
                            returnType = Boolean.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"},
                            returnType = Boolean.class
                    )
            },
            description = "Gets the map color of this BlockState, as a Vector3 with R,G,B ranging 0 to 1."
    )
    public static FiguraVec3 getMapColor(BlockStateWrapper blockState, Object x, Double y, Double z) {
        if (!WorldAPI.exists()) throw new LuaRuntimeException("World does not exist!");
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("getMapColor", "blockState", blockState);
        FiguraVec3 pos = LuaUtils.parseVec3("getMapColor", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return ColorUtils.intToRGB(blockState.blockState.get().getMapColor(WorldAPI.getCurrentWorld(), blockPos).col);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"},
                            returnType = Boolean.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"},
                            returnType = Boolean.class
                    )
            },
            description = "Gets whether or not the BlockState, at this position, is a solid block."
    )
    public static boolean isSolidBlock(BlockStateWrapper blockState, Object x, Double y, Double z) {
        if (!WorldAPI.exists()) throw new LuaRuntimeException("World does not exist!");
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("isSolidBlock", "blockState", blockState);
        FiguraVec3 pos = LuaUtils.parseVec3("isSolidBlock", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return blockState.blockState.get().isRedstoneConductor(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"},
                            returnType = Boolean.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"},
                            returnType = Boolean.class
                    )
            },
            description = "Gets whether or not the BlockState, at this position, has a full cube as its " +
                    "collision hitbox."
    )
    public static boolean isFullCube(BlockStateWrapper blockState, Object x, Double y, Double z) {
        if (!WorldAPI.exists()) throw new LuaRuntimeException("World does not exist!");
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("isFullCube", "blockState", blockState);
        FiguraVec3 pos = LuaUtils.parseVec3("isFullCube", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return blockState.blockState.get().isCollisionShapeFullBlock(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"},
                            returnType = Boolean.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"},
                            returnType = Boolean.class
                    )
            },
            description = "Gets whether or not the BlockState, at this position, uses emissive lighting."
    )
    public static boolean hasEmissiveLighting(BlockStateWrapper blockState, Object x, Double y, Double z) {
        if (!WorldAPI.exists()) throw new LuaRuntimeException("World does not exist!");
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("hasEmissiveLighting", "blockState", blockState);
        FiguraVec3 pos = LuaUtils.parseVec3("hasEmissiveLighting", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return blockState.blockState.get().emissiveRendering(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"},
                            returnType = Float.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"},
                            returnType = Float.class
                    )
            },
            description = "Gets the hardness of the BlockState, when placed at this position."
    )
    public static float getHardness(BlockStateWrapper blockState, Object x, Double y, Double z) {
        if (!WorldAPI.exists()) throw new LuaRuntimeException("World does not exist!");
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("getHardness", "blockState", blockState);
        FiguraVec3 pos = LuaUtils.parseVec3("getHardness", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return blockState.blockState.get().getDestroySpeed(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, FiguraVec3.class},
                            argumentNames = {"blockState", "pos"},
                            returnType = Integer.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BlockStateWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"blockState", "x", "y", "z"},
                            returnType = Integer.class
                    )
            },
            description = "Gets the amount of signal strength a comparator would get from this BlockState."
    )
    public static int getComparatorOutput(BlockStateWrapper blockState, Object x, Double y, Double z) {
        if (!WorldAPI.exists()) throw new LuaRuntimeException("World does not exist!");
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("getComparatorOutput", "blockState", blockState);
        FiguraVec3 pos = LuaUtils.parseVec3("getComparatorOutput", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        return blockState.blockState.get().getAnalogOutputSignal(WorldAPI.getCurrentWorld(), blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState",
                    returnType = Boolean.class
            ),
            description = "Gets whether or not this BlockState has an associated block entity."
    )
    public static boolean hasBlockEntity(BlockStateWrapper blockState) {
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("hasBlockEntity", "blockState", blockState);
        return blockState.blockState.get().hasBlockEntity();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState",
                    returnType = Boolean.class
            ),
            description = "Gets whether or not this BlockState is opaque."
    )
    public static boolean isOpaque(BlockStateWrapper blockState) {
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("isOpaque", "blockState", blockState);
        return blockState.blockState.get().hasBlockEntity();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState",
                    returnType = Boolean.class
            ),
            description = "Gets whether or not this BlockState emits redstone power."
    )
    public static boolean emitsRedstonePower(BlockStateWrapper blockState) {
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("emitsRedstonePower", "blockState", blockState);
        return blockState.blockState.get().isSignalSource();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState",
                    returnType = Integer.class
            ),
            description = "Gets the emission light level of this BlockState."
    )
    public static int getLuminance(BlockStateWrapper blockState) {
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("getLuminance", "blockState", blockState);
        return blockState.blockState.get().getLightEmission();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState",
                    returnType = Float.class
            ),
            description = "Gets the slipperiness of this BlockState. (Slime blocks and ice in vanilla)"
    )
    public static float getSlipperiness(BlockStateWrapper blockState) {
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("getSlipperiness", "blockState", blockState);
        return blockState.blockState.get().getBlock().getFriction();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState",
                    returnType = Float.class
            ),
            description = "Gets the velocity multiplier of this BlockState. (Only Soul sand, honey blocks in vanilla)"
    )
    public static float getVelocityMultiplier(BlockStateWrapper blockState) {
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("getVelocityMultiplier", "blockState", blockState);
        return blockState.blockState.get().getBlock().getSpeedFactor();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState",
                    returnType = Float.class
            ),
            description = "Gets the JumpVelocityMultiplier of this BlockState. (Literally just honey blocks in vanilla)"
    )
    public static float getJumpVelocityMultiplier(BlockStateWrapper blockState) {
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("getJumpVelocityMultiplier", "blockState", blockState);
        return blockState.blockState.get().getBlock().getJumpFactor();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BlockStateWrapper.class,
                    argumentNames = "blockState",
                    returnType = Float.class
            ),
            description = "Gets whether or not this BlockState has an associated block entity."
    )
    public static float getBlastResistance(BlockStateWrapper blockState) {
        if (!exists(blockState)) throw new LuaRuntimeException("BlockState does not exist!");
        LuaUtils.nullCheck("hasBlockEntity", "blockState", blockState);
        return blockState.blockState.get().getBlock().getExplosionResistance();
    }

}
