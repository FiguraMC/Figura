package org.moon.figura.lua.api.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;

@LuaType(typeName = "biome")
@LuaTypeDoc(
        name = "Biome",
        description = "biome"
)
public class BiomeAPI {

    private final Biome biome;
    private BlockPos pos;

    @LuaFieldDoc(description = "biome.name")
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
    @LuaMethodDoc(description = "biome.get_pos")
    public FiguraVec3 getPos() {
        BlockPos pos = getBlockPos();
        return FiguraVec3.of(pos.getX(), pos.getY(), pos.getZ());
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
            description = "biome.set_pos"
    )
    public void setPos(Object x, Double y, Double z) {
        FiguraVec3 newPos = LuaUtils.parseVec3("setPos", x, y, z);
        pos = newPos.asBlockPos();
        newPos.free();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "biome.get_temperature")
    public float getTemperature() {
        return biome.getBaseTemperature();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "biome.get_precipitation")
    public String getPrecipitation() {
        return biome.getPrecipitation().name();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "biome.get_sky_color")
    public FiguraVec3 getSkyColor() {
        return ColorUtils.intToRGB(biome.getSkyColor());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "biome.get_foliage_color")
    public FiguraVec3 getFoliageColor() {
        return ColorUtils.intToRGB(biome.getFoliageColor());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "biome.get_grass_color")
    public FiguraVec3 getGrassColor() {
        BlockPos pos = getBlockPos();
        return ColorUtils.intToRGB(biome.getGrassColor(pos.getX(), pos.getY()));
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "biome.get_fog_color")
    public FiguraVec3 getFogColor() {
        return ColorUtils.intToRGB(biome.getFogColor());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "biome.get_water_color")
    public FiguraVec3 getWaterColor() {
        return ColorUtils.intToRGB(biome.getWaterColor());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "biome.get_water_fog_color")
    public FiguraVec3 getWaterFogColor() {
        return ColorUtils.intToRGB(biome.getWaterFogColor());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "biome.get_downfall")
    public float getDownfall() {
        return biome.getDownfall();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "biome.is_hot")
    public boolean isHot() {
        return biome.shouldSnowGolemBurn(getBlockPos());
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "biome.is_cold")
    public boolean isCold() {
        return biome.coldEnoughToSnow(getBlockPos());
    }
}
