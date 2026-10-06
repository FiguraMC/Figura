package org.figuramc.figura.animation;

import com.mojang.datafixers.util.Pair;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.FiguraModelPart;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.regex.Pattern;

import static org.figuramc.figura.parsers.BlockbenchCommonTypes.FORMAT_V5;

public class Keyframe implements Comparable<Keyframe> {
    /**
     * <p>
     * wannabe <code>Either&lt;Double, LuaValue&gt;</code>
     * (because we have our own {@link org.figuramc.figura.server.utils.Either}, but it's in the server code so we can't use it)
     * </p>
     *
     * <p>(the presence or absence of {@link KeyframeValue#function} implies which value is the "real" one)</p>
     * <p>{@link KeyframeValue#chunkName} is also stored in here so that we don't have to dig it out of the function</p>
     */
    public record KeyframeValue(double literal, LuaValue function, String chunkName) {
        public static KeyframeValue literal(double literal) {
            return new KeyframeValue(literal, null, null);
        }

        public static KeyframeValue function(LuaValue function, String chunkName) {
            return new KeyframeValue(0.0, function, chunkName);
        }
    }

    private final Avatar owner;
    private final FiguraModelPart part;
    private final TransformType channel;
    private final Animation animation;
    private final float time;
    private final Interpolation interpolation;
    private final FiguraVec3 targetA, targetB;
    private final String[] aCode, bCode;
    private final FiguraVec3 bezierLeft, bezierRight;
    private final FiguraVec3 bezierLeftTime, bezierRightTime;

    private final KeyframeValue[] aCache = {null, null, null};
    private final KeyframeValue[] bCache = {null, null, null};

    public Keyframe(Avatar owner,
                    FiguraModelPart part,
                    TransformType channel,
                    Animation animation,
                    float time,
                    Interpolation interpolation,
                    Pair<FiguraVec3, String[]> a,
                    Pair<FiguraVec3, String[]> b,
                    FiguraVec3 bezierLeft,
                    FiguraVec3 bezierRight,
                    FiguraVec3 bezierLeftTime,
                    FiguraVec3 bezierRightTime) {
        this.owner = owner;
        this.part = part;
        this.channel = channel;
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
        return targetA != null ? targetA.copy() : FiguraVec3.of(getA(0, delta), getA(1, delta), getA(2, delta));
    }

    public FiguraVec3 getTargetB(float delta) {
        return targetB != null ? targetB.copy() : FiguraVec3.of(getB(0, delta), getB(1, delta), getB(2, delta));
    }

    private static final Pattern regex = Pattern.compile("^\\[string \"[^\"]*?\"]:");

    private static String trimErrorMessage(String message) {
        return regex.matcher(message).replaceAll("");
    }

    /**
     * Get a {@link KeyframeValue} for the provided source and Lua chunk name.
     *
     * @param source    source code or double literal
     * @param chunkName chunk name for debugging in case of errors
     * @return KeyframeValue if successful, or null if not (usually because the avatar isn't ready yet)
     */
    private KeyframeValue compile(String source, String chunkName) {
        try {
            try {
                return KeyframeValue.literal(Double.parseDouble(source));
            } catch (NumberFormatException fail) {
                try {
                    // Okay, so it's Lua. Try as an expression first...
                    String exprSrc = "return " + source;
                    LuaValue chunk = owner.loadScript(chunkName, exprSrc);
                    if (chunk == null) return null;
                    return KeyframeValue.function(chunk, chunkName);
                } catch (LuaError e) { /* chunk compile failed (probably syntax) */
                    try {
                        // Try as a statement.
                        LuaValue chunk = owner.loadScript(chunkName, source);
                        if (chunk == null) return null;
                        return KeyframeValue.function(chunk, chunkName);
                    } catch (LuaError e2) { /* garbage! */
                        String trailers = "";
                        if (part.formatVersion >= FORMAT_V5) {
                            // try to tell the user about the issue
                            //noinspection TextBlockMigration
                            trailers = "\n\n§6If you opened a 4.12 model file in 5.0, your keyframes might be corrupted.§r\n" +
                                    "§6You'll have to manually fix them; note that the X and Y values on rotation,§r\n" +
                                    "§6 as well as the X value on position, need to be negated.§r";
                        }
                        // text blocks are java 15+ only
                        //noinspection TextBlockMigration
                        throw new LuaError(String.format(
                                "Syntax error in keyframe [%s]:\n\n" +
                                        "Not a valid expression, because:\n%s\n" +
                                        "Not a valid block, because:\n%s\n\n" +
                                        "script:\n" +
                                        "%s" +
                                        "%s",
                                chunkName,
                                trimErrorMessage(e.getMessage()),
                                trimErrorMessage(e2.getMessage()),
                                source,
                                trailers
                        ));
                    }
                }
            }
        } catch (Exception e3) {
            // generic failure
            if (owner.luaRuntime != null) {
                owner.luaRuntime.error(e3);
            }
            return null;
        }
    }

    private double evaluate(KeyframeValue k, float delta) {
        if (k.function == null) return k.literal;
        Varargs v = owner.run(k.function, owner.animation, delta, animation);
        if (v == null)
            throw new LuaError(String.format(
                    "Tried to run animation with code [%s] without a functioning avatar runtime",
                    k.chunkName
            ));
        if (!v.isnumber(1)) throw new LuaError(String.format(
                "Failed to parse data from [%s]: expected number, but got %s",
                k.chunkName, v.arg1().typename()
        ));
        return v.todouble(1);
    }

    private double evalCompile(KeyframeValue[] cache, String[] code, int idx, float delta) {
        try {
            if (cache[idx] != null) return evaluate(cache[idx], delta);
            cache[idx] = compile(code[idx], generateChunkName(idx));
            if (cache[idx] != null) return evaluate(cache[idx], delta);
            throw new LuaError("Can't compile keyframe [" + generateChunkName(idx) + "] (code: " + code[idx] + ")");
        } catch (Exception e) {
            if (owner.luaRuntime != null) {
                owner.luaRuntime.error(e);
            }
            return 0; // fallback
        }
    }

    private double getA(int idx, float delta) {
        return evalCompile(aCache, aCode, idx, delta);
    }

    private double getB(int idx, float delta) {
        return evalCompile(bCache, bCode, idx, delta);
    }

    private String generateChunkName(int idx) {
        StringBuilder b = new StringBuilder();
        // note: scripts rely on the animation name followed by "keyframe" being at the start
        b.append(animation.getName())
                .append(" keyframe (part '");
        b.append(part.name);
        b.append("', time ").append(time).append("s, ");
        b.append(channel.name()).append(" ");
        switch (idx) {
            case 0:
                b.append("X");
                break;
            case 1:
                b.append("Y");
                break;
            case 2:
                b.append("Z");
                break;
            default:
                b.append("?");
                break;
        }
        b.append(")");
        return b.toString();
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
