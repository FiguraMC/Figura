package org.moon.figura.lua.api;

import com.mojang.brigadier.StringReader;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec6;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
@LuaTypeDoc(
        name = "ParticleAPI",
        description = "particle"
)
public class ParticleAPI {

    private final Avatar owner;

    public ParticleAPI(Avatar owner) {
        this.owner = owner;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {ParticleAPI.class, String.class, FiguraVec6.class},
                            argumentNames = {"api", "name", "posVel"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {ParticleAPI.class, String.class, FiguraVec3.class},
                            argumentNames = {"api", "name", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {ParticleAPI.class, String.class, FiguraVec3.class, FiguraVec3.class},
                            argumentNames = {"api", "name", "pos", "vel"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {ParticleAPI.class, String.class, Double.class, Double.class, Double.class},
                            argumentNames = {"api", "name", "posX", "posY", "posZ"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {ParticleAPI.class, String.class, FiguraVec3.class, Double.class, Double.class, Double.class},
                            argumentNames = {"api", "name", "pos", "velX", "velY", "velZ"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {ParticleAPI.class, String.class, Double.class, Double.class, Double.class, FiguraVec3.class},
                            argumentNames = {"api", "name", "posX", "posY", "posZ", "vel"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {ParticleAPI.class, String.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"api", "name", "posX", "posY", "posZ", "velX", "velY", "velZ"}
                    )
            },
            description = "particle.add_particle"
    )
    public static void addParticle(@LuaNotNil ParticleAPI api, @LuaNotNil String id, Object x, Object y, Double z, Object w, Double t, Double h) {
        if (!api.owner.particlesRemaining.use())
            return;

        FiguraVec3 pos, vel;

        //Parse pos and vel
        if (x instanceof FiguraVec3) {
            pos = ((FiguraVec3) x).copy();
            if (y instanceof FiguraVec3) {
                vel = ((FiguraVec3) y).copy();
            } else if (y == null || y instanceof Double) {
                //Intellij says: y should probably not be passed as parameter x
                //It really doesn't like the kind of programming that happens in this function lol
                vel = LuaUtils.oldParseVec3("addParticle", y, z, (Double) w);
            } else {
                throw new LuaRuntimeException("Illegal argument to addParticle(): " + y);
            }
        } else if (x == null || x instanceof Double) {
            pos = LuaUtils.oldParseVec3("addParticle", x, (Double) y, z);
            if (w instanceof FiguraVec3) {
                vel = ((FiguraVec3) w).copy();
            } else if (w == null || w instanceof Double) {
                vel = LuaUtils.oldParseVec3("addParticle", w, t, h);
            } else {
                throw new LuaRuntimeException("Illegal argument to addParticle(): " + w);
            }
        } else if (x instanceof FiguraVec6 posVel) {
            pos = FiguraVec3.of(posVel.x, posVel.y, posVel.z);
            vel = FiguraVec3.of(posVel.w, posVel.t, posVel.h);
        } else {
            throw new LuaRuntimeException("Illegal argument to addParticle(): " + x);
        }

        try {
            ParticleOptions particle = ParticleArgument.readParticle(new StringReader(id));
            Level level = WorldAPI.getCurrentWorld();

            if (!Minecraft.getInstance().isPaused() && level != null)
                level.addParticle(particle, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
        } catch (Exception e) {
            throw new LuaRuntimeException(e.getMessage());
        } finally {
            pos.free();
            vel.free();
        }
    }

    @Override
    public String toString() {
        return "ParticleAPI";
    }
}
