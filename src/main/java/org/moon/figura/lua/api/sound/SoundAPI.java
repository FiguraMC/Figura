package org.moon.figura.lua.api.sound;

import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.luaj.vm2.LuaError;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.ducks.SoundEngineAccessor;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.mixin.sound.SoundManagerAccessor;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "SoundAPI",
        description = "sound"
)
public class SoundAPI {

    private final Avatar owner;

    public SoundAPI(Avatar owner) {
        this.owner = owner;
    }

    public static SoundEngineAccessor getSoundEngine() {
        return (SoundEngineAccessor) ((SoundManagerAccessor) Minecraft.getInstance().getSoundManager()).getSoundEngine();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, FiguraVec3.class},
                            argumentNames = {"sound", "pos"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class},
                            argumentNames = {"sound", "posX", "posY", "posZ"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, FiguraVec3.class, Double.class, Double.class, Boolean.class},
                            argumentNames = {"sound", "pos", "volume", "pitch", "loop"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class, Double.class, Double.class, Boolean.class},
                            argumentNames = {"sound", "posX", "posY", "posZ", "volume", "pitch", "loop"}
                    )
            },
            description = "sound.play_sound"
    )
    public void playSound(@LuaNotNil String id, Object x, Double y, Double z, Object w, Double t, Boolean bl) {
        if (!owner.soundsRemaining.use())
            return;

        FiguraVec3 pos;
        double volume = 1.0;
        double pitch = 1.0;
        boolean loop = false;

        if (x instanceof FiguraVec3) {
            pos = ((FiguraVec3) x).copy();
            if (y != null) volume = y;
            if (z != null) pitch = z;
            if (w != null) {
                if (!(w instanceof Boolean))
                    throw new LuaError("Illegal argument to playSound(): " + w);
                loop = (boolean) w;
            }
        } else if (x == null || x instanceof Double) {
            pos = LuaUtils.parseVec3("playSound", x, y, z);
            if (w != null) {
                if (!(w instanceof Double))
                    throw new LuaError("Illegal argument to playSound(): " + w);
                volume = (double) w;
            }
            if (t != null) pitch = t;
            if (bl != null) loop = bl;
        } else {
            throw new LuaError("Illegal argument to playSound(): " + x);
        }

        //get and play the sound
        SoundBuffer buffer = owner.customSounds.get(id);
        if (buffer != null && owner.trust.get(TrustContainer.Trust.CUSTOM_SOUNDS) == 1) {
            getSoundEngine().figura$playCustomSound(owner.owner, id, buffer, pos.x, pos.y, pos.z, (float) volume, (float) pitch, loop);
        } else {
            try {
                SoundEvent event = new SoundEvent(new ResourceLocation(id));
                SimpleSoundInstance instance = new SimpleSoundInstance(event, SoundSource.PLAYERS, (float) volume, (float) pitch, RandomSource.create(WorldAPI.getCurrentWorld().random.nextLong()), pos.x, pos.y, pos.z);
                getSoundEngine().figura$playSound(owner.owner, id, instance, loop);
            } catch (Exception ignored) {}
        }

        pos.free();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload,
                    @LuaFunctionOverload(
                            argumentTypes = String.class,
                            argumentNames = "id"
                    )
            },
            description = "sound.stop_sound"
    )
    public void stopSound(String id) {
        getSoundEngine().figura$stopSound(owner.owner, id);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "sound.new_sound")
    public SoundBuilder newSound() {
        return new SoundBuilder(owner);
    }

    @Override
    public String toString() {
        return "SoundAPI";
    }
}
