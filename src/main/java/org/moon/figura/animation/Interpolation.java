package org.moon.figura.animation;

import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.MathUtils;

public enum Interpolation {

    LINEAR((frames, currentFrame, targetFrame, strength, delta) -> {
        FiguraVec3 prev = frames[currentFrame].getTargetB();
        FiguraVec3 next = frames[targetFrame].getTargetA();
        return FiguraVec3.of(
                MathUtils.lerp(delta, prev.x, next.x) * strength,
                MathUtils.lerp(delta, prev.y, next.y) * strength,
                MathUtils.lerp(delta, prev.z, next.z) * strength
        );
    }),
    CATMULLROM((frames, currentFrame, targetFrame, strength, delta) -> {
        FiguraVec3 prevA = frames[Math.max(0, currentFrame - 1)].getTargetB();
        FiguraVec3 prevB = frames[currentFrame].getTargetB();
        FiguraVec3 nextA = frames[targetFrame].getTargetA();
        FiguraVec3 nextB = frames[Math.min(frames.length - 1, targetFrame + 1)].getTargetA();
        return FiguraVec3.of(
                MathUtils.catmullrom(delta, prevA.x, prevB.x, nextA.x, nextB.x) * strength,
                MathUtils.catmullrom(delta, prevA.y, prevB.y, nextA.y, nextB.y) * strength,
                MathUtils.catmullrom(delta, prevA.z, prevB.z, nextA.z, nextB.z) * strength
        );
    });

    private final IInterpolation function;

    Interpolation(IInterpolation function) {
        this.function = function;
    }

    public FiguraVec3 generate(Keyframe[] keyframes, int currentFrame, int targetFrame, float strength, float delta) {
        return this.function.generate(keyframes, currentFrame, targetFrame, strength, delta);
    }

    private interface IInterpolation {
        FiguraVec3 generate(Keyframe[] keyframes, int currentFrame, int targetFrame, float strength, float delta);
    }
}
