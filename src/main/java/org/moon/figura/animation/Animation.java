package org.moon.figura.animation;

import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaTable;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LuaWhitelist
@LuaTypeDoc(
        name = "Animation",
        description = "animation"
)
public class Animation {

    private final Avatar owner;
    private final String modelName;

    @LuaWhitelist
    @LuaFieldDoc(description = "animation.name")
    public final String name;

    // -- keyframes -- //

    protected final Map<FiguraModelPart, List<AnimationChannel>> animationParts = new HashMap<>();
    private final Map<Float, String> codeFrames = new HashMap<>();

    // -- player variables -- //

    private final TimeController controller = new TimeController();
    protected PlayState playState = PlayState.STOPPED;
    protected float time = 0f;
    private float lastTime = -1f;

    // -- data variables -- //

    protected float length, blend, offset;
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
        this.controller.tick();
        float newTime = controller.getElapsedTimeSeconds();

        switch (this.loop) {
            case HOLD -> this.time = newTime + offset;
            case LOOP -> this.time = newTime % (length + loopDelay - offset) + offset;
            case ONCE -> {
                this.time = newTime + offset;
                if (this.time >= length)
                    Animation.stop(this);
            }
        }

        playCode(this.lastTime, this.time);
        this.lastTime = this.time;
    }

    public void playCode(float minTime, float maxTime) {
        if (maxTime < minTime) {
            playCode(minTime, length + 0.001f);
            minTime = offset;
        }

        for (Float codeTime : codeFrames.keySet()) {
            if (codeTime >= minTime && codeTime < maxTime && !owner.scriptError && owner.luaState != null)
                owner.luaState.runScript(codeFrames.get(codeTime), "animation (" + this.name + ")");
        }
    }

    public static LuaTable getTableForAnimations(Avatar avatar) {
        LuaTable models = new LuaTable();
        for (Animation animation : avatar.animations.values()) {
            //get or create animation table
            LuaTable animations = (LuaTable) models.get(animation.modelName);
            if (animations == null)
                animations = new LuaTable();

            //put animation on the model table
            animations.put(animation.name, animation);
            models.put(animation.modelName, animations);
        }
        return models;
    }

    // -- lua methods -- //

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Animation.class,
                    argumentNames = "animation"
            ),
            description = "animation.play"
    )
    public static void play(@LuaNotNil Animation animation) {
        switch (animation.playState) {
            case PAUSED -> animation.controller.resume();
            case STOPPED -> {
                animation.controller.init(animation.startDelay);
                animation.lastTime = animation.offset;
            }
            default -> {return;}
        }

        animation.playState = PlayState.PLAYING;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Animation.class,
                    argumentNames = "animation"
            ),
            description = "animation.pause"
    )
    public static void pause(@LuaNotNil Animation animation) {
        animation.controller.pause();
        animation.playState = PlayState.PAUSED;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Animation.class,
                    argumentNames = "animation"
            ),
            description = "animation.stop"
    )
    public static void stop(@LuaNotNil Animation animation) {
        animation.controller.reset();
        animation.playState = PlayState.STOPPED;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Animation.class,
                    argumentNames = "animation"
            ),
            description = "animation.restart"
    )
    public static void restart(@LuaNotNil Animation animation) {
        stop(animation);
        play(animation);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Animation.class,
                    argumentNames = "animation"
            ),
            description = "animation.get_time"
    )
    public static float getTime(@LuaNotNil Animation animation) {
        return animation.time;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Animation.class, Float.class},
                    argumentNames = {"animation", "time"}
            ),
            description = "animation.set_time"
    )
    public static void setTime(@LuaNotNil Animation animation, @LuaNotNil Float time) {
        animation.controller.setTime(time);
        animation.tick();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Animation.class,
                    argumentNames = "animation"
            ),
            description = "animation.get_play_state"
    )
    public static String getPlayState(@LuaNotNil Animation animation) {
        return animation.playState.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Animation.class, Float.class, String.class},
                    argumentNames = {"animation", "time", "code"}
            ),
            description = "animation.add_code"
    )
    public static Animation addCode(@LuaNotNil Animation animation, @LuaNotNil Float time, @LuaNotNil String data) {
        animation.codeFrames.put(time, data);
        return animation;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Animation.class, Float.class},
                    argumentNames = {"animation", "blend"}
            ),
            description = "animation.blend"
    )
    public static Animation blend(@LuaNotNil Animation animation, @LuaNotNil Float blend) {
        animation.blend = blend;
        return animation;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Animation.class, Float.class},
                    argumentNames = {"animation", "offset"}
            ),
            description = "animation.offset"
    )
    public static Animation offset(@LuaNotNil Animation animation, @LuaNotNil Float offset) {
        animation.offset = offset;
        return animation;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Animation.class, Float.class},
                    argumentNames = {"animation", "delay"}
            ),
            description = "animation.start_delay"
    )
    public static Animation startDelay(@LuaNotNil Animation animation, @LuaNotNil Float startDelay) {
        animation.startDelay = startDelay;
        return animation;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Animation.class, Float.class},
                    argumentNames = {"animation", "delay"}
            ),
            description = "animation.loop_delay"
    )
    public static Animation loopDelay(@LuaNotNil Animation animation, @LuaNotNil Float loopDelay) {
        animation.loopDelay = loopDelay;
        return animation;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Animation.class, Float.class},
                    argumentNames = {"animation", "length"}
            ),
            description = "animation.length"
    )
    public static Animation length(@LuaNotNil Animation animation, @LuaNotNil Float length) {
        animation.length = length;
        return animation;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Animation.class, Boolean.class},
                    argumentNames = {"animation", "override"}
            ),
            description = "animation.override"
    )
    public static Animation override(@LuaNotNil Animation animation, @LuaNotNil Boolean override) {
        animation.override = override;
        return animation;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Animation.class, String.class},
                    argumentNames = {"animation", "loop"}
            ),
            description = "animation.loop"
    )
    public static Animation loop(@LuaNotNil Animation animation, @LuaNotNil String loop) {
        try {
            animation.loop = LoopMode.valueOf(loop.toUpperCase());
            return animation;
        } catch (Exception ignored) {
            throw new LuaRuntimeException("Illegal LoopMode: \"" + loop + "\".");
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Animation.class, Integer.class},
                    argumentNames = {"animation", "priority"}
            ),
            description = "animation.priority"
    )
    public static Animation priority(@LuaNotNil Animation animation, @LuaNotNil Integer priority) {
        animation.priority = priority;
        return animation;
    }

    @Override
    public String toString() {
        return "Animation";
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
