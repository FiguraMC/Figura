package org.moon.figura.lua.api.sound;

import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.luaj.vm2.LuaError;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "SoundBuilder",
        description = "sound_builder"
)
public class SoundBuilder {

    private final Avatar owner;

    private String id;
    private FiguraVec3 pos;
    private double pitch = 1d;
    private double volume = 1d;
    private boolean loop = false;

    public SoundBuilder(Avatar owner) {
        this.owner = owner;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "id"
            ),
            description = "sound_builder.id"
    )
    public SoundBuilder id(@LuaNotNil String id) {
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
            description = "sound_builder.pos"
    )
    public SoundBuilder pos(Object x, Double y, Double z) {
        this.pos = LuaUtils.parseVec3("pos", x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Double.class,
                    argumentNames = "pitch"
            ),
            description = "sound_builder.pitch"
    )
    public SoundBuilder pitch(@LuaNotNil Double pitch) {
        this.pitch = pitch;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Double.class,
                    argumentNames = "volume"
            ),
            description = "sound_builder.volume"
    )
    public SoundBuilder volume(@LuaNotNil Double volume) {
        this.volume = volume;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "loop"
            ),
            description = "sound_builder.loop"
    )
    public SoundBuilder loop(@LuaNotNil Boolean loop) {
        this.loop = loop != null && loop;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "sound_builder.play")
    public void play() {
        if (id == null)
            throw new LuaError("Attempt to play empty sound");

        if (!owner.soundsRemaining.use())
            return;

        FiguraVec3 pos = this.pos == null ? FiguraVec3.of() : this.pos;
        //get and play the sound
        SoundBuffer buffer = owner.customSounds.get(id);
        if (buffer != null && owner.trust.get(TrustContainer.Trust.CUSTOM_SOUNDS) == 1) {
            SoundAPI.getSoundEngine().figura$playCustomSound(owner.owner, id, buffer, pos.x, pos.y, pos.z, (float) volume, (float) pitch, loop);
        } else {
            try {
                SoundEvent event = new SoundEvent(new ResourceLocation(id));
                SimpleSoundInstance instance = new SimpleSoundInstance(event, SoundSource.PLAYERS, (float) volume, (float) pitch, RandomSource.create(WorldAPI.getCurrentWorld().random.nextLong()), pos.x, pos.y, pos.z);
                SoundAPI.getSoundEngine().figura$playSound(owner.owner, id, instance, loop);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public String toString() {
        return id != null ? id + " (SoundBuilder)" : "SoundBuilder";
    }
}
