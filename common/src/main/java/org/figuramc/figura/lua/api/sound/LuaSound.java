package org.figuramc.figura.lua.api.sound;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.TextUtils;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(
        name = "Sound",
        value = "sound"
)
public class LuaSound {

    private final Avatar owner;
    private final String id;
    private final SoundBuffer buffer;
    private final Sound sound;

    private ChannelAccess.ChannelHandle handle;
    private boolean playing = false;

    private FiguraVec3 pos = FiguraVec3.of();
    private float pitch = 1f;
    private float volume = 1f;
    private float attenuation = 1f;
    private boolean loop = false;
    private Component subtitleText;
    private String subtitle;

    public LuaSound(SoundBuffer buffer, String id, Avatar owner) {
        this(null, buffer, id, Component.literal(id), owner);
    }

    public LuaSound(Sound sound, String id, Component subtitle, Avatar owner) {
        this(sound, null, id, subtitle, owner);
    }

    private LuaSound(Sound sound, SoundBuffer buffer, String id, Component subtitle, Avatar owner) {
        this.owner = owner;
        this.id = id;
        this.buffer = buffer;
        this.sound = sound;
        this.subtitleText = subtitle;
        this.subtitle = subtitle == null ? null : subtitle.getString();
    }

    public ChannelAccess.ChannelHandle getHandle() {
        return handle;
    }

    public Component getSubtitleText() {
        return subtitleText;
    }

    public String getId() {
        return id;
    }

    private float calculateVolume() {
        return SoundAPI.getSoundEngine().figura$getVolume(SoundSource.PLAYERS) * (owner.permissions.get(Permissions.VOLUME) / 100f);
    }

