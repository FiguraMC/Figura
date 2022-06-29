package org.moon.figura.lua.api.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import org.moon.figura.lua.LuaNotNil;
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
        name = "Biome",
        description = "biome"
)
public class BiomeWrapper {

    private final WeakReference<Biome> biome;
    private BlockPos pos;

    @LuaWhitelist
    @LuaFieldDoc(description = "biome.name")
    public final String name;

    public BiomeWrapper(Biome biome, BlockPos pos) {
        this.biome = new WeakReference<>(biome);
        this.pos = pos;
        this.name = WorldAPI.getCurrentWorld().registryAccess().registry(Registry.BIOME_REGISTRY).get().getKey(biome).toString();
    }

    public static Biome getBiome(BiomeWrapper biome) {
        if (!exists(biome)) throw new LuaRuntimeException("Biome does not exist!");
        return biome.biome.get();
    }

    protected static BlockPos getBlockPos(BiomeWrapper biome) {
        return biome.pos == null ? BlockPos.ZERO : biome.pos;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BiomeWrapper.class,
                    argumentNames = "biome"
            ),
            description = "biome.exists"
    )
    public static boolean exists(@LuaNotNil BiomeWrapper biome) {
        return biome.biome.get() != null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BiomeWrapper.class,
                    argumentNames = "biome"
            ),
            description = "biome.get_pos"
    )
    public static FiguraVec3 getPos(@LuaNotNil BiomeWrapper biome) {
        BlockPos pos = getBlockPos(biome);
        return FiguraVec3.of(pos.getX(), pos.getY(), pos.getZ());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {BiomeWrapper.class, FiguraVec3.class},
                            argumentNames = {"biome", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {BiomeWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"biome", "x", "y", "z"}
                    )
            },
            description = "biome.set_pos"
    )
    public static void setPos(@LuaNotNil BiomeWrapper biome, Object x, Double y, Double z) {
        FiguraVec3 newPos = LuaUtils.parseVec3("setPos", x, y, z);
        biome.pos = newPos.asBlockPos();
        newPos.free();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BiomeWrapper.class,
                    argumentNames = "biome"
            ),
            description = "biome.get_temperature"
    )
    public static float getTemperature(@LuaNotNil BiomeWrapper biome) {
        return getBiome(biome).getBaseTemperature();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BiomeWrapper.class,
                    argumentNames = "biome"
            ),
            description = "biome.get_precipitation"
    )
    public static String getPrecipitation(@LuaNotNil BiomeWrapper biome) {
        return getBiome(biome).getPrecipitation().name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BiomeWrapper.class,
                    argumentNames = "biome"
            ),
            description = "biome.get_sky_color"
    )
    public static FiguraVec3 getSkyColor(@LuaNotNil BiomeWrapper biome) {
        return ColorUtils.intToRGB(getBiome(biome).getSkyColor());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BiomeWrapper.class,
                    argumentNames = "biome"
            ),
            description = "biome.get_foliage_color"
    )
    public static FiguraVec3 getFoliageColor(@LuaNotNil BiomeWrapper biome) {
        return ColorUtils.intToRGB(getBiome(biome).getFoliageColor());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BiomeWrapper.class,
                    argumentNames = "biome"
            ),
            description = "biome.get_grass_color"
    )
    public static FiguraVec3 getGrassColor(@LuaNotNil BiomeWrapper biome) {
        BlockPos pos = getBlockPos(biome);
        return ColorUtils.intToRGB(getBiome(biome).getGrassColor(pos.getX(), pos.getY()));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BiomeWrapper.class,
                    argumentNames = "biome"
            ),
            description = "biome.get_fog_color"
    )
    public static FiguraVec3 getFogColor(@LuaNotNil BiomeWrapper biome) {
        return ColorUtils.intToRGB(getBiome(biome).getFogColor());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BiomeWrapper.class,
                    argumentNames = "biome"
            ),
            description = "biome.get_water_color"
    )
    public static FiguraVec3 getWaterColor(@LuaNotNil BiomeWrapper biome) {
        return ColorUtils.intToRGB(getBiome(biome).getWaterColor());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BiomeWrapper.class,
                    argumentNames = "biome"
            ),
            description = "biome.get_water_fog_color"
    )
    public static FiguraVec3 getWaterFogColor(@LuaNotNil BiomeWrapper biome) {
        return ColorUtils.intToRGB(getBiome(biome).getWaterFogColor());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BiomeWrapper.class,
                    argumentNames = "biome"
            ),
            description = "biome.get_downfall"
    )
    public static float getDownfall(@LuaNotNil BiomeWrapper biome) {
        return getBiome(biome).getDownfall();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BiomeWrapper.class,
                    argumentNames = "biome"
            ),
            description = "biome.is_hot"
    )
    public static boolean isHot(@LuaNotNil BiomeWrapper biome) {
        return getBiome(biome).shouldSnowGolemBurn(getBlockPos(biome));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = BiomeWrapper.class,
                    argumentNames = "biome"
            ),
            description = "biome.is_cold"
    )
    public static boolean isCold(@LuaNotNil BiomeWrapper biome) {
        return getBiome(biome).coldEnoughToSnow(getBlockPos(biome));
    }

    @Override
    public String toString() {
        return name + " (Biome)";
    }
}
