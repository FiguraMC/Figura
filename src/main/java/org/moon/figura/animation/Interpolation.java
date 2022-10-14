package org.moon.figura.animation;

import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.MathUtils;

public enum Interpolation {

    LINEAR((frames, currentFrame, targetFrame, strength, delta, type) -> {
        FiguraVec3 prev = frames[currentFrame].getTargetB();
        FiguraVec3 next = frames[targetFrame].getTargetA();
        FiguraVec3 result = FiguraVec3.of(
                MathUtils.lerp(delta, prev.x, next.x),
                MathUtils.lerp(delta, prev.y, next.y),
                MathUtils.lerp(delta, prev.z, next.z)
        );
        return getResult(result, strength, type);
    }),
    CATMULLROM((frames, currentFrame, targetFrame, strength, delta, type) -> {
        FiguraVec3 prevA = frames[Math.max(0, currentFrame - 1)].getTargetB();
        FiguraVec3 prevB = frames[currentFrame].getTargetB();
        FiguraVec3 nextA = frames[targetFrame].getTargetA();
        FiguraVec3 nextB = frames[Math.min(frames.length - 1, targetFrame + 1)].getTargetA();
        FiguraVec3 result = FiguraVec3.of(
                MathUtils.catmullrom(delta, prevA.x, prevB.x, nextA.x, nextB.x),
                MathUtils.catmullrom(delta, prevA.y, prevB.y, nextA.y, nextB.y),
                MathUtils.catmullrom(delta, prevA.z, prevB.z, nextA.z, nextB.z)
        );
        return getResult(result, strength, type);
    }),
    STEP((frames, currentFrame, targetFrame, strength, delta, type) -> getResult(frames[currentFrame].getTargetB().copy(), strength, type));

    private final IInterpolation function;

    Interpolation(IInterpolation function) {
        this.function = function;
    }

    private static FiguraVec3 getResult(FiguraVec3 result, float strength, TransformType type) {
        return type == TransformType.SCALE ? result.offset(-1).scale(strength).offset(1) : result.scale(strength);
    }

    public FiguraVec3 generate(Keyframe[] keyframes, int currentFrame, int targetFrame, float strength, float delta, TransformType type) {
        return this.function.generate(keyframes, currentFrame, targetFrame, strength, delta, type);
    }

    private interface IInterpolation {
        FiguraVec3 generate(Keyframe[] keyframes, int currentFrame, int targetFrame, float strength, float delta, TransformType type);
    }
}
