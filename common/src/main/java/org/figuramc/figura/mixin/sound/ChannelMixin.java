package org.figuramc.figura.mixin.sound;

import com.mojang.blaze3d.audio.Channel;
import org.figuramc.figura.ducks.ChannelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Channel.class)
public abstract class ChannelMixin implements ChannelAccessor {
    @Shadow protected abstract int getState();

    @Override
    public int figura$getState() {
        return getState();
    }

    @Override
    public boolean figura$isPlaying() {
        return getState() == 4114;
    }
}
