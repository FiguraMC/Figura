package org.moon.figura.animation;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaTypeManager;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LuaType(typeName = "animation")
@LuaTypeDoc(
        name = "Animation",
        description = "animation"
)
public class Animation {

    private final Avatar owner;
    private final String modelName;

    @LuaFieldDoc(description = "animation.name")
    public final String name;

    // -- keyframes -- //

    protected final Map<FiguraModelPart, List<AnimationChannel>> animationParts = new HashMap<>();
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
    protected boolean override;
    protected int priority = 0;
    protected LoopMode loop;

    // -- java methods -- //

    public Animation(Avatar owner, String modelName, String name, LoopMode loop, boolean override, float length, float offset, float blend, float startDelay, float loopDelay) {
        this.owner = owner;
        this.modelName = modelName;
        this.name = name;
        this.loop = loop;
        this.override = override;
        this.length = length;
        this.offset = offset;
        this.blend = blend;
        this.startDelay = startDelay;
        this.loopDelay = loopDelay;
    }

    public void addAnimation(FiguraModelPart part, AnimationChannel anim) {
        this.animationParts.computeIfAbsent(part, modelPart -> new ArrayList<>()).add(anim);
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
            if (codeTime >= minTime && codeTime < maxTime && !owner.scriptError && owner.luaRuntime != null)
                owner.luaRuntime.runScript(codeFrames.get(codeTime), "animation (" + this.name + ")");
        }
    }

    public static LuaTable getTableForAnimations(Avatar avatar) {
        LuaTable models = new LuaTable();
        for (Animation animation : avatar.animations.values()) {
            //get or create animation table
            LuaValue animations = models.get(animation.modelName);
            if (animations == null || animations.isnil())
                animations = new LuaTable();

            //put animation on the model table
            animations.set(animation.name, LuaTypeManager.wrap(animation));
            models.set(animation.modelName, animations);
        }
        return models;
    }

    // -- lua methods -- //

    @LuaWhitelist
    @LuaMethodDoc(description = "animation.play")
    public void play() {
        switch (playState) {
            case PAUSED -> controller.resume();
            case STOPPED -> {
                controller.init();
                time = inverted ? (length + startDelay) : (offset - startDelay);
                lastTime = time;
            }
            default -> {return;}
        }

        playState = PlayState.PLAYING;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "animation.pause")
    public void pause() {
        controller.pause();
        playState = PlayState.PAUSED;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "animation.stop")
    public void stop() {
        controller.reset();
        playState = PlayState.STOPPED;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "animation.restart")
    public void restart() {
        stop();
        play();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "animation.get_time")
    public float getTime() {
        return time;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Float.class,
                    argumentNames = "time"
            ),
            description = "animation.set_time"
    )
    public void setTime(@LuaNotNil Float time) {
        this.time = time;
        tick();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "animation.get_play_state")
    public String getPlayState() {
        return playState.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Float.class, String.class},
                    argumentNames = {"time", "code"}
            ),
            description = "animation.add_code"
    )
    public Animation addCode(@LuaNotNil Float time, @LuaNotNil String data) {
        codeFrames.put(Math.max(time, 0f), data);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Float.class,
                    argumentNames = "blend"
            ),
            description = "animation.blend"
    )
    public Animation blend(@LuaNotNil Float blend) {
        this.blend = blend;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Float.class,
                    argumentNames = "offset"
            ),
            description = "animation.offset"
    )
    public Animation offset(@LuaNotNil Float offset) {
        this.offset = offset;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Float.class,
                    argumentNames = "delay"
            ),
            description = "animation.start_delay"
    )
    public Animation startDelay(@LuaNotNil Float delay) {
        this.startDelay = delay;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Float.class,
                    argumentNames = "delay"
            ),
            description = "animation.loop_delay"
    )
    public Animation loopDelay(@LuaNotNil Float delay) {
        this.loopDelay = delay;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Float.class,
                    argumentNames = "length"
            ),
            description = "animation.length"
    )
    public Animation length(@LuaNotNil Float length) {
        this.length = length;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "override"
            ),
            description = "animation.override"
    )
    public Animation override(@LuaNotNil Boolean override) {
        this.override = override;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "loop"
            ),
            description = "animation.loop"
    )
    public Animation override(@LuaNotNil String loop) {
        try {
            this.loop = LoopMode.valueOf(loop.toUpperCase());
            return this;
        } catch (Exception ignored) {
            throw new LuaError("Illegal LoopMode: \"" + loop + "\".");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "priority"
            ),
            description = "animation.priority"
    )
    public Animation priority(@LuaNotNil Integer priority) {
        this.priority = priority;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Float.class,
                    argumentNames = "speed"
            ),
            description = "animation.speed"
    )
    public Animation speed(@LuaNotNil Float speed) {
        this.speed = speed;
        this.inverted = speed < 0;
        return this;
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
