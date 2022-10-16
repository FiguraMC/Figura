package org.moon.figura.math.vector;

import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.*;
import org.moon.figura.lua.docs.LuaMetamethodDoc.LuaMetamethodOverload;
import org.moon.figura.math.matrix.FiguraMatrix;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.caching.CacheUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "Vector5",
        value = "vector5"
)
public class FiguraVec5 extends FiguraVector<FiguraVec5, FiguraMatrix.DummyMatrix<FiguraVec5>> {

    @LuaWhitelist
    @LuaFieldDoc("vector_n.x")
    public double x;
    @LuaWhitelist
    @LuaFieldDoc("vector_n.y")
    public double y;
    @LuaWhitelist
    @LuaFieldDoc("vector_n.z")
    public double z;
    @LuaWhitelist
    @LuaFieldDoc("vector_n.w")
    public double w;
    @LuaWhitelist
    @LuaFieldDoc("vector_n.t")
    public double t;

    // -- cache -- //

    private final static CacheUtils.Cache<FiguraVec5> CACHE = CacheUtils.getCache(FiguraVec5::new, 300);

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec5.class
            ),
            value = "vector_n.reset"
    )
    public FiguraVec5 reset() {
        x = y = z = w = t = 0;
        return this;
    }

    @Override
    public void free() {
        CACHE.offerOld(this);
    }

    public static FiguraVec5 of() {
        return CACHE.getFresh();
    }

    public static FiguraVec5 of(double x, double y, double z, double w, double t) {
        return CACHE.getFresh().set(x, y, z, w, t);
    }

    // -- basic math -- //

    @Override
    public FiguraVec5 set(FiguraVec5 other) {
        return set(other.x, other.y, other.z, other.w, other.t);
    }

    public FiguraVec5 set(double x, double y, double z, double w, double t) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.t = t;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec5.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w", "t"}
                    )
            },
            value = "vector_n.set"
    )
    public FiguraVec5 set(@LuaNotNil Object x, double y, double z, double w, double t) {
        if (x instanceof FiguraVec5 vec)
            return set(vec);
        if (x instanceof Number n)
            return set(n.doubleValue(), y, z, w, t);
        throw new LuaError("Illegal type to set(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec5 add(FiguraVec5 other) {
        return add(other.x, other.y, other.z, other.w, other.t);
    }

    public FiguraVec5 add(double x, double y, double z, double w, double t) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        this.t += t;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec5.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w", "t"}
                    )
            },
            value = "vector_n.add"
    )
    public FiguraVec5 add(@LuaNotNil Object x, double y, double z, double w, double t) {
        if (x instanceof FiguraVec5 vec)
            return add(vec);
        if (x instanceof Number n)
            return add(n.doubleValue(), y, z, w, t);
        throw new LuaError("Illegal type to add(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec5 subtract(FiguraVec5 other) {
        return subtract(other.x, other.y, other.z, other.w, other.t);
    }

    public FiguraVec5 subtract(double x, double y, double z, double w, double t) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        this.t -= t;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec5.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w", "t"}
                    )
            },
            value = "vector_n.sub"
    )
    public FiguraVec5 sub(@LuaNotNil Object x, double y, double z, double w, double t) {
        if (x instanceof FiguraVec5 vec)
            return subtract(vec);
        if (x instanceof Number n)
            return subtract(n.doubleValue(), y, z, w, t);
        throw new LuaError("Illegal type to sub(): " + x.getClass().getSimpleName());
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "factor",
                    returnType = FiguraVec5.class
            ),
            value = "vector_n.offset"
    )
    public FiguraVec5 offset(double factor) {
        this.x += factor;
        this.y += factor;
        this.z += factor;
        this.w += factor;
        this.t += factor;
        return this;
    }

    @Override
    public FiguraVec5 multiply(FiguraVec5 other) {
        return multiply(other.x, other.y, other.z, other.w, other.t);
    }
    public FiguraVec5 multiply(double x, double y, double z, double w, double t) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        this.t *= t;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec5.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w", "t"}
                    )
            },
            value = "vector_n.mul"
    )
    public FiguraVec5 mul(@LuaNotNil Object x, double y, double z, double w, double t) {
        if (x instanceof FiguraVec5 vec)
            return multiply(vec);
        if (x instanceof Number n)
            return multiply(n.doubleValue(), y, z, w, t);
        throw new LuaError("Illegal type to mul(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec5 divide(FiguraVec5 other) {
        return divide(other.x, other.y, other.z, other.w, other.t);
    }

    public FiguraVec5 divide(double x, double y, double z, double w, double t) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        this.t /= t;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec5.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w", "t"}
                    )
            },
            value = "vector_n.div"
    )
    public FiguraVec5 div(@LuaNotNil Object x, double y, double z, double w, double t) {
        if (x instanceof FiguraVec5 vec)
            return divide(vec);
        if (x instanceof Number n)
            return divide(n.doubleValue(), y, z, w, t);
        throw new LuaError("Illegal type to div(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec5 reduce(FiguraVec5 other) {
        return reduce(other.x, other.y, other.z, other.w, other.t);
    }

    public FiguraVec5 reduce(double x, double y, double z, double w, double t) {
        this.x = ((this.x % x) + x) % x;
        this.y = ((this.y % y) + y) % y;
        this.z = ((this.z % z) + z) % z;
        this.w = ((this.w % w) + w) % w;
        this.t = ((this.t % t) + t) % t;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec5.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w", "t"}
                    )
            },
            value = "vector_n.reduce"
    )
    public FiguraVec5 reduce(@LuaNotNil Object x, double y, double z, double w, double t) {
        if (x instanceof FiguraVec5 vec)
            return reduce(vec);
        if (x instanceof Number n)
            return reduce(n.doubleValue(), y, z, w, t);
        throw new LuaError("Illegal type to reduce(): " + x.getClass().getSimpleName());
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "factor",
                    returnType = FiguraVec5.class
            ),
            value = "vector_n.scale"
    )
    public FiguraVec5 scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        this.w *= factor;
        this.t *= factor;
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("vector_n.unpack")
    public double[] unpack() {
        return new double[]{x, y, z, w, t};
    }

    // -- utility methods -- //

    @Override
    public FiguraVec5 transform(FiguraMatrix.DummyMatrix<FiguraVec5> matrix) {
        throw new IllegalStateException("Called bad method, cannot transform a FiguraVec5");
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("vector_n.length_squared")
    public double lengthSquared() {
        return x * x + y * y + z * z + w * w + t * t;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec5.class
            ),
            value = "vector_n.copy"
    )
    public FiguraVec5 copy() {
        return of(x, y, z, w, t);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraVec5.class,
                    argumentNames = "vec"
            ),
            value = "vector_n.dot"
    )
    public double dot(@LuaNotNil FiguraVec5 other) {
        return x * other.x + y * other.y + z * other.z + w * other.w + t * other.t;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec5.class
            ),
            value = "vector_n.normalize"
    )
    public FiguraVec5 normalize() {
        return super.normalize();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec5.class
            ),
            value = "vector_n.normalized"
    )
    public FiguraVec5 normalized() {
        return super.normalized();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class},
                    argumentNames = {"minLength", "maxLength"},
                    returnType = FiguraVec5.class
            ),
            value = "vector_n.clamp_length"
    )
    public FiguraVec5 clampLength(Double min, Double max) {
        return super.clampLength(min, max);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class},
                    argumentNames = {"minLength", "maxLength"},
                    returnType = FiguraVec5.class
            ),
            value = "vector_n.clamped"
    )
    public FiguraVec5 clamped(Double min, Double max) {
        return super.clamped(min, max);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("vector_n.length")
    public double length() {
        return super.length();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec5.class
            ),
            value = "vector_n.to_rad"
    )
    public FiguraVec5 toRad() {
        return super.toRad();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec5.class
            ),
            value = "vector_n.to_deg"
    )
    public FiguraVec5 toDeg() {
        return super.toDeg();
    }

    @LuaWhitelist
    @LuaMethodDoc("vector_n.floor")
    public FiguraVec5 floor() {
        return FiguraVec5.of(Math.floor(x), Math.floor(y), Math.floor(z), Math.floor(w), Math.floor(t));
    }

    @LuaWhitelist
    @LuaMethodDoc("vector_n.ceil")
    public FiguraVec5 ceil() {
        return FiguraVec5.of(Math.ceil(x), Math.ceil(y), Math.ceil(z), Math.ceil(w), Math.ceil(t));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "func"
            ),
            value = "vector_n.apply_func"
    )
    public FiguraVec5 applyFunc(@LuaNotNil LuaFunction function) {
        x = function.call(LuaDouble.valueOf(x)).todouble();
        y = function.call(LuaDouble.valueOf(y)).todouble();
        z = function.call(LuaDouble.valueOf(z)).todouble();
        w = function.call(LuaDouble.valueOf(w)).todouble();
        t = function.call(LuaDouble.valueOf(t)).todouble();
        return this;
    }

    @Override
    public int size() {
        return 5;
    }

    public double x() {
        return x;
    }
    public double y() {
        return y;
    }
    public double z() {
        return z;
    }
    public double w() {
        return w;
    }
    public double t() {
        return t;
    }

    @Override
    public double index(int i) {
        return switch (i) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            case 3 -> w;
            case 4 -> t;
            default -> throw new IndexOutOfBoundsException(i);
        };
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FiguraVec5 vec && x == vec.x && y == vec.y && z == vec.z && w == vec.w && t == vec.t;
    }

    @Override
    @LuaWhitelist
    public String toString() {
        return getString(x, y, z, w, t);
    }

    // -- metamethods -- //

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodOverload(
                            types = {FiguraVec5.class, FiguraVec5.class, FiguraVec5.class}
                    ),
                    @LuaMetamethodOverload(
                            types = {FiguraVec5.class, FiguraVec5.class, Double.class}
                    ),
                    @LuaMetamethodOverload(
                            types = {FiguraVec5.class, Double.class, FiguraVec5.class}
                    )
            }
    )
    public static FiguraVec5 __add(@LuaNotNil Object a, @LuaNotNil Object b) {
        if (a instanceof FiguraVec5 vec) {
            if (b instanceof FiguraVec5 vec2)
                return vec.plus(vec2);
            else if (b instanceof Number d)
                return vec.offseted(d.doubleValue());
        } else if (a instanceof Number d && b instanceof FiguraVec5 vec) {
            return vec.offseted(d.doubleValue());
        }
        throw new LuaError("Invalid types to __add: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodOverload(
                            types = {FiguraVec5.class, FiguraVec5.class, FiguraVec5.class}
                    ),
                    @LuaMetamethodOverload(
                            types = {FiguraVec5.class, FiguraVec5.class, Double.class}
                    ),
                    @LuaMetamethodOverload(
                            types = {FiguraVec5.class, Double.class, FiguraVec5.class}
                    )
            }
    )
    public static FiguraVec5 __sub(@LuaNotNil Object a, @LuaNotNil Object b) {
        if (a instanceof FiguraVec5 vec) {
            if (b instanceof FiguraVec5 vec2)
                return vec.minus(vec2);
            else if (b instanceof Number d)
                return vec.offseted(-d.doubleValue());
        } else if (a instanceof Number d && b instanceof FiguraVec5 vec) {
            return vec.scaled(-1).offset(d.doubleValue());
        }
        throw new LuaError("Invalid types to __sub: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodOverload(
                            types = {FiguraVec5.class, FiguraVec5.class, FiguraVec5.class}
                    ),
                    @LuaMetamethodOverload(
                            types = {FiguraVec5.class, FiguraVec5.class, Double.class}
                    ),
                    @LuaMetamethodOverload(
                            types = {FiguraVec5.class, Double.class, FiguraVec5.class}
                    )
            }
    )
    public static FiguraVec5 __mul(@LuaNotNil Object a, @LuaNotNil Object b) {
        if (a instanceof FiguraVec5 vec) {
            if (b instanceof FiguraVec5 vec2)
                return vec.times(vec2);
            else if (b instanceof Number d)
                return vec.scaled(d.doubleValue());
        } else if (a instanceof Number d && b instanceof FiguraVec5 vec) {
            return (vec.scaled(d.doubleValue()));
        }
        throw new LuaError("Invalid types to __mul: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodOverload(
                            types = {FiguraVec5.class, FiguraVec5.class, FiguraVec5.class}
                    ),
                    @LuaMetamethodOverload(
                            types = {FiguraVec5.class, FiguraVec5.class, Double.class}
                    )
            }
    )
    public FiguraVec5 __div(@LuaNotNil Object rhs) {
        if (rhs instanceof Number n) {
            double d = n.doubleValue();
            if (d == 0)
                throw new LuaError("Attempt to divide vector by 0");
            return scaled(1 / d);
        } else if (rhs instanceof FiguraVec5 vec) {
            return dividedBy(vec);
        }
        throw new LuaError("Invalid types to __div: " + getClass().getSimpleName() + ", " + rhs.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodOverload(
                            types = {FiguraVec5.class, FiguraVec5.class, FiguraVec5.class}
                    ),
                    @LuaMetamethodOverload(
                            types = {FiguraVec5.class, FiguraVec5.class, Double.class}
                    )
            }
    )
    public FiguraVec5 __mod(@LuaNotNil Object rhs) {
        if (rhs instanceof Number n) {
            double d = n.doubleValue();
            if (d == 0)
                throw new LuaError("Attempt to reduce vector by 0");
            FiguraVec5 modulus = of(d, d, d, d, d);
            FiguraVec5 result = mod(modulus);
            modulus.free();
            return result;
        } else if (rhs instanceof FiguraVec5 vec) {
            return mod(vec);
        }
        throw new LuaError("Invalid types to __mod: " + getClass().getSimpleName() + ", " + rhs.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec5.class, FiguraVec5.class}
            )
    )
    public boolean __eq(FiguraVec5 other) {
        return equals(other);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodOverload(
                    types = {FiguraVec5.class, FiguraVec5.class}
            )
    )
    public FiguraVec5 __unm() {
        return scaled(-1);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodOverload(
                    types = {Integer.class, FiguraVec5.class}
            )
    )
    public int __len() {
        return size();
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec5.class, FiguraVec5.class}
            )
    )
    public boolean __lt(@LuaNotNil FiguraVec5 r) {
        return x < r.x && y < r.y && z < r.z && w < r.w && t < r.t;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec5.class, FiguraVec5.class}
            )
    )
    public boolean __le(@LuaNotNil FiguraVec5 r) {
        return x <= r.x && y <= r.y && z <= r.z && w <= r.w && t <= r.t;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodOverload(
                    types = {String.class, FiguraVec5.class}
            )
    )
    public String __tostring() {
        return toString();
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodOverload(
                            types = {Double.class, FiguraVec5.class, Integer.class}
                    ),
                    @LuaMetamethodOverload(
                            types = {Double.class, FiguraVec5.class, String.class}
                    ),
                    @LuaMetamethodOverload(
                            types = {FiguraVector.class, FiguraVec5.class, String.class},
                            comment = "vector_n.comments.swizzle"
                    )
            }
    )
    public Object __index(Object arg) {
        if (arg == null)
            return null;
        String str = arg.toString();
        int len = str.length();
        if (len == 1) return switch(str.charAt(0)) {
            case '1', 'x', 'r' -> x;
            case '2', 'y', 'g' -> y;
            case '3', 'z', 'b' -> z;
            case '4', 'w', 'a' -> w;
            case '5', 't' -> t;
            case '_' -> 0;
            default -> null;
        };

        if (len > 6)
            return null;
        double[] vals = new double[len];
        boolean fail = false;
        for (int i = 0; i < len; i++)
            vals[i] = switch (str.charAt(i)) {
                case '1', 'x', 'r' -> x;
                case '2', 'y', 'g' -> y;
                case '3', 'z', 'b' -> z;
                case '4', 'w', 'a' -> w;
                case '5', 't' -> t;
                case '_' -> 0;
                default -> {fail = true; yield 0;}
            };
        return fail ? null : MathUtils.sizedVector(vals);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodOverload(
                            types = {void.class, FiguraVec5.class, Integer.class, Double.class}
                    ),
                    @LuaMetamethodOverload(
                            types = {void.class, FiguraVec5.class, String.class, Double.class}
                    ),
                    @LuaMetamethodOverload(
                            types = {void.class, FiguraVec5.class, String.class, FiguraVector.class}
                    )
            }
    )
    public void __newindex(String key, Object value) {
        if (key == null) return;
        int len = key.length();
        if (len == 1)  {
            if (value instanceof Number n) {
                double d = n.doubleValue();
                switch(key) {
                    case "1", "x", "r" -> x = d;
                    case "2", "y", "g" -> y = d;
                    case "3", "z", "b" -> z = d;
                    case "4", "w", "a" -> w = d;
                    case "5", "t" -> t = d;
                    case "_" -> {}
                    default -> throw new LuaError("Invalid key to vector __newindex: " + key);
                }
                return;
            }
            throw new LuaError("Invalid call to __newindex - value assigned to key " + key + " must be number.");
        }
        if (value instanceof FiguraVector<?,?> vecVal && len == vecVal.size()) {
            double[] vals = new double[] {vecVal.x(), vecVal.y(), vecVal.z(), vecVal.w(), vecVal.t(), vecVal.h()};
            for (int i = 0; i < len; i++) {
                switch (key.charAt(i)) {
                    case '1', 'x', 'r' -> x = vals[i];
                    case '2', 'y', 'g' -> y = vals[i];
                    case '3', 'z', 'b' -> z = vals[i];
                    case '4', 'w', 'a' -> w = vals[i];
                    case '5', 't' -> t = vals[i];
                    case '_' -> {}
                    default -> throw new LuaError("Invalid key to __newindex: invalid swizzle character: " + key.charAt(i));
                }
            }
            return;
        }
        throw new LuaError("Invalid call to __newindex - vector swizzles must be the same size.");
    }
}
