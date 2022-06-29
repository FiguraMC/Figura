package org.moon.figura.mixin.sound;

import com.mojang.blaze3d.audio.Library;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundEngineExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SoundEngine.class)
public interface SoundEngineAccessor {

    @Accessor("library")
    Library getLibrary();

    @Accessor("executor")
    SoundEngineExecutor getExecutor();

    @Accessor("soundBuffers")
    SoundBufferLibrary getSoundBuffers();
}
