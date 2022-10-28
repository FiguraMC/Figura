package org.moon.figura.lua.api.sound;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "Sound",
        value = "sound"
)
public class LuaSound {

    private final Avatar owner;
    private final String id;
    private final SoundBuffer buffer;
    private final ChannelAccess.ChannelHandle handle;
    private float volume = 1f;
    private Status status = Status.STOPPED;

    public LuaSound(SoundBuffer buffer, String id, Avatar owner) {
        this.owner = owner;
        this.id = id;
        this.buffer = buffer;
        this.handle = SoundAPI.getSoundEngine().figura$createHandle(owner.owner, id, Library.Pool.STATIC);
        handle.execute(channel -> channel.linearAttenuation(16f));
    }

    @LuaWhitelist
    public LuaSound play() {
        if (status != Status.PLAYING && !Minecraft.getInstance().isPaused() && owner.soundsRemaining.use()) {
            SoundAPI.getSoundEngine().figura$addSound(this);
            handle.execute(channel -> {
                if (isPaused()) {
                    channel.unpause();
                } else {
                    channel.attachStaticBuffer(buffer);
                    channel.play();
                }
            });
            status = Status.PLAYING;
        }
        return this;
    }

    @LuaWhitelist
    public boolean isPlaying() {
        return status == Status.PLAYING;
    }

    @LuaWhitelist
    public boolean isPaused() {
        return status == Status.PAUSED;
    }

    @LuaWhitelist
    public LuaSound pause() {
        handle.execute(Channel::pause);
        status = Status.PAUSED;
        return this;
    }

    @LuaWhitelist
    public LuaSound stop() {
        handle.execute(Channel::stop);
        status = Status.STOPPED;
        return this;
    }

    @LuaWhitelist
    public LuaSound pos(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("pos", x, y, z);
        handle.execute(channel -> channel.setSelfPosition(new Vec3(vec.x, vec.y, vec.z)));
        return this;
    }

    @LuaWhitelist
    public LuaSound volume(float volume) {
        this.volume = Math.min(volume * SoundAPI.getSoundEngine().figura$getVolume(SoundSource.PLAYERS), 1);
        handle.execute(channel -> channel.setVolume(volume));
        return this;
    }

    @LuaWhitelist
    public LuaSound attenuation(float attenuation) {
        handle.execute(channel -> channel.linearAttenuation(Math.max(attenuation, 1f) * 16f));
        return this;
    }

    @LuaWhitelist
    public LuaSound pitch(float pitch) {
        handle.execute(channel -> {
            channel.setPitch(pitch);
            if (pitch <= 0)
                channel.stop();
        });
        return this;
    }

    @LuaWhitelist
    public LuaSound loop(boolean loop) {
        handle.execute(channel -> channel.setLooping(loop));
        return this;
    }

    @LuaWhitelist
    public LuaSound relative(boolean relative) {
        handle.execute(channel -> channel.setRelative(relative));
        return this;
    }

    public ChannelAccess.ChannelHandle getHandle() {
        return handle;
    }

    public float getVolume() {
        return volume;
    }

    @Override
    public String toString() {
        return id + " (Sound)";
    }

    private enum Status {
        PAUSED,
        PLAYING,
        STOPPED
    }
}
