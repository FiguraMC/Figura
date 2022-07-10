package org.moon.figura.animation;

import org.moon.figura.math.vector.FiguraVec3;

public class Keyframe implements Comparable<Keyframe> {

    private final float time;
    private final Interpolation interpolation;
    private final FiguraVec3 targetA, targetB;

    public Keyframe(float timeStamp, Interpolation interpolation, FiguraVec3 target) {
        this(timeStamp, interpolation, target, target);
    }

    public Keyframe(float time, Interpolation interpolation, FiguraVec3 targetA, FiguraVec3 targetB) {
        this.time = time;
        this.interpolation = interpolation;
        this.targetA = targetA;
        this.targetB = targetB;
    }

    public FiguraVec3 getTargetA() {
        return targetA;
    }

    public FiguraVec3 getTargetB() {
        return targetB;
    }

    public float getTime() {
        return time;
    }

    public Interpolation getInterpolation() {
        return interpolation;
    }

    @Override
    public int compareTo(Keyframe other) {
        return Float.compare(this.getTime(), other.getTime());
    }
}
