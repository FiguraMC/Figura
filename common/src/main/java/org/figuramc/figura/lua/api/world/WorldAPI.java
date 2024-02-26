package org.figuramc.figura.lua.api.world;

import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.ReadOnlyLuaTable;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.lua.api.entity.PlayerAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;

import java.util.*;

@LuaWhitelist
@LuaTypeDoc(
        name = "WorldAPI",
        value = "world"
)
public class WorldAPI {

    public static final WorldAPI INSTANCE = new WorldAPI();

    public static Level getCurrentWorld() {
        return Minecraft.getInstance().level;
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
            value = "world.get_biome"
    )
    public static BiomeAPI getBiome(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getBiome", x, y, z);
        return new BiomeAPI(getCurrentWorld().getBiome(pos.asBlockPos()).value(), pos.asBlockPos());
    }

    @SuppressWarnings("deprecation")
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
            value = "world.get_block_state"
    )
    public static BlockStateAPI getBlockState(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getBlockState", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        Level world = getCurrentWorld();
        if (!world.hasChunkAt(blockPos))
            return new BlockStateAPI(Blocks.VOID_AIR.defaultBlockState(), blockPos);
        return new BlockStateAPI(world.getBlockState(blockPos), blockPos);
    }
    @SuppressWarnings("deprecation")
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
        value = "world.is_chunk_loaded"
    )
    public static boolean isChunkLoaded(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getBlockState", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        Level world = getCurrentWorld();
        return world.hasChunkAt(blockPos);
    }

    @SuppressWarnings("deprecation")
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class, FiguraVec3.class},
                            argumentNames = {"min", "max"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, FiguraVec3.class},
                            argumentNames = {"minX", "minY", "minZ", "max"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class, Double.class, Double.class, Double.class},
                            argumentNames = {"min", "maxX", "maxY", "maxZ"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"minX", "minY", "minZ", "maxX", "maxY", "maxZ"}
                    )
            },
            value = "world.get_blocks"
    )
    public static List<BlockStateAPI> getBlocks(Object x, Object y, Double z, Double w, Double t, Double h) {
        Pair<FiguraVec3, FiguraVec3> pair = LuaUtils.parse2Vec3("getBlocks", x, y, z, w, t, h, 1);
        List<BlockStateAPI> list = new ArrayList<>();

        BlockPos min = pair.getFirst().asBlockPos();
        BlockPos max = pair.getSecond().asBlockPos();
        max = new BlockPos(
                Math.min(min.getX() + 8, max.getX()),
                Math.min(min.getY() + 8, max.getY()),
                Math.min(min.getZ() + 8, max.getZ())
        );

        Level world = getCurrentWorld();
        if (!world.hasChunksAt(min, max))
            return list;

        BlockPos.betweenClosedStream(min, max).forEach(blockPos -> {
            BlockPos pos = new BlockPos(blockPos);
            list.add(new BlockStateAPI(world.getBlockState(pos), pos));
        });
        return list;
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
            value = "world.get_redstone_power"
    )
    public static int getRedstonePower(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getRedstonePower", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        if (getCurrentWorld().getChunkAt(blockPos) == null)
            return 0;
        return getCurrentWorld().getBestNeighborSignal(blockPos);
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
            value = "world.get_strong_redstone_power"
    )
    public static int getStrongRedstonePower(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getStrongRedstonePower", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        if (getCurrentWorld().getChunkAt(blockPos) == null)
            return 0;
        return getCurrentWorld().getDirectSignalTo(blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            value = "world.get_time"
    )
    public static double getTime(double delta) {
        return getCurrentWorld().getGameTime() + delta;
    }

    @LuaWhitelist
    @LuaMethodDoc(
        overloads = {
            @LuaMethodOverload,
            @LuaMethodOverload(
                argumentTypes = Double.class,
                argumentNames = "delta"
            )
        },
        value = "world.get_time_of_day"
    )
    public static double getTimeOfDay(double delta) {
        return getCurrentWorld().getDayTime() + delta;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            value = "world.get_day_time"
    )
    public static double getDayTime(double delta) {
        return (getCurrentWorld().getDayTime() + delta) % 24000;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            value = "world.get_day"
    )
    public static double getDay(double delta) {
        return Math.floor((getCurrentWorld().getDayTime() + delta) / 24000);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload,
            value = "world.get_moon_phase"
    )
    public static int getMoonPhase() {
        return getCurrentWorld().getMoonPhase();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "delta"
                    )
            },
            value = "world.get_rain_gradient"
    )
    public static double getRainGradient(Float delta) {
        if (delta == null) delta = 1f;
        return getCurrentWorld().getRainLevel(delta);
    }

    @LuaWhitelist
    @LuaMethodDoc("world.is_thundering")
    public static boolean isThundering() {
        return getCurrentWorld().isThundering();
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
            value = "world.get_light_level"
    )
    public static Integer getLightLevel(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getLightLevel", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        world.updateSkyBrightness();
        return world.getLightEngine().getRawBrightness(blockPos, world.getSkyDarken());
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
            value = "world.get_sky_light_level"
    )
    public static Integer getSkyLightLevel(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getSkyLightLevel", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.getBrightness(LightLayer.SKY, blockPos);
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
            value = "world.get_block_light_level"
    )
    public static Integer getBlockLightLevel(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("getBlockLightLevel", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.getBrightness(LightLayer.BLOCK, blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec2.class, String.class},
                            argumentNames = {"pos", "heightmap"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, String.class},
                            argumentNames = {"x", "z", "heightmap"}
                    )
            },
            value = "world.get_height"
    )
    public static Integer getHeight(Object x, Double z, String heightmap) {
        FiguraVec2 pos = LuaUtils.parseVec2("getHeight", x, z);
        Level world = getCurrentWorld();

        BlockPos blockPos = new BlockPos((int) pos.x(), 0, (int) pos.y());
        if (world.getChunkAt(blockPos) == null)
            return null;

        Heightmap.Types heightmapType;
        try {
            heightmapType = heightmap != null ? Heightmap.Types.valueOf(heightmap.toUpperCase(Locale.US)) : Heightmap.Types.MOTION_BLOCKING;
        } catch (IllegalArgumentException e) {
            throw new LuaError("Invalid heightmap type provided");
        }

        return world.getHeight(heightmapType, (int) pos.x(), (int) pos.y());
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
            value = "world.is_open_sky"
    )
    public static Boolean isOpenSky(Object x, Double y, Double z) {
        FiguraVec3 pos = LuaUtils.parseVec3("isOpenSky", x, y, z);
        BlockPos blockPos = pos.asBlockPos();
        Level world = getCurrentWorld();
        if (world.getChunkAt(blockPos) == null)
            return null;
        return world.canSeeSky(blockPos);
    }

    @LuaWhitelist
    @LuaMethodDoc("world.get_dimension")
    public static String getDimension() {
        Level world = getCurrentWorld();
        return world.dimension().location().toString();
    }

    @LuaWhitelist
    @LuaMethodDoc("world.get_players")
    public static Map<String, EntityAPI<?>> getPlayers() {
        HashMap<String, EntityAPI<?>> playerList = new HashMap<>();
        for (Player player : getCurrentWorld().players())
            playerList.put(player.getName().getString(), PlayerAPI.wrap(player));
        return playerList;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "UUID"
            ),
            value = "world.get_entity"
    )
    public static EntityAPI<?> getEntity(@LuaNotNil String uuid) {
        try {
            return EntityAPI.wrap(EntityUtils.getEntityByUUID(UUID.fromString(uuid)));
        } catch (Exception ignored) {
            throw new LuaError("Invalid UUID");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("world.avatar_vars")
    public static Map<String, LuaTable> avatarVars() {
        HashMap<String, LuaTable> varList = new HashMap<>();
        for (Avatar avatar : AvatarManager.getLoadedAvatars()) {
            LuaTable tbl = avatar.luaRuntime == null ? new LuaTable() : avatar.luaRuntime.avatar_meta.storedStuff;
            varList.put(avatar.owner.toString(), new ReadOnlyLuaTable(tbl));
        }
        return varList;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "block"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, FiguraVec3.class},
                            argumentNames = {"block", "pos"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class},
                            argumentNames = {"block", "x", "y", "z"}
                    )
            },
            value = "world.new_block"
    )
    public static BlockStateAPI newBlock(@LuaNotNil String string, Object x, Double y, Double z) {
        BlockPos pos = LuaUtils.parseVec3("newBlock", x, y, z).asBlockPos();
        try {
            Level level = getCurrentWorld();
            BlockState block = BlockStateArgument.block(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).getState();
            return new BlockStateAPI(block, pos);
        } catch (Exception e) {
            throw new LuaError("Could not parse block state from string: " + string);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "item"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Integer.class},
                            argumentNames = {"item", "count"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Integer.class, Integer.class},
                            argumentNames = {"item", "count", "damage"}
                    )
            },
            value = "world.new_item"
    )
    public static ItemStackAPI newItem(@LuaNotNil String string, Integer count, Integer damage) {
        try {
            Level level = getCurrentWorld();
            ItemStack item = ItemArgument.item(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).createItemStack(1, false);
            if (count != null)
                item.setCount(count);
            if (damage != null)
                item.setDamageValue(damage);
            return new ItemStackAPI(item);
        } catch (Exception e) {
            throw new LuaError("Could not parse item stack from string: " + string);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("world.exists")
    public static boolean exists() {
        return getCurrentWorld() != null;
    }

    @LuaWhitelist
    @LuaMethodDoc("world.get_build_height")
    public static int[] getBuildHeight() {
        Level world = getCurrentWorld();
        return new int[]{world.getMinBuildHeight(), world.getMaxBuildHeight()};
    }

    @LuaWhitelist
    @LuaMethodDoc("world.get_spawn_point")
    public static FiguraVec3 getSpawnPoint() {
        Level world = getCurrentWorld();
        return FiguraVec3.fromBlockPos(world.getSharedSpawnPos());
    }

    @Override
    public String toString() {
        return "WorldAPI";
    }
}
