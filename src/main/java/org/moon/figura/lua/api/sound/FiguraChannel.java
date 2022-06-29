package org.moon.figura.lua.api.sound;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.ducks.ChannelHandleAccessor;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.mixin.sound.ChannelAccessMixin;
import org.moon.figura.mixin.sound.SoundEngineAccessor;
import org.moon.figura.mixin.sound.SoundManagerAccessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FiguraChannel extends ChannelAccess {

    private static FiguraChannel instance;
    private final HashMap<UUID, HashSet<String>> stopRequests = new HashMap<>();
    private boolean stopAllSounds = false;
    private final Minecraft minecraft;

    public FiguraChannel(Library library, Executor executor, Minecraft minecraft) {
        super(library, executor);
        this.minecraft = minecraft;
    }

    // -- methods -- //

    private static SoundEngineAccessor getSoundEngine() {
        return (SoundEngineAccessor) ((SoundManagerAccessor) Minecraft.getInstance().getSoundManager()).getSoundEngine();
    }

    public static FiguraChannel getInstance() {
        if (instance == null) {
            SoundEngineAccessor engine = getSoundEngine();
            instance = new FiguraChannel(engine.getLibrary(), engine.getExecutor(), Minecraft.getInstance());
        }
        return instance;
    }

    public void tick() {
        ((ChannelAccessMixin) this).getExecutor().execute(() -> {
            //check for no volume
            Options options = minecraft.options;
            if (stopAllSounds || options.getSoundSourceVolume(SoundSource.PLAYERS) <= 0 || options.getSoundSourceVolume(SoundSource.MASTER) <= 0)
                stopAllSounds = true;

            //sound loop
            Iterator<ChannelHandle> iterator = ((ChannelAccessMixin) this).getChannels().iterator();
            while(iterator.hasNext()) {
                ChannelHandle channelHandle = iterator.next();
                Channel channel = ((ChannelHandleAccessor) channelHandle).getChannel();

                //stop sounds
                if (channel == null || stopAllSounds || shouldStop(channelHandle)) {
                    if (channel != null)
                        channel.stop();

                    channelHandle.release();
                    iterator.remove();
                    continue;
                }

                //remove sounds when done
                channel.updateStream();
                if (channel.stopped()) {
                    channelHandle.release();
                    iterator.remove();
                }
            }

            //clear stop requests
            stopRequests.clear();
            stopAllSounds = false;
        });
    }

    // -- play functions -- //

    private CompletableFuture<ChannelHandle> createSource(UUID owner, String name, Library.Pool pool) {
        return super.createHandle(pool).thenApply((channelHandle) -> {
            ((ChannelHandleAccessor) channelHandle).setOwner(owner);
            ((ChannelHandleAccessor) channelHandle).setName(name);
            return channelHandle;
        });
    }

    public void playSound(UUID owner, String sound, SoundBuffer buffer, double x, double y, double z, float volume, float pitch, boolean loop) {
        volume = Math.min(volume * minecraft.options.getSoundSourceVolume(SoundSource.PLAYERS), 1);
        if (volume <= 0 || pitch <= 0)
            return;

        float finalVolume = volume;
        createSource(owner, sound, Library.Pool.STATIC).thenAccept(channelHandle -> channelHandle.execute(channel -> {
            if (channel == null)
                return;

            channel.linearAttenuation(finalVolume * 16f);
            channel.attachStaticBuffer(buffer);
            channel.setSelfPosition(new Vec3(x, y, z));
            channel.setVolume(finalVolume);
            channel.setPitch(pitch);
            channel.setRelative(true);
            channel.setLooping(loop);
            channel.play();
        }));
    }

    public void playSound(UUID owner, String sound, SoundEvent event, double x, double y, double z, float volume, float pitch, boolean loop) {
        SimpleSoundInstance simpleSoundInstance = new SimpleSoundInstance(event, SoundSource.PLAYERS, volume, pitch, RandomSource.create(WorldAPI.getCurrentWorld().random.nextLong()), x, y, z);
        WeighedSoundEvents soundEvents = simpleSoundInstance.resolve(minecraft.getSoundManager());
        Sound s;
        ChannelHandle handle;

        if (soundEvents == null ||
                (volume = Math.min(volume * minecraft.options.getSoundSourceVolume(SoundSource.PLAYERS), 1)) <= 0 ||
                pitch <= 0 ||
                (s = simpleSoundInstance.getSound()) == SoundManager.EMPTY_SOUND ||
                (handle = createSource(owner, sound, s.shouldStream() ? Library.Pool.STREAMING : Library.Pool.STATIC).join()) == null
        ) {
            return;
        }

        int attenuation = s.getAttenuationDistance();
        float finalVolume = volume;
        boolean finalLoop = loop && !s.shouldStream();
        Vec3 pos = new Vec3(x, y, z);

        handle.execute(channel -> {
            channel.linearAttenuation(finalVolume * attenuation);
            channel.setSelfPosition(pos);
            channel.setVolume(finalVolume);
            channel.setPitch(pitch);
            channel.setRelative(true);
            channel.setLooping(finalLoop);
        });

        SoundBufferLibrary buffers = getSoundEngine().getSoundBuffers();
        if (s.shouldStream()) {
            buffers.getStream(s.getPath(), loop).thenAccept(stream -> handle.execute(channel -> {
                channel.attachBufferStream(stream);
                channel.play();
            }));
        } else {
            buffers.getCompleteBuffer(s.getPath()).thenAccept(buffer -> handle.execute(channel -> {
                channel.attachStaticBuffer(buffer);
                channel.play();
            }));
        }
    }

    // -- stop functions -- //

    public void stopAllSounds() {
        stopAllSounds = true;
    }

    public void stopSound(UUID owner, String sound) {
        HashSet<String> set = stopRequests.get(owner);
        if (set == null)
            set = new HashSet<>();

        if (sound == null) set.clear();
        else set.add(sound);

        stopRequests.put(owner, set);
    }

    public boolean shouldStop(ChannelHandle handle) {
        ChannelHandleAccessor accessor = (ChannelHandleAccessor) handle;
        HashSet<String> requestNames = stopRequests.get(accessor.getOwner());
        return requestNames != null && (requestNames.isEmpty() || requestNames.contains(accessor.getName()));
    }
}
