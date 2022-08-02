package org.moon.figura.lua.api.particle;

import com.mojang.brigadier.StringReader;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import org.luaj.vm2.LuaError;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "ParticleBuilder",
        description = "particle_builder"
)
public class ParticleBuilder {

    private final Avatar owner;

    private String id;
    private FiguraVec3 pos, vel;

    public ParticleBuilder(Avatar owner) {
        this.owner = owner;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "id"
            ),
            description = "particle_builder.id"
    )
    public ParticleBuilder id(@LuaNotNil String id) {
        this.id = id;
        return this;
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
            description = "particle_builder.pos"
    )
    public ParticleBuilder pos(Object x, Double y, Double z) {
        this.pos = LuaUtils.parseVec3("pos", x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vel"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "particle_builder.vel"
    )
    public ParticleBuilder vel(Object x, Double y, Double z) {
        this.vel = LuaUtils.parseVec3("vel", x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "particle_builder.render")
    public void render() {
        if (id == null)
            throw new LuaError("Attempt to render empty particle");

        if (!owner.particlesRemaining.use())
            return;

        FiguraVec3 pos = this.pos == null ? FiguraVec3.of() : this.pos;
        FiguraVec3 vel = this.vel == null ? FiguraVec3.of() : this.vel;

        try {
            ParticleOptions particle = ParticleArgument.readParticle(new StringReader(id));
            Level level = WorldAPI.getCurrentWorld();

            if (!Minecraft.getInstance().isPaused() && level != null)
                level.addParticle(particle, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return id != null ? id + " (ParticleBuilder)" : "ParticleBuilder";
    }
}
