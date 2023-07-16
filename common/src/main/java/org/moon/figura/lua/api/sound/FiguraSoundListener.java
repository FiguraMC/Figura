package org.moon.figura.lua.api.sound;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundSource;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.math.vector.FiguraVec3;

public class FiguraSoundListener implements SoundEventListener {
    @Override
    public void onPlaySound(SoundInstance sound, WeighedSoundEvents soundSet) {
        String id = sound.getLocation().toString();
        String file = sound.getSound().getLocation().toString();
        FiguraVec3 pos = FiguraVec3.of(sound.getX(), sound.getY(), sound.getZ());
        AvatarManager.executeAll("playSoundEvent", avatar -> avatar.playSoundEvent(id, pos, sound.getVolume(), sound.getPitch(), sound.isLooping(), sound.getSource().name(), file));
    }

    public void figuraPlaySound(LuaSound sound) {
        AvatarManager.executeAll("playSoundEvent", avatar -> avatar.playSoundEvent(sound.getId(), sound.getPos(), sound.getVolume(), sound.getPitch(), sound.isLooping(), SoundSource.PLAYERS.name(), null));
    }
}
