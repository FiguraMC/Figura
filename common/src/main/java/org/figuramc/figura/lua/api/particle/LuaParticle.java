package org.figuramc.figura.lua.api.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.WakeParticle;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.SingleQuadParticleAccessor;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.mixin.particle.ParticleAccessor;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "Particle",
        value = "particle"
)
public class LuaParticle {

    private final String name;
    private final Avatar owner;
    private final Particle particle;

    private float power;

    public LuaParticle(String name, Particle particle, Avatar owner) {
        this.name = name;
        this.particle = particle;
        this.owner = owner;
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.spawn")
    public LuaParticle spawn() {
        if (!Minecraft.getInstance().isPaused()) {
            if (owner.particlesRemaining.use()) {
                ParticleAPI.getParticleEngine().figura$spawnParticle(particle, owner.owner);
                owner.noPermissions.remove(Permissions.PARTICLES);
            } else {
                owner.noPermissions.add(Permissions.PARTICLES);
            }
        }
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.remove")
    public LuaParticle remove() {
        particle.remove();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.is_alive")
    public boolean isAlive() {
        return particle.isAlive();
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.get_pos")
    public FiguraVec3 getPos() {
        ParticleAccessor p = (ParticleAccessor) particle;
        return FiguraVec3.of(p.getX(), p.getY(), p.getZ());
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
            value = "particle.set_pos")
    public LuaParticle setPos(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setPos", x, y, z);
        particle.setPos(vec.x, vec.y, vec.z);

        ParticleAccessor p = (ParticleAccessor) particle;
        p.setXo(vec.x);
        p.setYo(vec.y);
        p.setZo(vec.z);

        return this;
    }

    @LuaWhitelist
    public LuaParticle pos(Object x, Double y, Double z) {
        return setPos(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.get_velocity")
    public FiguraVec3 getVelocity() {
        ParticleAccessor p = (ParticleAccessor) particle;
        return FiguraVec3.of(p.getXd(), p.getYd(), p.getZd());
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
            aliases = "velocity",
            value = "particle.set_velocity")
    public LuaParticle setVelocity(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setVelocity", x, y, z);
        particle.setParticleSpeed(vec.x, vec.y, vec.z);
        return this;
    }

    @LuaWhitelist
    public LuaParticle velocity(Object x, Double y, Double z) {
        return setVelocity(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.get_color")
    public FiguraVec4 getColor() {
        ParticleAccessor p = (ParticleAccessor) particle;
        return FiguraVec4.of(p.getRCol(), p.getGCol(), p.getBCol(), p.getAlpha());
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
            aliases = "color",
            value = "particle.set_color")
    public LuaParticle setColor(Object r, Double g, Double b, Double a) {
        FiguraVec4 vec = LuaUtils.parseVec4("setColor", r, g, b, a, 1, 1, 1, 1);
        particle.setColor((float) vec.x, (float) vec.y, (float) vec.z);
        ((ParticleAccessor) particle).setParticleAlpha((float) vec.w);
        return this;
    }

    @LuaWhitelist
    public LuaParticle color(Object r, Double g, Double b, Double a) {
        return setColor(r, g, b, a);
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.get_lifetime")
    public int getLifetime() {
        return particle.getLifetime();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "lifetime"
            ),
            aliases = "lifetime",
            value = "particle.set_lifetime")
    public LuaParticle setLifetime(int age) {
        particle.setLifetime(Math.max(particle instanceof WakeParticle ? Math.min(age, 60) : age, 0));
        return this;
    }

    @LuaWhitelist
    public LuaParticle lifetime(int age) {
        return setLifetime(age);
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.get_power")
    public float getPower() {
        return power;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "power"
            ),
            aliases = "power",
            value = "particle.set_power")
    public LuaParticle setPower(float power) {
        particle.setPower(power);
        this.power = power;
        return this;
    }

    @LuaWhitelist
    public LuaParticle power(float power) {
        return setPower(power);
    }

    @LuaWhitelist
    @LuaMethodDoc(aliases = "getSize", value = "particle.get_scale")
    public float getScale() {
        return ((ParticleAccessor) particle).getBbWidth() / 0.2f;
    }

    @LuaWhitelist
    public float getSize() {
        return getScale();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "scale"
            ),
            aliases = {"scale", "setSize", "size"},
            value = "particle.set_scale")
    public LuaParticle setScale(float scale) {
        if (particle instanceof SingleQuadParticle quadParticle)
            ((SingleQuadParticleAccessor) quadParticle).figura$fixQuadSize();
        particle.scale(scale);
        return this;
    }

    @LuaWhitelist
    public LuaParticle scale(float scale) {
        return setScale(scale);
    }

    @LuaWhitelist
    public LuaParticle setSize(float scale) {
        return setScale(scale);
    }

    @LuaWhitelist
    public LuaParticle size(float scale) {
        return setScale(scale);
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.get_gravity")
    public float getGravity() {
        return ((ParticleAccessor) particle).getGravity();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "gravity"
            ),
            aliases = "gravity",
            value = "particle.set_gravity")
    public LuaParticle setGravity(float gravity) {
        ((ParticleAccessor) particle).setGravity(gravity);
        return this;
    }

    @LuaWhitelist
    public LuaParticle gravity(float gravity) {
        return setGravity(gravity);
    }

    @LuaWhitelist
    @LuaMethodDoc("particle.has_physics")
    public boolean hasPhysics() {
        return ((ParticleAccessor) particle).getHasPhysics();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "physics"
            ),
            aliases = "physics",
            value = "particle.set_physics")
    public LuaParticle setPhysics(boolean physics) {
        ((ParticleAccessor) particle).setHasPhysics(physics);
        return this;
    }

    @LuaWhitelist
    public LuaParticle physics(boolean physics) {
        return setPhysics(physics);
    }

    public String toString() {
        return name + " (Particle)";
    }
}
