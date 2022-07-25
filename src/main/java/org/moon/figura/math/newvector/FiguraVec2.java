package org.moon.figura.math.newvector;

import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.moon.figura.math.newmatrix.FiguraMat2;
import org.moon.figura.newlua.LuaType;
import org.moon.figura.newlua.LuaWhitelist;
import org.moon.figura.newlua.docs.LuaFunctionOverload;
import org.moon.figura.newlua.docs.LuaMetamethodDoc;
import org.moon.figura.newlua.docs.LuaMethodDoc;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;

@LuaType(typeName = "vec3")
public class FiguraVec2 extends FiguraVector<FiguraVec2, FiguraMat2> {

    private final static CacheUtils.Cache<FiguraVec2> CACHE = CacheUtils.getCache(FiguraVec2::new, 300);
    public double x, y;

    public static FiguraVec2 of() {
        return CACHE.getFresh();
    }

    public static FiguraVec2 of(double x, double y) {
        return CACHE.getFresh().set(x, y);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.length_squared"
    )
    public double lengthSquared() {
        return x*x + y*y;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.copy"
    )
    public FiguraVec2 copy() {
        return of(x, y);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec2.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.dot"
    )
    public double dot(FiguraVec2 other) {
        return x*other.x + y*other.y;
    }

    @Override
    public boolean equals(FiguraVec2 other) {
        return x == other.x && y == other.y;
    }

    @Override
    public FiguraVec2 set(FiguraVec2 other) {
        return set(other.x, other.y);
    }

