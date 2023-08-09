package org.figuramc.figura.lua.api.sound;

import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.SoundEngineAccessor;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.mixin.sound.SoundManagerAccessor;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;

import java.util.Base64;
import java.util.Set;

@LuaWhitelist
@LuaTypeDoc(
        name = "SoundAPI",
        value = "sounds"
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
                    @LuaMethodOverload(
                            argumentTypes = {String.class, FiguraVec3.class},
                            argumentNames = {"sound", "pos"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class},
                            argumentNames = {"sound", "posX", "posY", "posZ"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, FiguraVec3.class, Double.class, Double.class, Boolean.class},
                            argumentNames = {"sound", "pos", "volume", "pitch", "loop"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class, Double.class, Double.class, Boolean.class},
                            argumentNames = {"sound", "posX", "posY", "posZ", "volume", "pitch", "loop"}
                    )
            },
            value = "sounds.play_sound"
    )
    public LuaSound playSound(@LuaNotNil String id, Object x, Double y, Double z, Object w, Double t, boolean loop) {
        LuaSound sound = __index(id);
        FiguraVec3 pos;
        float volume = 1f;
        float pitch = 1f;

        if (x instanceof FiguraVec3) {
            pos = ((FiguraVec3) x).copy();
            if (y != null) volume = y.floatValue();
            if (z != null) pitch = z.floatValue();
            if (w != null) {
                if (!(w instanceof Boolean))
                    throw new LuaError("Illegal argument to playSound(): " + w);
                loop = (boolean) w;
            }
        } else if (x == null || x instanceof Number) {
            pos = LuaUtils.parseVec3("playSound", x, y, z);
            if (w != null) {
                if (!(w instanceof Double))
                    throw new LuaError("Illegal argument to playSound(): " + w);
                volume = ((Double) w).floatValue();
            }
            if (t != null) pitch = t.floatValue();
        } else {
            throw new LuaError("Illegal argument to playSound(): " + x);
        }

        sound.pos(pos, null, null);
        sound.volume(volume);
        sound.pitch(pitch);
        sound.loop(loop);
        sound.play();

        return sound;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "id"
                    )
            },
            value = "sounds.stop_sound"
    )
    public SoundAPI stopSound(String id) {
        getSoundEngine().figura$stopSound(owner.owner, id);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {String.class, LuaTable.class},
                            argumentNames = {"name", "byteArray"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class},
                            argumentNames = {"name", "base64Text"}
                    )
            },
            value = "sounds.new_sound"
    )
    public SoundAPI newSound(@LuaNotNil String name, @LuaNotNil Object object) {
        byte[] bytes;
        if (object instanceof LuaTable table) {
            bytes = new byte[table.length()];
            for(int i = 0; i < bytes.length; i++)
                bytes[i] = (byte) table.get(i + 1).checkint();
        } else if (object instanceof String s) {
            bytes = Base64.getDecoder().decode(s);
        } else {
            throw new LuaError("Invalid type for newSound \"" + object.getClass().getSimpleName() + "\"");
        }

        try {
            owner.loadSound(name, bytes);
            return this;
        } catch (Exception e) {
            throw new LuaError("Failed to add custom sound \"" + name + "\"");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "id"
            ),
            value = "sounds.is_present"
    )
    public boolean isPresent(String id) {
        if (id == null)
            return false;
        if (owner.customSounds.get(id) != null)
            return true;
        try {
            return Minecraft.getInstance().getSoundManager().getSoundEvent(new ResourceLocation(id)) != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("sounds.get_custom_sounds")
    public Set<String> getCustomSounds() {
        return owner.customSounds.keySet();
    }

    @LuaWhitelist
    public LuaSound __index(String id) {
        SoundBuffer buffer = owner.customSounds.get(id);
        if (buffer != null) {
            if (owner.permissions.get(Permissions.CUSTOM_SOUNDS) == 1) {
                return new LuaSound(buffer, id, owner);
            } else {
                owner.noPermissions.add(Permissions.CUSTOM_SOUNDS);
            }
        }

        try {
            WeighedSoundEvents events = Minecraft.getInstance().getSoundManager().getSoundEvent(new ResourceLocation(id));
            if (events != null) {
                Sound sound = events.getSound(RandomSource.create(WorldAPI.getCurrentWorld().random.nextLong()));
                if (sound != SoundManager.EMPTY_SOUND) {
                    owner.noPermissions.remove(Permissions.CUSTOM_SOUNDS);
                    return new LuaSound(sound, id, events.getSubtitle(), owner);
                }
            }
            return new LuaSound(null, id, owner);
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "SoundAPI";
    }
}
