package org.moon.figura.math.newvector;

import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.ast.Str;
import org.moon.figura.newlua.LuaType;
import org.moon.figura.newlua.LuaWhitelist;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;

@LuaType(typeName = "vec6")
public class FiguraVec6 extends FiguraVector<FiguraVec6> implements CachedType {

    private final static CacheUtils.Cache<FiguraVec6> CACHE = CacheUtils.getCache(FiguraVec6::new, 500);
    double x, y, z, w, t, h;

    public static FiguraVec6 of() {
        return CACHE.getFresh();
    }

    public static FiguraVec6 of(double x, double y, double z, double w, double t, double h) {
        FiguraVec6 result = CACHE.getFresh();
        result.set(x, y, z, w, t, h);
        return result;
    }

    @Override
    @LuaWhitelist
    public double lengthSquared() {
        return x*x + y*y + z*z + w*w + t*t + h*h;
    }

    @Override
    @LuaWhitelist
    public FiguraVec6 copy() {
        FiguraVec6 result = of();
        result.set(this);
        return result;
    }

    @Override
    @LuaWhitelist
    public double dot(FiguraVec6 other) {
        return x*other.x + y*other.y + z*other.z + w*other.w + t*other.t + h*other.h;
    }

    @Override
    @LuaWhitelist
    public boolean equals(FiguraVec6 other) {
        return x == other.x && y == other.y && z == other.z && w == other.w && t == other.t && h == other.h;
    }

    @Override
    @LuaWhitelist
    public FiguraVec6 set(FiguraVec6 other) {
        return set(other.x, other.y, other.z, other.w, other.t, other.h);
    }

    @LuaWhitelist
    public FiguraVec6 set(double x, double y, double z, double w, double t, double h) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.t = t;
        this.h = h;
        return this;
    }

    @Override
    @LuaWhitelist
    public FiguraVec6 add(FiguraVec6 other) {
        return add(other.x, other.y, other.z, other.w, other.t, other.h);
    }

    @LuaWhitelist
    public FiguraVec6 add(double x, double y, double z, double w, double t, double h) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        this.t += t;
        this.h += h;
        return this;
    }

    @Override
    @LuaWhitelist
    public FiguraVec6 subtract(FiguraVec6 other) {
        return subtract(other.x, other.y, other.z, other.w, other.t, other.h);
    }

    @LuaWhitelist
    public FiguraVec6 subtract(double x, double y, double z, double w, double t, double h) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        this.t -= t;
        this.h -= h;
        return this;
    }

    @Override
    @LuaWhitelist
    public FiguraVec6 multiply(FiguraVec6 other) {
        return multiply(other.x, other.y, other.z, other.w, other.t, other.h);
    }

    @LuaWhitelist
    public FiguraVec6 multiply(double x, double y, double z, double w, double t, double h) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        this.t *= t;
        this.h *= h;
        return this;
    }

    @Override
    @LuaWhitelist
    public FiguraVec6 divide(FiguraVec6 other) {
        return divide(other.x, other.y, other.z, other.w, other.t, other.h);
    }

    @LuaWhitelist
    public FiguraVec6 divide(double x, double y, double z, double w, double t, double h) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        this.t /= t;
        this.h /= h;
        return this;
    }

    @Override
    @LuaWhitelist
    public FiguraVec6 reduce(FiguraVec6 other) {
        return reduce(other.x, other.y, other.z, other.w, other.t, other.h);
    }

    @LuaWhitelist
    public FiguraVec6 reduce(double x, double y, double z, double w, double t, double h) {
        this.x = ((this.x % x) + x) % x;
        this.y = ((this.y % y) + y) % y;
        this.z = ((this.z % z) + z) % z;
        this.w = ((this.w % w) + w) % w;
        this.t = ((this.t % t) + t) % t;
        this.h = ((this.h % h) + h) % h;
        return this;
    }

    @Override
    @LuaWhitelist
    public FiguraVec6 scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        this.w *= factor;
        this.t *= factor;
        this.h *= factor;
        return this;
    }

    @Override
    public void reset() {
        x = y = z = w = t = h = 0;
    }

    @Override
    //DO NOT WHITELIST THIS ONE!
    public void free() {
        CACHE.offerOld(this);
    }


    /*
    Additional methods, mirroring super
     */
    @LuaWhitelist
    public double length() {
        return super.length();
    }
    @LuaWhitelist
    public FiguraVec6 clamped(Double min, Double max) {
        return super.clamped(min, max);
    }
    @LuaWhitelist
    public FiguraVec6 clampLength(Double min, Double max) {
        return super.clampLength(min, max);
    }
    @LuaWhitelist
    public FiguraVec6 floor() {
        return FiguraVec6.of(Math.floor(x), Math.floor(y), Math.floor(z), Math.floor(w), Math.floor(t), Math.floor(h));
    }
    @LuaWhitelist
    public FiguraVec6 ceil() {
        return FiguraVec6.of(Math.ceil(x), Math.ceil(y), Math.ceil(z), Math.ceil(w), Math.ceil(t), Math.ceil(h));
    }
    @LuaWhitelist
    public FiguraVec6 applyFunc(LuaFunction function) {
        x = function.call(LuaDouble.valueOf(x)).todouble();
        y = function.call(LuaDouble.valueOf(y)).todouble();
        z = function.call(LuaDouble.valueOf(z)).todouble();
        w = function.call(LuaDouble.valueOf(w)).todouble();
        t = function.call(LuaDouble.valueOf(t)).todouble();
        h = function.call(LuaDouble.valueOf(h)).todouble();
        return this;
    }
    @LuaWhitelist
    public FiguraVec6 normalized() {
        return super.normalized();
    }
    @LuaWhitelist
    public FiguraVec6 normalize() {
        return super.normalize();
    }
    @LuaWhitelist
    public static FiguraVec6 reset(FiguraVec6 vec) { //get around method conflict
        vec.reset();
        return vec;
    }
    @LuaWhitelist
    public String toString() {
        return "{" + x + "," + y + "," + z + "," + w + "," + t + "," + h + "}";
    }


    /*
    Metamethods
     */

    @LuaWhitelist
    public FiguraVec6 __add(FiguraVec6 other) {
        return plus(other);
    }
    @LuaWhitelist
    public FiguraVec6 __sub(FiguraVec6 other) {
        return minus(other);
    }
    @LuaWhitelist
    public static FiguraVec6 __mul(Object a, Object b) {
        if (a instanceof FiguraVec6 vec) {
            if (b instanceof FiguraVec6 vec2) {
                return vec.times(vec2);
            } else if (b instanceof Double d) {
                return vec.scaled(d);
            }
        } else if (a instanceof Double d) {
            return ((FiguraVec6) b).scaled(d);
        }
        throw new LuaError("Invalid types to __mul: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
    }
    @LuaWhitelist
    public FiguraVec6 __div(Object rhs) {
        if (rhs instanceof Double d) {
            if (d == 0)
                throw new LuaError("Attempt to divide vec6 by 0");
            return scaled(1/d);
        } else if (rhs instanceof FiguraVec6 vec) {
            return dividedBy(vec);
        }
        throw new LuaError("Invalid types to __div: " + getClass().getSimpleName() + ", " + rhs.getClass().getSimpleName());
    }

}
