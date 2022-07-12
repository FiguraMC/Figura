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

        if (anim.time < 0f)
            return limit;

        for (Map.Entry<FiguraModelPart, List<Animation.AnimationChannel>> entry : anim.animationParts.entrySet()) {
            FiguraModelPart part = entry.getKey();

            if (part.lastAnimationPriority > anim.priority)
                continue;

            boolean merge = part.lastAnimationPriority == anim.priority;
            part.lastAnimationPriority = anim.priority;
            part.animated = true;
            part.animationOverride = part.animationOverride || anim.override;

            for (Animation.AnimationChannel channel : entry.getValue()) {
                if (limit <= 0)
                    return limit;

                Keyframe[] keyframes = channel.keyframes();

                int currentIndex = Math.max(0, Mth.binarySearch(0, keyframes.length, index -> anim.time <= keyframes[index].getTime()) - 1);
                int nextIndex = Math.min(keyframes.length - 1, currentIndex + 1);

                Keyframe current = keyframes[currentIndex];
                Keyframe next = keyframes[nextIndex];

                float timeDiff = anim.time - current.getTime();
                float delta = Math.min(Math.max(timeDiff / (next.getTime() - current.getTime()), 0), 1);

                FiguraVec3 transform = next.getInterpolation().generate(keyframes, currentIndex, nextIndex, anim.blend, delta);
                channel.type().apply(part, transform, merge);
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
            part.animationOverride = false;
        }
    }
}
