package org.moon.figura.lua.api.world;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import org.moon.figura.lua.LuaWhitelist;

import java.lang.ref.WeakReference;

@LuaWhitelist
public class BiomeWrapper {

    private final WeakReference<Biome> wrappedBiome;

    public BiomeWrapper(Biome biome) {
        this.wrappedBiome = new WeakReference<>(biome);
        name = Minecraft.getInstance().level.registryAccess().registry(Registry.BIOME_REGISTRY).get().getKey(biome).toString();
    }

    @LuaWhitelist
    public final String name;





}