    @LuaWhitelist
    @LuaMethodDoc("sound.play")
    public LuaSound play() {
        // Only skip logic when we truely know the sound is currently playing
        if (this.playing && this.handle != null && !this.handle.isStopped())
            return this;

        if (!owner.soundsRemaining.use()) {
            owner.noPermissions.add(Permissions.SOUNDS);
            return this;
        }

        owner.noPermissions.remove(Permissions.SOUNDS);

        // if we still have a handle reference but the handle has been released, remove the reference
        // This occurs when sounds naturally stops
        if (this.handle != null && this.handle.isStopped()) {
            this.handle = null;
        }

        // if handle exists, the sound was previously played. Unpause it
        if (handle != null) {
            handle.execute(Channel::unpause);
            this.playing = true;
            return this;
        }

        // if there is no sound data, exit early
        if (buffer == null && sound == null)
            return this;

        float vol = calculateVolume();
        // if nobody can hear you scream, are you really screaming? No.
        if (vol <= 0)
            return this;

        // Technically I am setting playing to true earlier than I am supposed to,
        // but the function cannot exit early past here (other than crashing) so its fine
        this.playing = true;
        AvatarManager.executeAll("playSoundEvent", avatar -> {
            boolean cancel = avatar.playSoundEvent(
                this.getId(),
                this.getPos(),
                this.getVolume(), this.getPitch(),
                this.isLooping(),
                SoundSource.PLAYERS.name(),
                null
            );
            if (avatar.permissions.get(Permissions.CANCEL_SOUNDS) >= 1) {
                avatar.noPermissions.remove(Permissions.CANCEL_SOUNDS);
                if (cancel)
                    this.playing = false;
            }
            else {
                avatar.noPermissions.add(Permissions.CANCEL_SOUNDS);
            }
        });
        // If the sound was cancelled, exit.
        if (!this.playing) {
            return this;
        }

        if (buffer != null)
            this.handle = SoundAPI.getSoundEngine().figura$createHandle(owner.owner, id, Library.Pool.STATIC);
        else
            this.handle = SoundAPI.getSoundEngine().figura$createHandle(owner.owner, id, sound.shouldStream() ? Library.Pool.STREAMING : Library.Pool.STATIC);

        // I dunno why this check was here in the first place and I aint about to ruin things by removing it.
        if (handle == null) {
            this.playing = false; // explicit set just incase
            return this;
        }

        SoundAPI.getSoundEngine().figura$addSound(this);

        if (buffer != null) {
            handle.execute(channel -> {
                channel.setPitch(pitch);
                channel.setVolume(volume * vol);
                channel.linearAttenuation(attenuation * 16f);
                channel.setLooping(loop);
                channel.setSelfPosition(pos.asVec3());
                channel.setRelative(false);
                channel.attachStaticBuffer(buffer);
                channel.play();
            });
        } else {
            handle.execute(channel -> {
                channel.setPitch(pitch);
                channel.setVolume(volume * vol);
                channel.linearAttenuation(attenuation * 16f);
                channel.setLooping(loop && !sound.shouldStream());
                channel.setSelfPosition(pos.asVec3());
                channel.setRelative(false);
            });

            SoundBufferLibrary lib = SoundAPI.getSoundEngine().figura$getSoundBuffers();
            if (!sound.shouldStream()) {
                lib.getCompleteBuffer(sound.getPath()).thenAccept(buffer -> handle.execute(channel -> {
                    channel.attachStaticBuffer(buffer);
                    channel.play();
                }));
            } else {
                lib.getStream(sound.getPath(), loop).thenAccept(stream -> handle.execute(channel -> {
                    channel.attachBufferStream(stream);
                    channel.play();
                }));
            }
        }

        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("sound.is_playing")
    public boolean isPlaying() {
        if (handle != null) {
            // If the handle was released via the sound stopping naturlly, force set to false.
            // When a handle is stopped, it has no channel, so playing will not update.
            if (handle.isStopped())
                this.playing = false;
            // set playing based on whether the sound is paused or not.
            else
                handle.execute(channel -> this.playing = channel.playing());
        }
        // If there is no handle, forcefully set it to false just incase
        else
            this.playing = false;
        return this.playing;
    }

    @LuaWhitelist
    @LuaMethodDoc("sound.pause")
    public LuaSound pause() {
        this.playing = false;
        if (handle != null)
            handle.execute(Channel::pause);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("sound.stop")
    public LuaSound stop() {
        this.playing = false;
        if (handle != null)
            handle.execute(Channel::stop);
        handle = null;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("sound.get_pos")
    public FiguraVec3 getPos() {
        return pos;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "pos",
            value = "sound.set_pos")
    public LuaSound setPos(Object x, Double y, Double z) {
        this.pos = LuaUtils.parseVec3("setPos", x, y, z);
        if (handle != null)
            handle.execute(channel -> channel.setSelfPosition(pos.asVec3()));
        return this;
    }

    @LuaWhitelist
    public LuaSound pos(Object x, Double y, Double z) {
        return setPos(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("sound.get_volume")
    public float getVolume() {
        return volume;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "volume"
            ),
            aliases = "volume",
            value = "sound.set_volume")
    public LuaSound setVolume(float volume) {
        this.volume = Math.min(volume, 1);
        if (handle != null)
            handle.execute(channel -> {
                float f = calculateVolume();
                if (f <= 0) {
                    channel.stop();
                } else {
                    channel.setVolume(this.volume * f);
                }
            });
        return this;
    }

    @LuaWhitelist
    public LuaSound volume(float volume) {
        return setVolume(volume);
    }

    @LuaWhitelist
    @LuaMethodDoc("sound.get_attenuation")
    public float getAttenuation() {
        return attenuation;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "attenuation"
            ),
            aliases = "attenuation",
            value = "sound.set_attenuation")
    public LuaSound setAttenuation(float attenuation) {
        this.attenuation = Math.max(attenuation, 1);
        if (handle != null)
            handle.execute(channel -> channel.linearAttenuation(this.attenuation * 16f));
        return this;
    }

    @LuaWhitelist
    public LuaSound attenuation(float attenuation) {
        return setAttenuation(attenuation);
    }

    @LuaWhitelist
    @LuaMethodDoc("sound.get_pitch")
    public float getPitch() {
        return pitch;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "pitch"
            ),
            aliases = "pitch",
            value = "sound.set_pitch")
    public LuaSound setPitch(float pitch) {
        this.pitch = Math.max(pitch, 0);
        if (handle != null)
            handle.execute(channel -> channel.setPitch(this.pitch));
        return this;
    }

    @LuaWhitelist
    public LuaSound pitch(float pitch) {
        return setPitch(pitch);
    }

    @LuaWhitelist
    @LuaMethodDoc("sound.is_looping")
    public boolean isLooping() {
        return loop;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "loop"
            ),
            aliases = "loop",
            value = "sound.set_loop")
    public LuaSound setLoop(boolean loop) {
        this.loop = loop;
        if (handle != null)
            handle.execute(channel -> channel.setLooping(this.loop));
        return this;
    }

    @LuaWhitelist
    public LuaSound loop(boolean loop) {
        return setLoop(loop);
    }

    @LuaWhitelist
    @LuaMethodDoc("sound.get_subtitle")
    public String getSubtitle() {
        return subtitle;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "subtitle"
            ),
            aliases = "subtitle",
            value = "sound.set_subtitle")
    public LuaSound setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        if (subtitle == null) {
            this.subtitleText = null;
        } else {
            this.subtitleText = TextUtils.tryParseJson(subtitle);
            if (this.subtitleText.getString().length() > 48)
                throw new LuaError("Text length exceeded limit of 48 characters");
        }
        return this;
    }

    @LuaWhitelist
    public LuaSound subtitle(String subtitle) {
        return setSubtitle(subtitle);
    }

    @Override
    public String toString() {
        return id + " (Sound)";
    }
}
