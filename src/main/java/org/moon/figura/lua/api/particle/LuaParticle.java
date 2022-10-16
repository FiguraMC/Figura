package org.moon.figura.lua.api.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.mixin.particle.ParticleAccessor;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "Particle",
        value = "particle"
)
public class LuaParticle {

    private final Avatar owner;
    private final Particle particle;

    public LuaParticle(Particle particle, Avatar owner) {
        this.particle = particle;
        this.owner = owner;
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.spawn")
    public LuaParticle spawn() {
        if (!Minecraft.getInstance().isPaused() && owner.particlesRemaining.use())
            ParticleAPI.getParticleEngine().figura$spawnParticle(particle, owner.owner);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.remove")
    public void remove() {
        particle.remove();
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.is_alive")
    public boolean isAlive() {
        return particle.isAlive();
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.get_lifetime")
    public int getLifetime() {
        return particle.getLifetime();
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
            value = "particle.pos")
    public LuaParticle pos(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("pos", x, y, z);
        particle.setPos(vec.x, vec.y, vec.z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "velocity"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "particle.velocity")
    public LuaParticle velocity(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("velocity", x, y, z);
        particle.setParticleSpeed(vec.x, vec.y, vec.z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rgb"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec4.class,
                            argumentNames = "rgba"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b", "a"}
                    )
            },
            value = "particle.color")
    public LuaParticle color(Object r, Double g, Double b, Double a) {
        FiguraVec4 vec = LuaUtils.parseVec4("color", r, g, b, a, 1, 1, 1, 1);
        particle.setColor((float) vec.x, (float) vec.y, (float) vec.z);
        ((ParticleAccessor) particle).setParticleAlpha((float) vec.w);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "lifetime"
            ),
            value = "particle.lifetime")
    public LuaParticle lifetime(int age) {
        particle.setLifetime(age);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "power"
            ),
            value = "particle.power")
    public LuaParticle power(float power) {
        particle.setPower(power);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "scale"
            ),
            value = "particle.scale")
    public LuaParticle scale(float scale) {
        particle.scale(scale);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "gravity"
            ),
            value = "particle.gravity")
    public LuaParticle gravity(float gravity) {
        ((ParticleAccessor) particle).setGravity(gravity);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "physics"
            ),
            value = "particle.physics")
    public LuaParticle physics(boolean physics) {
        ((ParticleAccessor) particle).setHasPhysics(physics);
        return this;
    }

    public String toString() {
        return particle.getClass().getSimpleName() + " (Particle)";
    }
}
