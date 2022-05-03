package org.moon.figura.lua.api.world;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
public class WorldAPI {

    public static final WorldAPI INSTANCE = new WorldAPI();

    private static Level getCurrentWorld() {
        return Minecraft.getInstance().level;
    }

    @LuaWhitelist
    public static BiomeWrapper getBiome(FiguraVec3 pos) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        LuaUtils.nullCheck("getBiome", "pos", pos);
        return new BiomeWrapper(getCurrentWorld().getBiome(pos.asBlockPos()).value());
    }

    @LuaWhitelist
    public static BlockStateWrapper getBlockState(FiguraVec3 pos) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        LuaUtils.nullCheck("getBlockState", "pos", pos);
        BlockPos blockPos = pos.asBlockPos();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return new BlockStateWrapper(world.getBlockState(blockPos));
    }

    @LuaWhitelist
    public static Integer getRedstonePower(FiguraVec3 pos) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        LuaUtils.nullCheck("getRedstonePower", "pos", pos);
        BlockPos blockPos = pos.asBlockPos();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.getBestNeighborSignal(blockPos);
    }

    @LuaWhitelist
    public static Integer getStrongRedstonePower(FiguraVec3 pos) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        LuaUtils.nullCheck("getStrongRedstonePower", "pos", pos);
        BlockPos blockPos = pos.asBlockPos();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.getDirectSignalTo(blockPos);
    }

    @LuaWhitelist
    public static double getTime(Double delta) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        if (delta == null) delta = 0d;
        return getCurrentWorld().getGameTime() + delta;
    }

    @LuaWhitelist
    public static double getTimeOfDay(Double delta) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        if (delta == null) delta = 0d;
        return getCurrentWorld().getDayTime() + delta;
    }

    @LuaWhitelist
    public static int getMoonPhase() {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        return getCurrentWorld().getMoonPhase();
    }

    @LuaWhitelist
    public static double getRainGradient(Float delta) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        if (delta == null) delta = 1f;
        return getCurrentWorld().getRainLevel(delta);
    }

    @LuaWhitelist
    public static boolean isLightning() {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        return getCurrentWorld().isThundering();
    }

    @LuaWhitelist
    public static Integer getLightLevel(FiguraVec3 pos) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        LuaUtils.nullCheck("getLightLevel", "pos", pos);
        BlockPos blockPos = pos.asBlockPos();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        world.updateSkyBrightness();
        return world.getLightEngine().getRawBrightness(blockPos, world.getSkyDarken());
    }

    @LuaWhitelist
    public static Integer getSkyLightLevel(FiguraVec3 pos) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        LuaUtils.nullCheck("getSkyLightLevel", "pos", pos);
        BlockPos blockPos = pos.asBlockPos();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.getBrightness(LightLayer.SKY, blockPos);
    }

    @LuaWhitelist
    public static Integer getBlockLightLevel(FiguraVec3 pos) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        LuaUtils.nullCheck("getBlockLightLevel", "pos", pos);
        BlockPos blockPos = pos.asBlockPos();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.getBrightness(LightLayer.BLOCK, blockPos);
    }

    @LuaWhitelist
    public static Boolean isOpenSky(FiguraVec3 pos) {
        if (!exists()) throw new LuaRuntimeException("World does not exist!");
        LuaUtils.nullCheck("isOpenSky", "pos", pos);
        BlockPos blockPos = pos.asBlockPos();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.canSeeSky(blockPos);
    }

    @LuaWhitelist
    public static boolean exists() {
        return getCurrentWorld() != null;
    }

}
