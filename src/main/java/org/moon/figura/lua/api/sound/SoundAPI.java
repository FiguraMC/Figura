package org.moon.figura.lua.api.sound;

import net.minecraft.client.Minecraft;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.ducks.SoundEngineAccessor;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMetamethodDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.mixin.sound.SoundManagerAccessor;
import org.moon.figura.trust.TrustContainer;

import java.util.HashMap;
import java.util.Map;

@LuaWhitelist
@LuaTypeDoc(
        name = "SoundAPI",
        description = "sounds"
)
public class SoundAPI {

    private final Avatar owner;
    private final Map<String, LuaSound> luaSounds;

    public SoundAPI(Avatar owner) {
        this.owner = owner;
        luaSounds = new HashMap<>();
    }

    public static SoundEngineAccessor getSoundEngine() {
        return (SoundEngineAccessor) ((SoundManagerAccessor) Minecraft.getInstance().getSoundManager()).getSoundEngine();
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {LuaSound.class, String.class}
            )
    )
    public LuaSound __index(String id) {
        LuaSound sound = luaSounds.computeIfAbsent(id, str -> new LuaSound(str, owner));
        if (sound.custom && owner.trust.get(TrustContainer.Trust.CUSTOM_SOUNDS) != 1) {
            luaSounds.remove(id);
            sound = new LuaSound(id, owner);
        }
        return sound;
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
            description = "sounds.play_sound"
    )
    public void playSound(@LuaNotNil String id, Object x, Double y, Double z, Object w, Double t, Boolean bl) {
        __index(id).play(x, y, z, w, t, bl);
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
            description = "sounds.stop_sound"
    )
    public void stopSound(String id) {
        getSoundEngine().figura$stopSound(owner.owner, id);
    }

    @Override
    public String toString() {
        return "SoundAPI";
    }
}
