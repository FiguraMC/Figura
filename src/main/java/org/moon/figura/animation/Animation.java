package org.moon.figura.animation;

import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.lua.LuaWhitelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Animation {

    protected final Map<FiguraModelPart, List<AnimationChannel>> animationParts = new HashMap<>();

    // -- player variables -- //

    private PlayState playState = PlayState.STOPPED;
    private float time;

    // -- data variables -- //

    @LuaWhitelist
    public float strength = 1f;
    @LuaWhitelist
    public float length;
    @LuaWhitelist
    public boolean loop;

    public Animation length(float length) {
        this.length = length;
        return this;
    }

    public Animation loop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public Animation addAnimation(FiguraModelPart part, AnimationChannel anim) {
        this.animationParts.computeIfAbsent(part, modelPart -> new ArrayList<>()).add(anim);
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

    public record AnimationChannel(TransformType type, Keyframe... keyframes) {}
}
