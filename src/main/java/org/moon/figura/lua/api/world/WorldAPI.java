package org.moon.figura.lua.api.world;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
@LuaTypeDoc(
        name = "WorldAPI",
        description = "A global API dedicated to reading information from the Minecraft world. " +
                "Accessed using the name \"world\"."
)
public class WorldAPI {

    public static final WorldAPI INSTANCE = new WorldAPI();

    public static Level getCurrentWorld() {
        return Minecraft.getInstance().level;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos",
                            returnType = BiomeWrapper.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = BiomeWrapper.class
                    )
            },
            description = "Gets the Biome located at the given position."
    )
    public static BiomeWrapper getBiome(Object x, Double y, Double z) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        FiguraVec3 pos = LuaUtils.parseVec3("getBiome", x, y, z);
        BiomeWrapper result = new BiomeWrapper(getCurrentWorld().getBiome(pos.asBlockPos()).value());
        pos.free();
        return result;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos",
                            returnType = BlockStateWrapper.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = BlockStateWrapper.class
                    )
            },
            description = "Gets the BlockState of the block at the given position."
    )
    public static BlockStateWrapper getBlockState(Object x, Double y, Double z) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        FiguraVec3 pos = LuaUtils.parseVec3("getBlockState", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return new BlockStateWrapper(world.getBlockState(blockPos));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos",
                            returnType = Integer.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = Integer.class
                    )
            },
            description = "Gets the redstone power level of the block at the given position."
    )
    public static Integer getRedstonePower(Object x, Double y, Double z) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        FiguraVec3 pos = LuaUtils.parseVec3("getRedstonePower", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        if (getCurrentWorld().getChunkAt(blockPos) == null)
            return null;
        return getCurrentWorld().getBestNeighborSignal(blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos",
                            returnType = Integer.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = Integer.class
                    )
            },
            description = "Gets the direct redstone power level of the block at the given position."
    )
    public static Integer getStrongRedstonePower(Object x, Double y, Double z) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        FiguraVec3 pos = LuaUtils.parseVec3("getStrongRedstonePower", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        if (getCurrentWorld().getChunkAt(blockPos) == null)
            return null;
        return getCurrentWorld().getDirectSignalTo(blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {},
                            argumentNames = {},
                            returnType = Double.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta",
                            returnType = Double.class
                    )
            },
            description = "Gets the current game time of the world. If delta is passed in, then it adds delta " +
                    "to the time. The default value of delta is zero."
    )
    public static double getTime(Double delta) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        if (delta == null) delta = 0d;
        return getCurrentWorld().getGameTime() + delta;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {},
                            argumentNames = {},
                            returnType = Double.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta",
                            returnType = Double.class
                    )
            },
            description = "Gets the current day time of the world. If delta is passed in, then it adds delta " +
                    "to the time. The default value of delta is zero."
    )
    public static double getTimeOfDay(Double delta) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        if (delta == null) delta = 0d;
        return getCurrentWorld().getDayTime() + delta;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {},
                    argumentNames = {},
                    returnType = Integer.class
            ),
            description = "Gets the current moon phase of the world, stored as an integer."
    )
    public static int getMoonPhase() {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        return getCurrentWorld().getMoonPhase();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {},
                            argumentNames = {},
                            returnType = Double.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta",
                            returnType = Double.class
                    )
            },
            description = "Gets the current rain gradient in the world, interpolated from the previous " +
                    "tick to the current one. The default value of delta is 1, which is the current tick."
    )
    public static double getRainGradient(Float delta) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        if (delta == null) delta = 1f;
        return getCurrentWorld().getRainLevel(delta);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {},
                    argumentNames = {},
                    returnType = Boolean.class
            ),
            description = "Gets whether or not there is currently thunder/lightning happening in the world."
    )
    public static boolean isThundering() {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        return getCurrentWorld().isThundering();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos",
                            returnType = Integer.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = Integer.class
                    )
            },
            description = "Gets the overall light level of the block at the given position."
    )
    public static Integer getLightLevel(Object x, Double y, Double z) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        FiguraVec3 pos = LuaUtils.parseVec3("getLightLevel", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        world.updateSkyBrightness();
        return world.getLightEngine().getRawBrightness(blockPos, world.getSkyDarken());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos",
                            returnType = Integer.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = Integer.class
                    )
            },
            description = "Gets the sky light level of the block at the given position."
    )
    public static Integer getSkyLightLevel(Object x, Double y, Double z) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        FiguraVec3 pos = LuaUtils.parseVec3("getSkyLightLevel", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.getBrightness(LightLayer.SKY, blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos",
                            returnType = Integer.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = Integer.class
                    )
            },
            description = "Gets the block light level of the block at the given position."
    )
    public static Integer getBlockLightLevel(Object x, Double y, Double z) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        FiguraVec3 pos = LuaUtils.parseVec3("getBlockLightLevel", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.getBrightness(LightLayer.BLOCK, blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos",
                            returnType = Boolean.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = Boolean.class
                    )
            },
            description = "Gets whether or not the sky is open at the given position."
    )
    public static Boolean isOpenSky(Object x, Double y, Double z) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        FiguraVec3 pos = LuaUtils.parseVec3("isOpenSky", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.canSeeSky(blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {},
                    argumentNames = {},
                    returnType = Boolean.class
            ),
            description = "Checks whether or not a world currently exists. This will almost always " +
                    "be true, but might be false on some occasions such as while travelling between dimensions."
    )
    public static boolean exists() {
        return getCurrentWorld() != null;
    }

}
