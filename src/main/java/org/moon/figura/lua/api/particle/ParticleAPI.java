package org.moon.figura.lua.api.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import org.luaj.vm2.LuaError;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.ducks.ParticleEngineAccessor;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "ParticleAPI",
        value = "particles"
)
public class ParticleAPI {

    private final Avatar owner;

    public ParticleAPI(Avatar owner) {
        this.owner = owner;
    }

    public static ParticleEngineAccessor getParticleEngine() {
        return (ParticleEngineAccessor) Minecraft.getInstance().particleEngine;
    }

    private LuaParticle generate(String id, double x, double y, double z, double w, double t, double h) {
        try {
            ParticleOptions options = ParticleArgument.readParticle(new StringReader(id));
            Particle p = getParticleEngine().figura$makeParticle(options, x, y, z, w, t, h);
            if (p == null) throw new LuaError("Could not parse particle \"" + id + "\"");
            return new LuaParticle(id, p, owner);
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {String.class, FiguraVec3.class},
                            argumentNames = {"name", "pos"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, FiguraVec3.class, FiguraVec3.class},
                            argumentNames = {"name", "pos", "vel"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class},
                            argumentNames = {"name", "posX", "posY", "posZ"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, FiguraVec3.class, Double.class, Double.class, Double.class},
                            argumentNames = {"name", "pos", "velX", "velY", "velZ"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class, FiguraVec3.class},
                            argumentNames = {"name", "posX", "posY", "posZ", "vel"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"name", "posX", "posY", "posZ", "velX", "velY", "velZ"}
                    )
            },
            value = "particles.new_particle"
    )
    public LuaParticle newParticle(@LuaNotNil String id, Object x, Object y, Double z, Object w, Double t, Double h) {
        FiguraVec3 pos, vel;

        //Parse pos and vel
        Pair<FiguraVec3, FiguraVec3> pair = LuaUtils.parse2Vec3("newParticle", x, y, z, w, t, h);
        pos = pair.getFirst();
        vel = pair.getSecond();

        LuaParticle particle = generate(id, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
        particle.spawn();

        pos.free();
        vel.free();

        return particle;
    }

    @LuaWhitelist
    @LuaMethodDoc("particles.remove_particles")
    public ParticleAPI removeParticles() {
        getParticleEngine().figura$clearParticles(owner.owner);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "id"
            ),
            value = "particles.is_present"
    )
    public boolean isPresent(String id) {
        try {
            ParticleOptions options = ParticleArgument.readParticle(new StringReader(id), BuiltInRegistries.PARTICLE_TYPE.asLookup());
            return getParticleEngine().figura$makeParticle(options, 0, 0, 0, 0, 0, 0) != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    @LuaWhitelist
    public LuaParticle __index(String id) {
        return generate(id, 0, 0, 0, 0, 0, 0);
    }

    @Override
    public String toString() {
        return "ParticleAPI";
    }
}