    public FiguraVec2 set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "vec"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"x", "y"}
                    )
            },
            description = "vector_n.set"
    )
    public FiguraVec2 set(Object x, double y) {
        if (x instanceof FiguraVec2 vec)
            return set(vec);
        if (x instanceof Number n)
            return set(n.doubleValue(), y);
        throw new LuaError("Illegal type to set(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec2 add(FiguraVec2 other) {
        return add(other.x, other.y);
    }

    public FiguraVec2 add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "vec"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"x", "y"}
                    )
            },
            description = "vector_n.add"
    )
    public FiguraVec2 add(Object x, double y) {
        if (x instanceof FiguraVec2 vec)
            return add(vec);
        if (x instanceof Number n)
            return add(n.doubleValue(), y);
        throw new LuaError("Illegal type to add(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec2 subtract(FiguraVec2 other) {
        return subtract(other.x, other.y);
    }

    public FiguraVec2 subtract(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "vec"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"x", "y"}
                    )
            },
            description = "vector_n.sub"
    )
    public FiguraVec2 sub(Object x, double y) {
        if (x instanceof FiguraVec2 vec)
            return subtract(vec);
        if (x instanceof Number n)
            return subtract(n.doubleValue(), y);
        throw new LuaError("Illegal type to sub(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec2 multiply(FiguraVec2 other) {
        return multiply(other.x, other.y);
    }

    @Override
    public FiguraVec2 transform(FiguraMat2 mat) {
        return set(
                mat.v11*x+mat.v12*y,
                mat.v21*x+mat.v22*y
        );
    }

    public FiguraVec2 multiply(double x, double y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "vec"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"x", "y"}
                    )
            },
            description = "vector_n.mul"
    )
    public FiguraVec2 mul(Object x, double y) {
        if (x instanceof FiguraVec2 vec)
            return multiply(vec);
        if (x instanceof Number n)
            return multiply(n.doubleValue(), y);
        throw new LuaError("Illegal type to mul(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec2 divide(FiguraVec2 other) {
        return divide(other.x, other.y);
    }

    public FiguraVec2 divide(double x, double y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "vec"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"x", "y"}
                    )
            },
            description = "vector_n.div"
    )
    public FiguraVec2 div(Object x, double y) {
        if (x instanceof FiguraVec2 vec)
            return divide(vec);
        if (x instanceof Number n)
            return divide(n.doubleValue(), y);
        throw new LuaError("Illegal type to div(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec2 reduce(FiguraVec2 other) {
        return reduce(other.x, other.y);
    }

    public FiguraVec2 reduce(double x, double y) {
        this.x = ((this.x % x) + x) % x;
        this.y = ((this.y % y) + y) % y;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "vec"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"x", "y"}
                    )
            },
            description = "vector_n.reduce"
    )
    public FiguraVec2 reduce(Object x, double y) {
        if (x instanceof FiguraVec2 vec)
            return reduce(vec);
        if (x instanceof Number n)
            return reduce(n.doubleValue(), y);
        throw new LuaError("Illegal type to reduce(): " + x.getClass().getSimpleName());
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Double.class,
                    argumentNames = "factor"
            ),
            description = "vector_n.scale"
    )
    public FiguraVec2 scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        return this;
    }

    @Override
    public FiguraVec2 reset() {
        x = y = 0;
        return this;
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
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.length"
    )
    public double length() {
        return super.length();
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Double.class, Double.class},
                    argumentNames = {"minLength", "maxLength"}
            ),
            description = "vector_n.clamped"
    )
    public FiguraVec2 clamped(Double min, Double max) {
        return super.clamped(min, max);
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Double.class, Double.class},
                    argumentNames = {"minLength", "maxLength"}
            ),
            description = "vector_n.clamp_length"
    )
    public FiguraVec2 clampLength(Double min, Double max) {
        return super.clampLength(min, max);
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.floor"
    )
    public FiguraVec2 floor() {
        return FiguraVec2.of(Math.floor(x), Math.floor(y));
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.ceil"
    )
    public FiguraVec2 ceil() {
        return FiguraVec2.of(Math.ceil(x), Math.ceil(y));
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "func"
            ),
            description = "vector_n.apply_func"
    )
    public FiguraVec2 applyFunc(LuaFunction function) {
        x = function.call(LuaDouble.valueOf(x)).todouble();
        y = function.call(LuaDouble.valueOf(y)).todouble();
        return this;
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.normalized"
    )
    public FiguraVec2 normalized() {
        return super.normalized();
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.normalize"
    )
    public FiguraVec2 normalize() {
        return super.normalize();
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec2.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.reset"
    )
    public static FiguraVec2 reset(FiguraVec2 vec) { //get around method conflict, need to return this for chaining
        vec.reset();
        return vec;
    }
    @LuaWhitelist
    public String toString() {
        return "{" + x + "," + y + "}";
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.to_rad"
    )
    public FiguraVec2 toRad() {
        return super.toRad();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.to_deg"
    )
    public FiguraVec2 toDeg() {
        return super.toDeg();
    }


    /*
    Metamethods
     */

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {FiguraVec2.class, FiguraVec2.class, FiguraVec2.class}
            )
    )
    public FiguraVec2 __add(FiguraVec2 other) {
        return plus(other);
    }
    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {FiguraVec2.class, FiguraVec2.class, FiguraVec2.class}
            )
    )
    public FiguraVec2 __sub(FiguraVec2 other) {
        return minus(other);
    }
    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec2.class, FiguraVec2.class, FiguraVec2.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec2.class, FiguraVec2.class, Double.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec2.class, Double.class, FiguraVec2.class}
                    )
            }
    )
    public static FiguraVec2 __mul(Object a, Object b) {
        if (a instanceof FiguraVec2 vec) {
            if (b instanceof FiguraVec2 vec2) {
                return vec.times(vec2);
            } else if (b instanceof Double d) {
                return vec.scaled(d);
            }
        } else if (a instanceof Double d) {
            return ((FiguraVec2) b).scaled(d);
        }
        throw new LuaError("Invalid types to __mul: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
    }
    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec2.class, FiguraVec2.class, FiguraVec2.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec2.class, FiguraVec2.class, Double.class}
                    )
            }
    )
    public FiguraVec2 __div(Object rhs) {
        if (rhs instanceof Double d) {
            if (d == 0)
                throw new LuaError("Attempt to divide vector by 0");
            return scaled(1/d);
        } else if (rhs instanceof FiguraVec2 vec) {
            return dividedBy(vec);
        }
        throw new LuaError("Invalid types to __div: " + getClass().getSimpleName() + ", " + rhs.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec2.class, FiguraVec2.class, FiguraVec2.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec2.class, FiguraVec2.class, Double.class}
                    )
            }
    )
    public FiguraVec2 __mod(Object rhs) {
        if (rhs instanceof Double d) {
            if (d == 0)
                throw new LuaError("Attempt to reduce vector by 0");
            FiguraVec2 modulus = of(d, d);
            FiguraVec2 result = mod(modulus);
            modulus.free();
            return result;
        } else if (rhs instanceof FiguraVec2 vec) {
            return mod(vec);
        }
        throw new LuaError("Invalid types to __mod: " + getClass().getSimpleName() + ", " + rhs.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec2.class, FiguraVec2.class}
            )
    )
    public boolean __eq(FiguraVec2 other) {
        return equals(other);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {FiguraVec2.class, FiguraVec2.class}
            )
    )
    public FiguraVec2 __unm() {
        return scaled(-1);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Integer.class, FiguraVec2.class}
            )
    )
    public int __len() {
        return 3;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec2.class, FiguraVec2.class}
            )
    )
    public boolean __lt(FiguraVec2 r) {
        return x < r.x && y < r.y;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec2.class, FiguraVec2.class}
            )
    )
    public boolean __le(FiguraVec2 r) {
        return x <= r.x && y <= r.y;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {String.class, FiguraVec2.class}
            )
    )
    public String __tostring() {
        return toString();
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {Double.class, FiguraVec2.class, Integer.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {Double.class, FiguraVec2.class, String.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVector.class, FiguraVec2.class, String.class},
                            comment = "vector_n.comments.swizzle"
                    )
            }
    )
    public Object __index(Object arg) {
        if (arg == null)
            return null;
        String str = arg.toString();
        int len = str.length();
        if (len == 1) return switch(str) {
            case "1", "x", "r" -> x;
            case "2", "y", "g" -> y;
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
                case '_' -> 0;
                default -> {fail = true; yield 0;}
            };
        return fail ? null : MathUtils.sizedVector(vals);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {void.class, FiguraVec2.class, Integer.class, Double.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {void.class, FiguraVec2.class, String.class, Double.class}
                    )//,
//                    @LuaMetamethodDoc.LuaMetamethodOverload(
//                            types = {void.class, FiguraVec6.class, String.class, FiguraVector.class}
//                    )
            }
    )
    public void __newindex(Object key, Object value) {
        String str = key.toString();
        int len = str.length();
        if (len == 1 && value instanceof Number n)  {
            switch(str) {
                case "1", "x", "r" -> x = n.doubleValue();
                case "2", "y", "g" -> y = n.doubleValue();
            }
            return;
        }
        throw new LuaError("Illegal key " + str + " to __newindex()");
    }

}
