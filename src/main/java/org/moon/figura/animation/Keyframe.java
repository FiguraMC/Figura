package org.moon.figura.animation;

import com.mojang.datafixers.util.Pair;
import org.luaj.vm2.Varargs;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.math.vector.FiguraVec3;

public class Keyframe implements Comparable<Keyframe> {

    private final Avatar owner;
    private final float time;
    private final Interpolation interpolation;
    private final FiguraVec3 targetA, targetB;
    private final String[] aCode, bCode;

    public Keyframe(Avatar owner, float time, Interpolation interpolation, Pair<FiguraVec3, String[]> a, Pair<FiguraVec3, String[]> b) {
        this.owner = owner;
        this.time = time;
        this.interpolation = interpolation;
        this.targetA = a.getFirst();
        this.targetB = b.getFirst();
        this.aCode = a.getSecond();
        this.bCode = b.getSecond();
    }

    public FiguraVec3 getTargetA() {
        return targetA != null ? targetA : FiguraVec3.of(parseStringData(aCode[0]), parseStringData(aCode[1]), parseStringData(aCode[2]));
    }

    public FiguraVec3 getTargetB() {
        return targetB != null ? targetB : FiguraVec3.of(parseStringData(bCode[0]), parseStringData(bCode[1]), parseStringData(bCode[2]));
    }

    private float parseStringData(String data) {
        FiguraMod.pushProfiler(data);
        try {
            return FiguraMod.popReturnProfiler(Float.parseFloat(data));
        } catch (Exception ignored) {
            if (data == null || owner.luaRuntime == null)
                return FiguraMod.popReturnProfiler(0f);

            try {
                Varargs val = owner.run(Pair.of("keyframe_data", "return " + data), owner.render);
                if (val.isnumber(1))
                    return FiguraMod.popReturnProfiler(val.tofloat(1));
            } catch (Exception e) {
                owner.luaRuntime.error(e);
            }
        }

        return FiguraMod.popReturnProfiler(0f);
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
