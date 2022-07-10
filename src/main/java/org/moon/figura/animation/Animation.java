package org.moon.figura.animation;

import org.moon.figura.avatars.model.FiguraModelPart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Animation {

    protected final Map<FiguraModelPart, List<AnimationChannel>> animationParts = new HashMap<>();
    private final Map<Float, String> codeFrames = new HashMap<>();

    // -- player variables -- //

    private PlayState playState = PlayState.STOPPED;
    private float time = 0f;

    // -- data variables -- //

    public float length = 0f;
    public float blend = 1f;
    public float offset = 0f;
    public float startDelay = 0f;
    public float loopDelay = 0f;
    public boolean override = false;
    public LoopMode loop = LoopMode.ONCE;

    public Animation blend(float blend) {
        this.blend = blend;
        return this;
    }

    public Animation offset(float offset) {
        this.offset = offset;
        return this;
    }

    public Animation startDelay(float startDelay) {
        this.startDelay = startDelay;
        return this;
    }

    public Animation loopDelay(float loopDelay) {
        this.loopDelay = loopDelay;
        return this;
    }

    public Animation length(float length) {
        this.length = length;
        return this;
    }

    public Animation override(boolean override) {
        this.override = override;
        return this;
    }

    public Animation loop(LoopMode loop) {
        this.loop = loop;
        return this;
    }

    public Animation addAnimation(FiguraModelPart part, AnimationChannel anim) {
        this.animationParts.computeIfAbsent(part, modelPart -> new ArrayList<>()).add(anim);
        return this;
    }

    public Animation addCode(float time, String data) {
        this.codeFrames.put(time, data);
        return this;
    }

    public void updateTime(long time) {

    }

    public float getTime() {
        return time;
    }

    public PlayState getPlayState() {
        return playState;
    }

    public void setPlayState(PlayState playState) {
        this.playState = playState;
    }

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
