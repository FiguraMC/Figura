package org.moon.figura.animation;

import net.minecraft.util.Mth;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.math.vector.FiguraVec3;

import java.util.List;
import java.util.Map;

public class Player {

    public static void tick(Animation anim, long timeNow) {
        if (anim.getPlayState() != Animation.PlayState.PLAYING)
            return;

        anim.updateTime(timeNow);

        for (Map.Entry<FiguraModelPart, List<Animation.AnimationChannel>> entry : anim.animationParts.entrySet()) {
            FiguraModelPart part = entry.getKey();
            List<Animation.AnimationChannel> channels = entry.getValue();

            for (Animation.AnimationChannel channel : channels) {
                Keyframe[] keyframes = channel.keyframes();

                int currentIndex = Math.max(0, Mth.binarySearch(0, keyframes.length, index -> anim.getTime() <= keyframes[index].getTime()) - 1);
                int nextIndex = Math.min(keyframes.length - 1, currentIndex + 1);

                Keyframe current = keyframes[currentIndex];
                Keyframe next = keyframes[nextIndex];

                float timeDiff = anim.getTime() - current.getTime();
                float delta = Math.min(Math.max(timeDiff / (next.getTime() - current.getTime()), 0), 1);

                FiguraVec3 transform = next.getInterpolation().generate(keyframes, currentIndex, nextIndex, anim.strength, delta);
                channel.type().apply(part, transform);
            }
        }
    }
}
