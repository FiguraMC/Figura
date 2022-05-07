package org.moon.figura.lua.api;

import com.mojang.brigadier.StringReader;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.math.vector.FiguraVec3;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
public class ParticleAPI {

    public static final ParticleAPI INSTANCE = new ParticleAPI();

    @LuaWhitelist
    public static void addParticle(String id, FiguraVec3 pos, FiguraVec3 dir) {
        if (dir == null)
            dir = FiguraVec3.of(0, 0, 0);

        try {
            ParticleOptions particle = ParticleArgument.readParticle(new StringReader(id));
            Level level = WorldAPI.getCurrentWorld();

            if (!Minecraft.getInstance().isPaused() && level != null)
                level.addParticle(particle, pos.x, pos.y, pos.z, dir.x, dir.y, dir.z);
        } catch (Exception e) {
            throw new LuaRuntimeException(e.getMessage());
        }
    }
}
