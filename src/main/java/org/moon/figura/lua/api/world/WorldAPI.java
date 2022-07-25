package org.moon.figura.lua.api.world;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.entity.PlayerEntityWrapper;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaTable;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "WorldAPI",
        description = "world"
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
                            argumentNames = "pos"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "world.get_biome"
    )
    public static BiomeWrapper getBiome(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.oldParseVec3("getBiome", x, y, z);
        BiomeWrapper result = new BiomeWrapper(getCurrentWorld().getBiome(pos.asBlockPos()).value(), pos.asBlockPos());
        pos.free();
        return result;
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
            description = "world.get_blockstate"
    )
    public static BlockStateWrapper getBlockState(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.oldParseVec3("getBlockState", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return new BlockStateWrapper(world.getBlockState(blockPos), blockPos);
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
            description = "world.get_redstone_power"
    )
    public static Integer getRedstonePower(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.oldParseVec3("getRedstonePower", x, y, z);
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
                            argumentNames = "pos"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "world.get_strong_redstone_power"
    )
    public static Integer getStrongRedstonePower(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.oldParseVec3("getStrongRedstonePower", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        if (getCurrentWorld().getChunkAt(blockPos) == null)
            return null;
        return getCurrentWorld().getDirectSignalTo(blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(),
                    @LuaFunctionOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            description = "world.get_time"
    )
    public static double getTime(Double delta) {
        if (delta == null) delta = 0d;
        return getCurrentWorld().getGameTime() + delta;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(),
                    @LuaFunctionOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            description = "world.get_time_of_day"
    )
    public static double getTimeOfDay(Double delta) {
        if (delta == null) delta = 0d;
        return getCurrentWorld().getDayTime() + delta;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(),
            description = "world.get_moon_phase"
    )
    public static int getMoonPhase() {
        return getCurrentWorld().getMoonPhase();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(),
                    @LuaFunctionOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            description = "world.get_rain_gradient"
    )
    public static double getRainGradient(Float delta) {
        if (delta == null) delta = 1f;
        return getCurrentWorld().getRainLevel(delta);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(),
            description = "world.is_thundering"
    )
    public static boolean isThundering() {
        return getCurrentWorld().isThundering();
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
            description = "world.get_light_level"
    )
    public static Integer getLightLevel(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.oldParseVec3("getLightLevel", x, y, z);
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
                            argumentNames = "pos"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "world.get_sky_light_level"
    )
    public static Integer getSkyLightLevel(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.oldParseVec3("getSkyLightLevel", x, y, z);
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
                            argumentNames = "pos"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "world.get_block_light_level"
    )
    public static Integer getBlockLightLevel(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.oldParseVec3("getBlockLightLevel", x, y, z);
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
                            argumentNames = "pos"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "world.is_open_sky"
    )
    public static Boolean isOpenSky(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.oldParseVec3("isOpenSky", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        pos.free();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.canSeeSky(blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(),
            description = "world.get_players"
    )
    public static LuaTable getPlayers() {
        LuaTable playerList = new LuaTable();
        for (Player player : getCurrentWorld().players())
            playerList.put(player.getName().getString(), PlayerEntityWrapper.fromEntity(player));
        return playerList;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(),
            description = "world.exists"
    )
    public static boolean exists() {
        return getCurrentWorld() != null;
    }

    @Override
    public String toString() {
        return "WorldAPI";
    }
}
