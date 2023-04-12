package org.moon.figura.animation;

import com.mojang.datafixers.util.Pair;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.math.vector.FiguraVec3;

public class Keyframe implements Comparable<Keyframe> {

    private final Avatar owner;
    private final Animation animation;
    private final float time;
    private final Interpolation interpolation;
    private final FiguraVec3 targetA, targetB;
    private final String[] aCode, bCode;
    private final FiguraVec3 bezierLeft, bezierRight;
    private final FiguraVec3 bezierLeftTime, bezierRightTime;

    public Keyframe(Avatar owner, Animation animation, float time, Interpolation interpolation, Pair<FiguraVec3, String[]> a, Pair<FiguraVec3, String[]> b, FiguraVec3 bezierLeft, FiguraVec3 bezierRight, FiguraVec3 bezierLeftTime, FiguraVec3 bezierRightTime) {
        this.owner = owner;
        this.animation = animation;
        this.time = time;
        this.interpolation = interpolation;
        this.targetA = a.getFirst();
        this.targetB = b.getFirst();
        this.aCode = a.getSecond();
        this.bCode = b.getSecond();
        this.bezierLeft = bezierLeft;
        this.bezierRight = bezierRight;
        this.bezierLeftTime = bezierLeftTime;
        this.bezierRightTime = bezierRightTime;
    }

    public FiguraVec3 getTargetA(float delta) {
        return targetA != null ? targetA.copy() : FiguraVec3.of(parseStringData(aCode[0], delta), parseStringData(aCode[1], delta), parseStringData(aCode[2], delta));
    }

    public FiguraVec3 getTargetB(float delta) {
        return targetB != null ? targetB.copy() : FiguraVec3.of(parseStringData(bCode[0], delta), parseStringData(bCode[1], delta), parseStringData(bCode[2], delta));
    }

    private float parseStringData(String data, float delta) {
        FiguraMod.pushProfiler(data);
        try {
            return FiguraMod.popReturnProfiler(Float.parseFloat(data));
        } catch (Exception ignored) {
            if (data == null)
                return FiguraMod.popReturnProfiler(0f);

            try {
                return FiguraMod.popReturnProfiler(run(delta, "return " + data));
            } catch (Exception ignored2) {
                try {
                    return FiguraMod.popReturnProfiler(run(delta, data));
                } catch (Exception e) {
                    if (owner.luaRuntime != null)
                        owner.luaRuntime.error(e);
                }
            }
        }

        return FiguraMod.popReturnProfiler(0f);
    }

    private float run(float delta, String chunk) {
        LuaValue val = owner.loadScript("keyframe_data", chunk);
        if (val == null)
            return 0f;

        Varargs args = owner.run(val, owner.animation, delta, animation);
        try {
            return (float) args.checkdouble(1);
        } catch (Exception e) {
            if (owner.luaRuntime != null)
                owner.luaRuntime.error(e);
        }

        return 0f;
    }

    public float getTime() {
        return time;
    }

    public Interpolation getInterpolation() {
        return interpolation;
    }

    public FiguraVec3 getBezierLeft() {
        return bezierLeft.copy();
    }

    public FiguraVec3 getBezierRight() {
        return bezierRight.copy();
    }

    public FiguraVec3 getBezierLeftTime() {
        return bezierLeftTime.copy();
    }

    public FiguraVec3 getBezierRightTime() {
        return bezierRightTime.copy();
    }

    @Override
    public int compareTo(Keyframe other) {
        return Float.compare(this.getTime(), other.getTime());
    }
}
