package org.figuramc.figura.animation;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.model.FiguraModelPart;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;

import java.util.*;

@LuaWhitelist
@LuaTypeDoc(
        name = "Animation",
        value = "animation"
)
public class Animation {

    private final Avatar owner;
    public final String modelName;

    @LuaWhitelist
    @LuaFieldDoc("animation.name")
    public final String name;

    // -- keyframes -- // 

    protected final List<Map.Entry<FiguraModelPart, List<Animation.AnimationChannel>>> animationParts = new ArrayList<>();
    private final Map<Float, String> codeFrames = new HashMap<>();

    // -- player variables -- // 

    private final TimeController controller = new TimeController();
    public PlayState playState = PlayState.STOPPED;
    private float time = 0f;
    private boolean inverted = false;
    private float lastTime = 0f;
    protected float frameTime = 0f;

    // -- data variables -- // 

    protected float length, blend, offset;
    protected float speed = 1f;
    protected float startDelay, loopDelay;
    protected int override;
    protected int priority = 0;
    protected LoopMode loop;

    // -- java methods -- // 

    public Animation(Avatar owner, String modelName, String name, LoopMode loop, boolean override, float length, float offset, float blend, float startDelay, float loopDelay) {
        this.owner = owner;
        this.modelName = modelName;
        this.name = name;
        this.loop = loop;
        this.override = override ? 7 : 0;
        this.length = length;
        this.offset = offset;
        this.blend = blend;
        this.startDelay = startDelay;
        this.loopDelay = loopDelay;
    }

    public void addAnimation(FiguraModelPart part, AnimationChannel anim) {
        Map.Entry<FiguraModelPart, List<AnimationChannel>> entry = null;
        for (Map.Entry<FiguraModelPart, List<AnimationChannel>> listEntry : this.animationParts) {
            if (listEntry.getKey() == part) {
                entry = listEntry;
                break;
            }
        }

        if (entry == null) {
            entry = new AbstractMap.SimpleEntry<>(part, new ArrayList<>());
            this.animationParts.add(entry);
        }

        entry.getValue().add(anim);
        this.animationParts.sort(Map.Entry.comparingByKey());
    }

    public void tick() {
        // tick time
        this.controller.tick();

        this.time += controller.getDiff() * speed;

        // loop checks
        switch (this.loop) {
            case ONCE -> {
                if ((!inverted && time >= length) || (inverted && time <= 0))
                    stop();
            }
            case LOOP -> {
                if (!inverted && time > length + loopDelay)
                    time -= length + loopDelay - offset;
                else if (inverted && time < offset - loopDelay)
                    time += length + loopDelay - offset;
            }
            case HOLD -> time = inverted ? Math.max(time, offset) : Math.min(time, length);
        }

        this.lastTime = this.frameTime;
        this.frameTime = Math.max(this.time, this.offset);

        // code events
        if (inverted)
            playCode(this.frameTime, this.lastTime);
        else
            playCode(this.lastTime, this.frameTime);
    }

    public void playCode(float minTime, float maxTime) {
        if (owner.luaRuntime == null || codeFrames.keySet().isEmpty())
            return;

        if (maxTime < minTime) {
            float len = length + 0.001f;
            playCode(Math.min(minTime, len), len);
            minTime = offset;
        }

        for (Float codeTime : codeFrames.keySet()) {
            if (codeTime >= minTime && codeTime < maxTime) {
                try {
                    LuaValue value = owner.loadScript("animations." + modelName + "." + name, codeFrames.get(codeTime));
                    owner.run(value, owner.animation, this);
                } catch (Exception e) {
                    owner.luaRuntime.error(e);
                }
            }
        }
    }


    // -- lua methods -- // 


