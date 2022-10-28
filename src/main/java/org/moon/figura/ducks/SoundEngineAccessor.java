package org.moon.figura.ducks;

import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.moon.figura.lua.api.sound.LuaSound;

import java.util.UUID;

public interface SoundEngineAccessor {

    void figura$addSound(LuaSound sound);
    void figura$stopSound(UUID owner, String name);
    void figura$stopAllSounds();
    ChannelAccess.ChannelHandle figura$createHandle(UUID owner, String name, Library.Pool pool);
    SoundBuffer figura$getBuffer(ResourceLocation id);
    float figura$getVolume(SoundSource category);
}
