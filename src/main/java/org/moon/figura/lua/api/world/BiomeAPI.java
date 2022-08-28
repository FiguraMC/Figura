package org.moon.figura.lua.api.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "Biome",
        value = "biome"
)
public class BiomeAPI {

    private final Biome biome;
    private BlockPos pos;

//    @LuaWhitelist
//    @LuaFieldDoc(description = "biome.name")
    public final String name;

    public BiomeAPI(Biome biome, BlockPos pos) {
        this.biome = biome;
        this.pos = pos;
        this.name = WorldAPI.getCurrentWorld().registryAccess().registry(Registry.BIOME_REGISTRY).get().getKey(biome).toString();
    }

    protected BlockPos getBlockPos() {
        return pos == null ? BlockPos.ZERO : pos;
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_pos")
    public FiguraVec3 getPos() {
        return FiguraVec3.fromBlockPos(getBlockPos());
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
            value = "biome.set_pos"
    )
    public void setPos(Object x, Double y, Double z) {
        FiguraVec3 newPos = LuaUtils.parseVec3("setPos", x, y, z);
        pos = newPos.asBlockPos();
        newPos.free();
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_temperature")
    public float getTemperature() {
        return biome.getBaseTemperature();
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_precipitation")
    public String getPrecipitation() {
        return biome.getPrecipitation().name();
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_sky_color")
    public FiguraVec3 getSkyColor() {
        return ColorUtils.intToRGB(biome.getSkyColor());
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_foliage_color")
    public FiguraVec3 getFoliageColor() {
        return ColorUtils.intToRGB(biome.getFoliageColor());
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_grass_color")
    public FiguraVec3 getGrassColor() {
        BlockPos pos = getBlockPos();
        return ColorUtils.intToRGB(biome.getGrassColor(pos.getX(), pos.getY()));
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_fog_color")
    public FiguraVec3 getFogColor() {
        return ColorUtils.intToRGB(biome.getFogColor());
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_water_color")
    public FiguraVec3 getWaterColor() {
        return ColorUtils.intToRGB(biome.getWaterColor());
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_water_fog_color")
    public FiguraVec3 getWaterFogColor() {
        return ColorUtils.intToRGB(biome.getWaterFogColor());
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_downfall")
    public float getDownfall() {
        return biome.getDownfall();
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.is_hot")
    public boolean isHot() {
        return biome.shouldSnowGolemBurn(getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.is_cold")
    public boolean isCold() {
        return biome.coldEnoughToSnow(getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_name")
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (Biome)";
    }
}
