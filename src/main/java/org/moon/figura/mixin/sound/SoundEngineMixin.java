package org.moon.figura.mixin.sound;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.Listener;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.moon.figura.ducks.ChannelHandleAccessor;
import org.moon.figura.ducks.SoundEngineAccessor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.UUID;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin implements SoundEngineAccessor {

    @Shadow @Final private Library library;
    @Shadow @Final private SoundEngineExecutor executor;
    @Shadow @Final private SoundManager soundManager;
    @Shadow @Final private SoundBufferLibrary soundBuffers;
    @Shadow @Final private Listener listener;
    @Shadow private boolean loaded;

    @Shadow protected abstract float getVolume(@Nullable SoundSource category);

    @Unique
    private ChannelAccess figuraChannel;
    @Unique
    private final HashMap<ChannelAccess.ChannelHandle, Float> figuraHandlers = new HashMap<>();

    @Inject(at = @At("RETURN"), method = "<init>")
    private void soundEngineInit(SoundManager soundManager, Options options, ResourceManager resourceManager, CallbackInfo ci) {
        figuraChannel = new ChannelAccess(this.library, this.executor);
    }

    @Inject(at = @At("RETURN"), method = "tick")
    private void tick(boolean bl, CallbackInfo ci) {
        figuraChannel.scheduleTick();
    }

    @Inject(at = @At("RETURN"), method = "stopAll")
    private void stopAll(CallbackInfo ci) {
        figura$stopAllSounds();
    }

    @Inject(at = @At("RETURN"), method = "pause")
    private void pause(CallbackInfo ci) {
        if (this.loaded) figuraChannel.executeOnChannels(stream -> stream.forEach(Channel::pause));
    }

    @Inject(at = @At("RETURN"), method = "resume")
    private void resume(CallbackInfo ci) {
        if (this.loaded) figuraChannel.executeOnChannels(stream -> stream.forEach(Channel::unpause));
    }

    @Inject(at = @At("RETURN"), method = "updateCategoryVolume")
    private void updateCategoryVolume(SoundSource category, float volume, CallbackInfo ci) {
        if (!this.loaded || category != SoundSource.PLAYERS)
            return;

        figuraHandlers.forEach((channelHandle, volume1) -> {
            float newVol = Math.min(volume1 * this.getVolume(category), 1);
            channelHandle.execute(channel -> {
                if (newVol <= 0) channel.stop();
                else channel.setVolume(newVol);
            });
        });
    }

    @Inject(at = @At("RETURN"), method = "stop(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/sounds/SoundSource;)V")
    private void stop(ResourceLocation id, SoundSource category, CallbackInfo ci) {
        if (category == SoundSource.PLAYERS)
            figura$stopAllSounds();
    }

    @Override @Intrinsic
    public void figura$playCustomSound(UUID owner, String name, SoundBuffer buffer, double x, double y, double z, float volume, float pitch, boolean loop) {
        if (!this.loaded || this.listener.getGain() <= 0)
            return;

        //volume
        float finalVol = Math.min(volume * this.getVolume(SoundSource.PLAYERS), 1);
        if (finalVol <= 0f || pitch < 0f)
            return;

        //create handle
        ChannelAccess.ChannelHandle handle = figura$createHandle(owner, name, Library.Pool.STATIC);
        if (handle == null)
            return;

        //engine data
        figuraHandlers.put(handle, volume);

        //sound properties and play
        float finalAttenuation = Math.max(volume, 1f) * 16f;
        Vec3 pos = new Vec3(x, y, z);
        handle.execute(channel -> {
            channel.setPitch(pitch);
            channel.setVolume(finalVol);
            channel.linearAttenuation(finalAttenuation);
            channel.setLooping(loop);
            channel.setSelfPosition(pos);
            channel.setRelative(false);
            channel.attachStaticBuffer(buffer);
            channel.play();
        });
    }

    @Override @Intrinsic
    public void figura$playSound(UUID owner, String name, SoundInstance instance, boolean loop) {
        if (!this.loaded || !instance.canPlaySound() || this.listener.getGain() <= 0 || instance.resolve(this.soundManager) == null)
            return;

        Sound sound = instance.getSound();
        if (sound == SoundManager.EMPTY_SOUND)
            return;

        //volume
        float vol = instance.getVolume();
        SoundSource source = instance.getSource();
        float finalVol = Math.min(vol * this.getVolume(source), 1);
        float finalPitch = instance.getPitch();
        if ((finalVol <= 0f && !instance.canStartSilent()) || finalPitch <= 0f)
            return;

        //create handle
        boolean shouldStream = sound.shouldStream();
        ChannelAccess.ChannelHandle handle = figura$createHandle(owner, name, shouldStream ? Library.Pool.STREAMING : Library.Pool.STATIC);
        if (handle == null)
            return;

        //engine data
        figuraHandlers.put(handle, vol);

        //sound properties
        float attenuation = Math.max(vol, 1f) * sound.getAttenuationDistance();
        Vec3 pos = new Vec3(instance.getX(), instance.getY(), instance.getZ());
        boolean relative = instance.isRelative();
        handle.execute(channel -> {
            channel.setPitch(finalPitch);
            channel.setVolume(finalVol);
            channel.linearAttenuation(attenuation);
            channel.setLooping(loop && !shouldStream);
            channel.setSelfPosition(pos);
            channel.setRelative(relative);
        });

        //append sound data then play
        ResourceLocation resourceLocation = sound.getPath();
        if (!shouldStream) {
            this.soundBuffers.getCompleteBuffer(resourceLocation).thenAccept(buffer -> handle.execute(channel -> {
                channel.attachStaticBuffer(buffer);
                channel.play();
            }));
        } else {
            this.soundBuffers.getStream(resourceLocation, loop).thenAccept(stream -> handle.execute(channel -> {
                channel.attachBufferStream(stream);
                channel.play();
            }));
        }
    }

    @Override @Intrinsic
    public void figura$stopSound(UUID owner, String name) {
        if (!this.loaded)
            return;

        for (ChannelAccess.ChannelHandle channelHandle : figuraHandlers.keySet()) {
            ChannelHandleAccessor accessor = (ChannelHandleAccessor) channelHandle;
            if (owner == null || (accessor.getOwner().equals(owner) && (name == null || accessor.getName().equals(name))))
                channelHandle.execute(Channel::stop);
        }
    }

    @Override @Intrinsic
    public void figura$stopAllSounds() {
        if (this.loaded) {
            for (ChannelAccess.ChannelHandle channelHandle : figuraHandlers.keySet())
                channelHandle.execute(Channel::stop);
            figuraHandlers.clear();
            figuraChannel.clear();
        }
    }

    @Override @Intrinsic
    public ChannelAccess.ChannelHandle figura$createHandle(UUID owner, String name, Library.Pool pool) {
        return figuraChannel.createHandle(pool).thenApply(channelHandle -> {
            if (channelHandle != null) {
                ((ChannelHandleAccessor) channelHandle).setOwner(owner);
                ((ChannelHandleAccessor) channelHandle).setName(name);
            }
            return channelHandle;
        }).join();
    }
}
