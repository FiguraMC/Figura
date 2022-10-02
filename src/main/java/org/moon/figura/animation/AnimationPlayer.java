package org.moon.figura.animation;

import net.minecraft.util.Mth;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.math.vector.FiguraVec3;

import java.util.List;
import java.util.Map;

public class AnimationPlayer {

    public static int tick(Animation anim, int limit) {
        if (anim.playState == Animation.PlayState.STOPPED)
            return limit;

        if (anim.playState != Animation.PlayState.PAUSED)
            anim.tick();

        for (Map.Entry<FiguraModelPart, List<Animation.AnimationChannel>> entry : anim.animationParts.entrySet()) {
            FiguraModelPart part = entry.getKey();

            if (part.lastAnimationPriority > anim.priority)
                continue;

            boolean merge = part.lastAnimationPriority == anim.priority;
            part.lastAnimationPriority = anim.priority;
            part.animated = true;

            for (Animation.AnimationChannel channel : entry.getValue()) {
                if (limit <= 0)
                    return limit;

                Keyframe[] keyframes = channel.keyframes();

                int currentIndex = Math.max(0, Mth.binarySearch(0, keyframes.length, index -> anim.frameTime <= keyframes[index].getTime()) - 1);
                int nextIndex = Math.min(keyframes.length - 1, currentIndex + 1);

                Keyframe current = keyframes[currentIndex];
                Keyframe next = keyframes[nextIndex];

                float timeDiff = anim.frameTime - current.getTime();
                float delta = Math.min(Math.max(timeDiff / (next.getTime() - current.getTime()), 0), 1);
                if (Float.isNaN(delta))
                    delta = 0;

                TransformType type = channel.type();
                FiguraVec3 transform = current.getInterpolation().generate(keyframes, currentIndex, nextIndex, anim.blend, delta, type);
                type.apply(part, transform, merge);

                if (anim.override) {
                    switch (type) {
                        case ROTATION -> part.animationOverride |= 1;
                        case POSITION -> part.animationOverride |= 2;
                        case SCALE -> part.animationOverride |= 4;
                    }
                }

                limit--;
            }
        }

        return limit;
    }

    public static void clear(Animation anim) {
        FiguraVec3 zero = FiguraVec3.of();
        for (FiguraModelPart part : anim.animationParts.keySet()) {
            if (!part.animated)
                continue;

            part.animPosition(zero, false);
            part.animRotation(zero, false);
            part.animScale(FiguraVec3.of(1, 1, 1), false);
            part.lastAnimationPriority = Integer.MIN_VALUE;
            part.animated = false;
            part.animationOverride = 0;
        }
    }
}
