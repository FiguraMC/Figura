package org.moon.figura.lua.api.particle;

import com.mojang.brigadier.StringReader;
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
import org.moon.figura.math.vector.FiguraVec6;
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
            return new LuaParticle(p, owner);
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {String.class, FiguraVec6.class},
                            argumentNames = {"name", "posVel"}
                    ),
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
            value = "particles.add_particle"
    )
    public void addParticle(@LuaNotNil String id, Object x, Object y, Double z, Object w, Double t, Double h) {
        FiguraVec3 pos, vel;

        //Parse pos and vel
        if (x instanceof FiguraVec6 posVel) {
            pos = FiguraVec3.of(posVel.x, posVel.y, posVel.z);
            vel = FiguraVec3.of(posVel.w, posVel.t, posVel.h);
        } else if (x instanceof FiguraVec3 vec1) {
            pos = vec1.copy();
            if (y instanceof FiguraVec3 vec2) {
                vel = vec2.copy();
            } else if (y == null || y instanceof Number) {
                if (w == null || w instanceof Number) {
                    //Intellij says: y should probably not be passed as parameter x
                    //It really doesn't like the kind of programming that happens in this function lol
                    vel = LuaUtils.parseVec3("addParticle", y, z, (Number) w);
                } else {
                    throw new LuaError("Illegal argument to addParticle(): " + w);
                }
            } else {
                throw new LuaError("Illegal argument to addParticle(): " + y);
            }
        } else if (x == null || x instanceof Number && y == null || y instanceof Number) {
            pos = LuaUtils.parseVec3("addParticle", x, (Number) y, z);
            if (w instanceof FiguraVec3 vec1) {
                vel = vec1.copy();
            } else if (w == null || w instanceof Number) {
                vel = LuaUtils.parseVec3("addParticle", w, t, h);
            } else {
                throw new LuaError("Illegal argument to addParticle(): " + w);
            }
        } else {
            throw new LuaError("Illegal argument to addParticle(): " + x);
        }

        LuaParticle particle = generate(id, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
        particle.spawn();

        pos.free();
        vel.free();
    }

    @LuaWhitelist
    @LuaMethodDoc("particles.remove_particles")
    public void removeParticles() {
        getParticleEngine().figura$clearParticles(owner.owner);
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
