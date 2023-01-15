package org.moon.figura.mixin.sound;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import net.minecraft.client.Options;
import net.minecraft.client.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.moon.figura.ducks.ChannelHandleAccessor;
import org.moon.figura.ducks.SoundEngineAccessor;
import org.moon.figura.lua.api.sound.LuaSound;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin implements SoundEngineAccessor {

    @Shadow @Final private Library library;
    @Shadow @Final private SoundEngineExecutor executor;
    @Shadow @Final private SoundBufferLibrary soundBuffers;
    @Shadow private boolean loaded;

    @Shadow protected abstract float getVolume(@Nullable SoundSource category);

    @Unique
    private ChannelAccess figuraChannel;
    @Unique
    private final ArrayList<LuaSound> figuraHandlers = new ArrayList<>();

    @Inject(at = @At("RETURN"), method = "<init>")
    private void soundEngineInit(SoundManager soundManager, Options options, ResourceProvider resourceProvider, CallbackInfo ci) {
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

        for (LuaSound sound : figuraHandlers)
            sound.volume(sound.getVolume());
    }

    @Inject(at = @At("RETURN"), method = "stop(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/sounds/SoundSource;)V")
    private void stop(ResourceLocation id, SoundSource category, CallbackInfo ci) {
        if (category == SoundSource.PLAYERS)
            figura$stopAllSounds();
    }

    @Override @Intrinsic
    public void figura$addSound(LuaSound sound) {
        figuraHandlers.add(sound);
    }

    @Override @Intrinsic
    public void figura$stopSound(UUID owner, String name) {
        if (!this.loaded)
            return;

        Iterator<LuaSound> iterator = figuraHandlers.iterator();
        while (iterator.hasNext()) {
            LuaSound sound = iterator.next();
            ChannelHandleAccessor accessor = (ChannelHandleAccessor) sound.getHandle();
            if (accessor != null && (owner == null || (accessor.getOwner().equals(owner) && (name == null || accessor.getName().equals(name))))) {
                sound.stop();
                iterator.remove();
            }
        }
    }

    @Override @Intrinsic
    public void figura$stopAllSounds() {
        if (this.loaded) {
            for (LuaSound sound : figuraHandlers)
                sound.stop();
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

    @Override @Intrinsic
    public float figura$getVolume(SoundSource category) {
        return getVolume(category);
    }

    @Override @Intrinsic
    public SoundBufferLibrary figura$getSoundBuffers() {
        return this.soundBuffers;
    }
}
