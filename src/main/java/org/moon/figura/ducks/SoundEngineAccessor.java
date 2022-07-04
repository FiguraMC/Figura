package org.moon.figura.ducks;

import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;

import java.util.UUID;

public interface SoundEngineAccessor {

    void figura$playCustomSound(UUID owner, String name, SoundBuffer buffer, double x, double y, double z, float volume, float pitch, boolean loop);
    void figura$playSound(UUID owner, String name, SoundInstance instance, boolean loop);
    void figura$stopSound(UUID owner, String name);
    void figura$stopAllSounds();
    ChannelAccess.ChannelHandle figura$createHandle(UUID owner, String name, Library.Pool pool);
}
