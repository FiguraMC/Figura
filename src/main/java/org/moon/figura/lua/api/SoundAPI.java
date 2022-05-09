package org.moon.figura.lua.api;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
@LuaTypeDoc(
        name = "SoundAPI",
        description = "sound"
)
public class SoundAPI {

    public static final SoundAPI INSTANCE = new SoundAPI();

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
                            argumentTypes = {String.class, FiguraVec3.class, Double.class, Double.class},
                            argumentNames = {"sound", "pos", "volume", "pitch"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"sound", "posX", "posY", "posZ", "volume", "pitch"}
                    )
            },
            description = "sound.play_sound"
    )
    public static void playSound(String id, Object x, Double y, Double z, Double w, Double t) {
        FiguraVec3 pos;
        double volume = 1.0;
        double pitch = 1.0;

        if (x instanceof FiguraVec3) {
            pos = ((FiguraVec3) x).copy();
            if (y != null) volume = y;
            if (z != null) pitch = z;
        } else if (x == null || x instanceof Double) {
            pos = LuaUtils.parseVec3("playSound", x, y, z);
            if (w != null) volume = w;
            if (t != null) pitch = t;
        } else {
            throw new LuaRuntimeException("Illegal argument to playSound(): " + x);
        }

        Level level = WorldAPI.getCurrentWorld();
        if (Minecraft.getInstance().isPaused() || level == null)
            return;

        SoundEvent targetEvent = new SoundEvent(new ResourceLocation(id));
        level.playLocalSound(
                pos.x, pos.y, pos.z,
                targetEvent, SoundSource.PLAYERS,
                (float) volume, (float) pitch, true
        );
        pos.free();
    }

    @Override
    public String toString() {
        return "SoundAPI";
    }
}
