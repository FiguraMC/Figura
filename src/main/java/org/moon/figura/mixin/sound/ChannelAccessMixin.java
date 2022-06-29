package org.moon.figura.mixin.sound;

import net.minecraft.client.sounds.ChannelAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import java.util.concurrent.Executor;

@Mixin(ChannelAccess.class)
public interface ChannelAccessMixin {

    @Accessor("channels")
    Set<ChannelAccess.ChannelHandle> getChannels();

    @Accessor("executor")
    Executor getExecutor();
}
