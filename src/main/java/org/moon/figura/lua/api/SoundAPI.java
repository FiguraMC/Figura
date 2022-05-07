package org.moon.figura.lua.api;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;

@LuaWhitelist
public class SoundAPI {

    public static final SoundAPI INSTANCE = new SoundAPI();

    @LuaWhitelist
    public static void playSound(String id, FiguraVec3 pos, FiguraVec2 volPitch) {
        if (volPitch == null) volPitch = FiguraVec2.of(1, 1);

        Level level = WorldAPI.getCurrentWorld();
        if (Minecraft.getInstance().isPaused() || level == null)
            return;

        SoundEvent targetEvent = new SoundEvent(new ResourceLocation(id));
        level.playLocalSound(
                pos.x, pos.y, pos.z,
                targetEvent, SoundSource.PLAYERS,
                (float) volPitch.x, (float) volPitch.y, true
        );
    }
}
