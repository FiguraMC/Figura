package org.moon.figura.lua.api.sound;


import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.luaj.vm2.LuaError;
import org.moon.figura.avatars.Avatar;
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
        name = "Sound",
        description = "sound"
)
public class LuaSound {

    public final boolean custom;
    private final Avatar owner;
    private final String id;
    private final SoundBuffer buffer; //For custom sounds
    private final SoundEvent event; //For vanilla sounds

    public LuaSound(String id, Avatar owner) {
        this.owner = owner;
        this.id = id;
        buffer = owner.customSounds.get(id);
        if (buffer != null && owner.trust.get(TrustContainer.Trust.CUSTOM_SOUNDS) == 1) {
            custom = true;
            event = null;
            return;
        }
        custom = false;
        event = new SoundEvent(new ResourceLocation(id));
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
                            argumentNames = {"posX", "posY", "posZ"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraVec3.class, Double.class, Double.class, Boolean.class},
                            argumentNames = {"pos", "volume", "pitch", "loop"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class, Boolean.class},
                            argumentNames = {"posX", "posY", "posZ", "volume", "pitch", "loop"}
                    )
            },
            description = "sound.play"
    )
    public void play(Object x, Double y, Double z, Object w, Double t, Boolean bl) {
        if (!owner.soundsRemaining.use())
            return;
        if (custom && owner.trust.get(TrustContainer.Trust.CUSTOM_SOUNDS) != 1)
            return;

        FiguraVec3 pos;
        float volume = 1.0f;
        float pitch = 1.0f;
        boolean loop = false;

        if (x instanceof FiguraVec3) {
            pos = ((FiguraVec3) x).copy();
            if (y != null) volume = y.floatValue();
            if (z != null) pitch = z.floatValue();
            if (w != null) {
                if (!(w instanceof Boolean))
                    throw new LuaError("Illegal argument to play(): " + w);
                loop = (boolean) w;
            }
        } else if (x == null || x instanceof Double) {
            pos = LuaUtils.parseVec3("play", x, y, z);
            if (w != null) {
                if (!(w instanceof Double))
                    throw new LuaError("Illegal argument to playSound(): " + w);
                volume = ((Double) w).floatValue();
            }
            if (t != null) pitch = t.floatValue();
            if (bl != null) loop = bl;
        } else {
            throw new LuaError("Illegal argument to playSound(): " + x);
        }

        if (custom) {
            SoundAPI.getSoundEngine().figura$playCustomSound(
                    owner.owner,
                    id,
                    buffer,
                    pos.x, pos.y, pos.z,
                    volume, pitch,
                    loop);
        } else {
            try {
                SimpleSoundInstance instance = new SimpleSoundInstance(
                        event, SoundSource.PLAYERS,
                        volume, pitch,
                        RandomSource.create(WorldAPI.getCurrentWorld().random.nextLong()),
                        pos.x, pos.y, pos.z);

                SoundAPI.getSoundEngine().figura$playSound(
                        owner.owner, id, instance, loop
                );
            } catch (Exception ignored) {}
        }
        pos.free();
    }

}
