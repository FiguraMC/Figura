package org.figuramc.figura.lua.api.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.mixin.BiomeAccessor;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.LuaUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@LuaWhitelist
@LuaTypeDoc(
        name = "Biome",
        value = "biome"
)
public class BiomeAPI {

    private final Biome biome;
    private BlockPos pos;

    @LuaWhitelist
    @LuaFieldDoc("biome.id")
    public final String id;

    public BiomeAPI(Biome biome, BlockPos pos) {
        this.biome = biome;
        this.pos = pos;
        this.id = WorldAPI.getCurrentWorld().registryAccess().registry(Registries.BIOME).get().getKey(biome).toString();
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
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "pos",
            value = "biome.set_pos"
    )
    public BiomeAPI setPos(Object x, Double y, Double z) {
        FiguraVec3 newPos = LuaUtils.parseVec3("setPos", x, y, z);
        pos = newPos.asBlockPos();
        return this;
    }

    @LuaWhitelist
    public BiomeAPI pos(Object x, Double y, Double z) {
        return setPos(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_tags")
    public List<String> getTags() {
        List<String> list = new ArrayList<>();

        Registry<Biome> registry = WorldAPI.getCurrentWorld().registryAccess().registryOrThrow(Registries.BIOME);
        Optional<ResourceKey<Biome>> key = registry.getResourceKey(biome);

        if (key.isEmpty())
            return list;

        for (TagKey<Biome> biomeTagKey : registry.getHolderOrThrow(key.get()).tags().toList())
            list.add(biomeTagKey.location().toString());

        return list;
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_temperature")
    public float getTemperature() {
        return biome.getBaseTemperature();
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.get_precipitation")
    public String getPrecipitation() {
        return biome.getPrecipitationAt(getBlockPos()).name();
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
        return ((BiomeAccessor) (Object) biome).getClimateSettings().downfall();
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.is_hot")
    public boolean isHot() {
        return ((BiomeAccessor) (Object) biome).getTheTemperature(getBlockPos()) > 1f;
    }

    @LuaWhitelist
    @LuaMethodDoc("biome.is_cold")
    public boolean isCold() {
        return biome.coldEnoughToSnow(getBlockPos());
    }

    @LuaWhitelist
    public boolean __eq(BiomeAPI other) {
        return this.biome.equals(other.biome);
    }

    @LuaWhitelist
    public Object __index(String arg) {
        return "id".equals(arg) ? id : null;
    }

    @Override
    public String toString() {
        return id + " (Biome)";
    }
}