    @LuaWhitelist
    @LuaMethodDoc("animation.is_playing")
    public boolean isPlaying() {
        return this.playState == PlayState.PLAYING;
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.is_paused")
    public boolean isPaused() {
        return this.playState == PlayState.PAUSED;
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.is_stopped")
    public boolean isStopped() {
        return this.playState == PlayState.STOPPED;
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.play")
    public Animation play() {
        switch (playState) {
            case PAUSED -> controller.resume();
            case STOPPED -> {
                controller.init();
                time = inverted ? (length + startDelay) : (offset - startDelay);
                lastTime = time;
                frameTime = 0f;
            }
            default -> {return this;}
        }

        playState = PlayState.PLAYING;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.pause")
    public Animation pause() {
        controller.pause();
        playState = PlayState.PAUSED;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.stop")
    public Animation stop() {
        controller.reset();
        playState = PlayState.STOPPED;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.restart")
    public Animation restart() {
        stop();
        play();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Boolean.class,
                            argumentNames = "bool"
                    )
            },
            aliases = "playing",
            value = "animation.set_playing"
    )
    public Animation setPlaying(boolean bool) {
        return bool ? play() : stop();
    }

    @LuaWhitelist
    public Animation playing(boolean bool) {
        return setPlaying(bool);
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_time")
    public float getTime() {
        return time;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "time"
            ),
            aliases = "time",
            value = "animation.set_time"
    )
    public Animation setTime(float time) {
        this.time = time;
        this.lastTime = time;
        this.frameTime = Math.max(time, this.offset);
        return this;
    }

    @LuaWhitelist
    public Animation time(float time) {
        return setTime(time);
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_play_state")
    public String getPlayState() {
        return playState.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Float.class, String.class},
                    argumentNames = {"time", "code"}
            ),
            aliases = "code",
            value = "animation.new_code"
    )
    public Animation newCode(float time, @LuaNotNil String data) {
        codeFrames.put(Math.max(time, 0f), data);
        return this;
    }

    @LuaWhitelist
    public Animation code(float time, @LuaNotNil String data) {
        return newCode(time, data);
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_blend")
    public float getBlend() {
        return this.blend;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "blend"
            ),
            aliases = "blend",
            value = "animation.set_blend"
    )
    public Animation setBlend(float blend) {
        this.blend = blend;
        return this;
    }

    @LuaWhitelist
    public Animation blend(float blend) {
        return setBlend(blend);
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_offset")
    public float getOffset() {
        return this.offset;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "offset"
            ),
            aliases = "offset",
            value = "animation.set_offset"
    )
    public Animation setOffset(float offset) {
        this.offset = offset;
        return this;
    }

    @LuaWhitelist
    public Animation offset(float offset) {
        return setOffset(offset);
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_start_delay")
    public float getStartDelay() {
        return this.startDelay;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "delay"
            ),
            aliases = "startDelay",
            value = "animation.set_start_delay"
    )
    public Animation setStartDelay(float delay) {
        this.startDelay = delay;
        return this;
    }

    @LuaWhitelist
    public Animation startDelay(float delay) {
        return setStartDelay(delay);
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_loop_delay")
    public float getLoopDelay() {
        return this.loopDelay;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "delay"
            ),
            aliases = "loopDelay",
            value = "animation.set_loop_delay"
    )
    public Animation setLoopDelay(float delay) {
        this.loopDelay = delay;
        return this;
    }

    @LuaWhitelist
    public Animation loopDelay(float delay) {
        return setLoopDelay(delay);
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_length")
    public float getLength() {
        return this.length;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "length"
            ),
            aliases = "length",
            value = "animation.set_length"
    )
    public Animation setLength(float length) {
        this.length = length;
        return this;
    }

    @LuaWhitelist
    public Animation length(float length) {
        return setLength(length);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "override"
            ),
            aliases = "override",
            value = "animation.set_override"
    )
    public Animation setOverride(boolean override) {
        this.override = override ? 7 : 0;
        return this;
    }

    @LuaWhitelist
    public Animation override(boolean override) {
        return setOverride(override);
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_override_rot")
    public boolean getOverrideRot() {
        return (override & 1) == 1;
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_override_pos")
    public boolean getOverridePos() {
        return (override & 2) == 2;
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_override_scale")
    public boolean getOverrideScale() {
        return (override & 4) == 4;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "override"
            ),
            aliases = "overrideRot",
            value = "animation.set_override_rot"
    )
    public Animation setOverrideRot(boolean override) {
        this.override = override ? this.override | 1 : this.override & 6;
        return this;
    }

    @LuaWhitelist
    public Animation overrideRot(boolean override) {
        return setOverrideRot(override);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "override"
            ),
            aliases = "overridePos",
            value = "animation.set_override_pos"
    )
    public Animation setOverridePos(boolean override) {
        this.override = override ? this.override | 2 : this.override & 5;
        return this;
    }

    @LuaWhitelist
    public Animation overridePos(boolean override) {
        return setOverridePos(override);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "override"
            ),
            aliases = "overrideScale",
            value = "animation.set_override_scale"
    )
    public Animation setOverrideScale(boolean override) {
        this.override = override ? this.override | 4 : this.override & 3;
        return this;
    }

    @LuaWhitelist
    public Animation overrideScale(boolean override) {
        setOverrideScale(override);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_loop")
    public String getLoop() {
        return this.loop.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "loop"
            ),
            aliases = "loop",
            value = "animation.set_loop"
    )
    public Animation setLoop(@LuaNotNil String loop) {
        try {
            this.loop = LoopMode.valueOf(loop.toUpperCase(Locale.US));
            return this;
        } catch (Exception ignored) {
            throw new LuaError("Illegal LoopMode: \"" + loop + "\".");
        }
    }

    @LuaWhitelist
    public Animation loop(@LuaNotNil String loop) {
        return setLoop(loop);
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_priority")
    public int getPriority() {
        return this.priority;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "priority"
            ),
            aliases = "priority",
            value = "animation.set_priority"
    )
    public Animation setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @LuaWhitelist
    public Animation priority(int priority) {
        return setPriority(priority);
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_speed")
    public float getSpeed() {
        return this.speed;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Float.class,
                    argumentNames = "speed"
            ),
            aliases = "speed",
            value = "animation.set_speed"
    )
    public Animation setSpeed(Float speed) {
        if (speed == null) speed = 1f;
        this.speed = speed;
        this.inverted = speed < 0;
        return this;
    }

    @LuaWhitelist
    public Animation speed(Float speed) {
        return setSpeed(speed);
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.get_name")
    public String getName() {
        return name;
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        if (arg.equals("name"))
            return name;
        return null;
    }

    @Override
    public String toString() {
        return name + " (Animation)";
    }

    // -- other classes -- // 

    public enum PlayState {
        STOPPED,
        PAUSED,
        PLAYING
    }

    public enum LoopMode {
        LOOP,
        ONCE,
        HOLD
    }

    public record AnimationChannel(TransformType type, Keyframe... keyframes) {}
}
