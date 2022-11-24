package org.moon.figura.animation;

import com.mojang.datafixers.util.Pair;
import org.luaj.vm2.LuaError;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.model.FiguraModelPart;

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
    protected PlayState playState = PlayState.STOPPED;
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
        //tick time
        this.controller.tick();

        this.time += controller.getDiff() * speed;

        //loop checks
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
        }

        this.lastTime = this.frameTime;
        this.frameTime = Math.max(this.time, this.offset);

        //code events
        if (inverted)
            playCode(this.frameTime, this.lastTime);
        else
            playCode(this.lastTime, this.frameTime);
    }

    public void playCode(float minTime, float maxTime) {
        if (codeFrames.keySet().isEmpty())
            return;

        if (maxTime < minTime) {
            float len = length + 0.001f;
            playCode(Math.min(minTime, len), len);
            minTime = offset;
        }

        for (Float codeTime : codeFrames.keySet()) {
            if (codeTime >= minTime && codeTime < maxTime)
                owner.run(Pair.of("animations." + modelName + "." + name, codeFrames.get(codeTime)), owner.tick, this);
        }
    }

    public static Map<String, Map<String, Animation>> getTableForAnimations(Avatar avatar) {
        HashMap<String, Map<String, Animation>> root = new HashMap<>();
        for (Animation animation : avatar.animations.values()) {
            //get or create animation table
            Map<String, Animation> animations = root.get(animation.modelName);
            if (animations == null)
                animations = new HashMap<>();

            //put animation on the model table
            animations.put(animation.name, animation);
            root.put(animation.modelName, animations);
        }
        return root;
    }

    // -- lua methods -- //

    @LuaWhitelist
    @LuaMethodDoc("animation.play")
    public void play() {
        switch (playState) {
            case PAUSED -> controller.resume();
            case STOPPED -> {
                controller.init();
                time = inverted ? (length + startDelay) : (offset - startDelay);
                lastTime = time;
                frameTime = 0f;
            }
            default -> {return;}
        }

        playState = PlayState.PLAYING;
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.pause")
    public void pause() {
        controller.pause();
        playState = PlayState.PAUSED;
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.stop")
    public void stop() {
        controller.reset();
        playState = PlayState.STOPPED;
    }

    @LuaWhitelist
    @LuaMethodDoc("animation.restart")
    public void restart() {
        stop();
        play();
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
            value = "animation.set_playing"
    )
    public void setPlaying(boolean bool) {
        if (bool)
            play();
        else
            stop();
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
            value = "animation.set_time"
    )
    public void setTime(float time) {
        this.time = time;
        this.lastTime = time;
        this.frameTime = Math.max(time, this.offset);
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
            value = "animation.new_code"
    )
    public Animation newCode(float time, @LuaNotNil String data) {
        codeFrames.put(Math.max(time, 0f), data);
        return this;
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
            value = "animation.blend"
    )
    public Animation blend(float blend) {
        this.blend = blend;
        return this;
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
            value = "animation.offset"
    )
    public Animation offset(float offset) {
        this.offset = offset;
        return this;
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
            value = "animation.start_delay"
    )
    public Animation startDelay(float delay) {
        this.startDelay = delay;
        return this;
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
            value = "animation.loop_delay"
    )
    public Animation loopDelay(float delay) {
        this.loopDelay = delay;
        return this;
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
            value = "animation.length"
    )
    public Animation length(float length) {
        this.length = length;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "override"
            ),
            value = "animation.override"
    )
    public Animation override(boolean override) {
        this.override = override ? 7 : 0;
        return this;
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
            value = "animation.override_rot"
    )
    public Animation overrideRot(boolean override) {
        this.override = override ? this.override | 1 : this.override & 6;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "override"
            ),
            value = "animation.override_pos"
    )
    public Animation overridePos(boolean override) {
        this.override = override ? this.override | 2 : this.override & 5;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "override"
            ),
            value = "animation.override_scale"
    )
    public Animation overrideScale(boolean override) {
        this.override = override ? this.override | 4 : this.override & 3;
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
            value = "animation.loop"
    )
    public Animation loop(@LuaNotNil String loop) {
        try {
            this.loop = LoopMode.valueOf(loop.toUpperCase());
            return this;
        } catch (Exception ignored) {
            throw new LuaError("Illegal LoopMode: \"" + loop + "\".");
        }
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
            value = "animation.priority"
    )
    public Animation priority(int priority) {
        this.priority = priority;
        return this;
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
            value = "animation.speed"
    )
    public Animation speed(Float speed) {
        if (speed == null) speed = 1f;
        this.speed = speed;
        this.inverted = speed < 0;
        return this;
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
