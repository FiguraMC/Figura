package org.figuramc.figura.math.vector;

import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.*;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.utils.MathUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(
        name = "Vector4",
        value = "vector4"
)
public class FiguraVec4 extends FiguraVector<FiguraVec4, FiguraMat4> {

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

    // -- cache -- // 

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec4.class
            ),
            value = "vector_n.reset"
    )
    public FiguraVec4 reset() {
        x = y = z = w = 0;
        return this;
    }

    public static FiguraVec4 of() {
        return new FiguraVec4();
    }

    public static FiguraVec4 of(double x, double y, double z, double w) {
        return of().set(x, y, z, w);
    }

    // -- basic math -- // 

    @Override
    public FiguraVec4 set(FiguraVec4 other) {
        return set(other.x, other.y, other.z, other.w);
    }

    public FiguraVec4 set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec4.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w"}
                    )
            },
            value = "vector_n.set"
    )
    public FiguraVec4 set(@LuaNotNil Object x, double y, double z, double w) {
        if (x instanceof FiguraVec4 vec)
            return set(vec);
        if (x instanceof Number n)
            return set(n.doubleValue(), y, z, w);
        throw new LuaError("Illegal type to set(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec4 add(FiguraVec4 other) {
        return add(other.x, other.y, other.z, other.w);
    }

    public FiguraVec4 add(double x, double y, double z, double w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec4.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w"}
                    )
            },
            value = "vector_n.add"
    )
    public FiguraVec4 add(@LuaNotNil Object x, double y, double z, double w) {
        if (x instanceof FiguraVec4 vec)
            return add(vec);
        if (x instanceof Number n)
            return add(n.doubleValue(), y, z, w);
        throw new LuaError("Illegal type to add(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec4 subtract(FiguraVec4 other) {
        return subtract(other.x, other.y, other.z, other.w);
    }

    public FiguraVec4 subtract(double x, double y, double z, double w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec4.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w"}
                    )
            },
            value = "vector_n.sub"
    )
    public FiguraVec4 sub(@LuaNotNil Object x, double y, double z, double w) {
        if (x instanceof FiguraVec4 vec)
            return subtract(vec);
        if (x instanceof Number n)
            return subtract(n.doubleValue(), y, z, w);
        throw new LuaError("Illegal type to sub(): " + x.getClass().getSimpleName());
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "factor",
                    returnType = FiguraVec4.class
            ),
            value = "vector_n.offset"
    )
    public FiguraVec4 offset(double factor) {
        this.x += factor;
        this.y += factor;
        this.z += factor;
        this.w += factor;
        return this;
    }

    @Override
    public FiguraVec4 multiply(FiguraVec4 other) {
        return multiply(other.x, other.y, other.z, other.w);
    }

    public FiguraVec4 multiply(double x, double y, double z, double w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec4.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w"}
                    )
            },
            value = "vector_n.mul"
    )
    public FiguraVec4 mul(@LuaNotNil Object x, double y, double z, double w) {
        if (x instanceof FiguraVec4 vec)
            return multiply(vec);
        if (x instanceof Number n)
            return multiply(n.doubleValue(), y, z, w);
        throw new LuaError("Illegal type to mul(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec4 divide(FiguraVec4 other) {
        return divide(other.x, other.y, other.z, other.w);
    }

    public FiguraVec4 divide(double x, double y, double z, double w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec4.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w"}
                    )
            },
            value = "vector_n.div"
    )
    public FiguraVec4 div(@LuaNotNil Object x, double y, double z, double w) {
        if (x instanceof FiguraVec4 vec)
            return divide(vec);
        if (x instanceof Number n)
            return divide(n.doubleValue(), y, z, w);
        throw new LuaError("Illegal type to div(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec4 reduce(FiguraVec4 other) {
        return reduce(other.x, other.y, other.z, other.w);
    }

    public FiguraVec4 reduce(double x, double y, double z, double w) {
        this.x = ((this.x % x) + x) % x;
        this.y = ((this.y % y) + y) % y;
        this.z = ((this.z % z) + z) % z;
        this.w = ((this.w % w) + w) % w;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec4.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w"}
                    )
            },
            value = "vector_n.reduce"
    )
    public FiguraVec4 reduce(@LuaNotNil Object x, double y, double z, double w) {
        if (x instanceof FiguraVec4 vec)
            return reduce(vec);
        if (x instanceof Number n)
            return reduce(n.doubleValue(), y, z, w);
        throw new LuaError("Illegal type to reduce(): " + x.getClass().getSimpleName());
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "factor",
                    returnType = FiguraVec4.class
            ),
            value = "vector_n.scale"
    )
    public FiguraVec4 scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        this.w *= factor;
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("vector_n.unpack")
    public double[] unpack() {
        return new double[]{x, y, z, w};
    }

    // -- utility methods -- // 

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "mat",
                    returnType = FiguraVec4.class
            ),
            value = "vector_n.transform"
    )
    public FiguraVec4 transform(@LuaNotNil FiguraMat4 mat) {
        return set(
                mat.v11 * x + mat.v12 * y + mat.v13 * z + mat.v14 * w,
                mat.v21 * x + mat.v22 * y + mat.v23 * z + mat.v24 * w,
                mat.v31 * x + mat.v32 * y + mat.v33 * z + mat.v34 * w,
                mat.v41 * x + mat.v42 * y + mat.v43 * z + mat.v44 * w
        );
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("vector_n.length_squared")
    public double lengthSquared() {
        return x * x + y * y + z * z + w * w;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec4.class
            ),
            value = "vector_n.copy"
    )
    public FiguraVec4 copy() {
        return of(x, y, z, w);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraVec4.class,
                    argumentNames = "vec"
            ),
            value = "vector_n.dot"
    )
    public double dot(@LuaNotNil FiguraVec4 other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec4.class
            ),
            value = "vector_n.normalize"
    )
    public FiguraVec4 normalize() {
        return super.normalize();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec4.class
            ),
            value = "vector_n.normalized"
    )
    public FiguraVec4 normalized() {
        return super.normalized();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class},
                    argumentNames = {"minLength", "maxLength"},
                    returnType = FiguraVec4.class
            ),
            value = "vector_n.clamp_length"
    )
    public FiguraVec4 clampLength(Double min, Double max) {
        return super.clampLength(min, max);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class},
                    argumentNames = {"minLength", "maxLength"},
                    returnType = FiguraVec4.class
            ),
            value = "vector_n.clamped"
    )
    public FiguraVec4 clamped(Double min, Double max) {
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
                    returnType = FiguraVec4.class
            ),
            value = "vector_n.to_rad"
    )
    public FiguraVec4 toRad() {
        return super.toRad();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec4.class
            ),
            value = "vector_n.to_deg"
    )
    public FiguraVec4 toDeg() {
        return super.toDeg();
    }

    @LuaWhitelist
    @LuaMethodDoc("vector_n.floor")
    public FiguraVec4 floor() {
        return FiguraVec4.of(Math.floor(x), Math.floor(y), Math.floor(z), Math.floor(w));
    }

    @LuaWhitelist
    @LuaMethodDoc("vector_n.ceil")
    public FiguraVec4 ceil() {
        return FiguraVec4.of(Math.ceil(x), Math.ceil(y), Math.ceil(z), Math.ceil(w));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "func"
            ),
            value = "vector_n.apply_func"
    )
    public FiguraVec4 applyFunc(@LuaNotNil LuaFunction function) {
        x = function.call(LuaValue.valueOf(x), LuaValue.valueOf(1)).todouble();
        y = function.call(LuaValue.valueOf(y), LuaValue.valueOf(2)).todouble();
        z = function.call(LuaValue.valueOf(z), LuaValue.valueOf(3)).todouble();
        w = function.call(LuaValue.valueOf(w), LuaValue.valueOf(4)).todouble();
        return this;
    }

    @Override
    public int size() {
        return 4;
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

    @Override
    public double index(int i) {
        return switch (i) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            case 3 -> w;
            default -> throw new IndexOutOfBoundsException(i);
        };
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FiguraVec4 vec && x == vec.x && y == vec.y && z == vec.z && w == vec.w;
    }

    @Override
    @LuaWhitelist
    public String toString() {
        return getString(x, y, z, w);
    }

    // -- metamethods -- // 

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, FiguraVec4.class, FiguraVec4.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, FiguraVec4.class, Double.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, Double.class, FiguraVec4.class}
                    )
            }
    )
    public static FiguraVec4 __add(@LuaNotNil Object a, @LuaNotNil Object b) {
        if (a instanceof FiguraVec4 vec) {
            if (b instanceof FiguraVec4 vec2)
                return vec.plus(vec2);
            else if (b instanceof Number d)
                return vec.offseted(d.doubleValue());
        } else if (a instanceof Number d && b instanceof FiguraVec4 vec) {
            return vec.offseted(d.doubleValue());
        }
        throw new LuaError("Invalid types to __add: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, FiguraVec4.class, FiguraVec4.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, FiguraVec4.class, Double.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, Double.class, FiguraVec4.class}
                    )
            }
    )
    public static FiguraVec4 __sub(@LuaNotNil Object a, @LuaNotNil Object b) {
        if (a instanceof FiguraVec4 vec) {
            if (b instanceof FiguraVec4 vec2)
                return vec.minus(vec2);
            else if (b instanceof Number d)
                return vec.offseted(-d.doubleValue());
        } else if (a instanceof Number d && b instanceof FiguraVec4 vec) {
            return vec.scaled(-1).offset(d.doubleValue());
        }
        throw new LuaError("Invalid types to __sub: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, FiguraVec4.class, FiguraVec4.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, FiguraVec4.class, Double.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, FiguraVec4.class, FiguraMat4.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, Double.class, FiguraVec4.class}
                    )
            }
    )
    public static FiguraVec4 __mul(@LuaNotNil Object a, @LuaNotNil Object b) {
        if (a instanceof FiguraVec4 vec) {
            if (b instanceof FiguraVec4 vec2)
                return vec.times(vec2);
            else if (b instanceof Number d)
                return vec.scaled(d.doubleValue());
            else if (b instanceof FiguraMat4 mat)
                return vec.copy().transform(mat);
        } else if (a instanceof Number d && b instanceof FiguraVec4 vec) {
            return (vec.scaled(d.doubleValue()));
        }
        throw new LuaError("Invalid types to __mul: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, FiguraVec4.class, FiguraVec4.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, FiguraVec4.class, Double.class}
                    )
            }
    )
    public FiguraVec4 __div(@LuaNotNil Object rhs) {
        if (rhs instanceof Number n) {
            double d = n.doubleValue();
            if (d == 0)
                throw new LuaError("Attempt to divide vector by 0");
            return scaled(1 / d);
        } else if (rhs instanceof FiguraVec4 vec) {
            return dividedBy(vec);
        }
        throw new LuaError("Invalid types to __div: " + getClass().getSimpleName() + ", " + rhs.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, FiguraVec4.class, FiguraVec4.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec4.class, FiguraVec4.class, Double.class}
                    )
            }
    )
    public FiguraVec4 __mod(@LuaNotNil Object rhs) {
        if (rhs instanceof Number n) {
            double d = n.doubleValue();
            if (d == 0)
                throw new LuaError("Attempt to reduce vector by 0");
            FiguraVec4 modulus = of(d, d, d, d);
            return mod(modulus);
        } else if (rhs instanceof FiguraVec4 vec) {
            return mod(vec);
        }
        throw new LuaError("Invalid types to __mod: " + getClass().getSimpleName() + ", " + rhs.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec4.class, FiguraVec4.class}
            )
    )
    public boolean __eq(FiguraVec4 other) {
        return equals(other);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {FiguraVec4.class, FiguraVec4.class}
            )
    )
    public FiguraVec4 __unm() {
        return scaled(-1);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Integer.class, FiguraVec4.class}
            )
    )
    public int __len() {
        return size();
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec4.class, FiguraVec4.class}
            )
    )
    public boolean __lt(@LuaNotNil FiguraVec4 r) {
        return x < r.x && y < r.y && z < r.z && w < r.w;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec4.class, FiguraVec4.class}
            )
    )
    public boolean __le(@LuaNotNil FiguraVec4 r) {
        return x <= r.x && y <= r.y && z <= r.z && w <= r.w;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {String.class, FiguraVec4.class}
            )
    )
    public String __tostring() {
        return toString();
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {Double.class, FiguraVec4.class, Integer.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {Double.class, FiguraVec4.class, String.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVector.class, FiguraVec4.class, String.class},
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
            case '_' -> 0;
            default -> null;
        };

        if (len > 4)
            return null;
        double[] vals = new double[len];
        boolean fail = false;
        for (int i = 0; i < len; i++)
            vals[i] = switch (str.charAt(i)) {
                case '1', 'x', 'r' -> x;
                case '2', 'y', 'g' -> y;
                case '3', 'z', 'b' -> z;
                case '4', 'w', 'a' -> w;
                case '_' -> 0;
                default -> {fail = true; yield 0;}
            };
        return fail ? null : MathUtils.sizedVector(vals);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {void.class, FiguraVec4.class, Integer.class, Double.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {void.class, FiguraVec4.class, String.class, Double.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {void.class, FiguraVec4.class, String.class, FiguraVector.class}
                    )
            }
    )
    public void __newindex(@LuaNotNil String key, Object value) {
        int len = key.length();
        if (len == 1)  {
            if (value instanceof Number n) {
                double d = n.doubleValue();
                switch(key) {
                    case "1", "x", "r" -> x = d;
                    case "2", "y", "g" -> y = d;
                    case "3", "z", "b" -> z = d;
                    case "4", "w", "a" -> w = d;
                    case "_" -> {}
                    default -> throw new LuaError("Invalid key to vector __newindex: " + key);
                }
                return;
            }
            throw new LuaError("Invalid call to __newindex - value assigned to key " + key + " must be number.");
        }
        if (value instanceof FiguraVector<?,?> vecVal && len == vecVal.size()) {
            double[] vals = new double[] {vecVal.x(), vecVal.y(), vecVal.z(), vecVal.w()};
            for (int i = 0; i < len; i++) {
                switch (key.charAt(i)) {
                    case '1', 'x', 'r' -> x = vals[i];
                    case '2', 'y', 'g' -> y = vals[i];
                    case '3', 'z', 'b' -> z = vals[i];
                    case '4', 'w', 'a' -> w = vals[i];
                    case '_' -> {}
                    default -> throw new LuaError("Invalid key to __newindex: invalid swizzle character: " + key.charAt(i));
                }
            }
            return;
        }
        throw new LuaError("Invalid call to __newindex - vector swizzles must be the same size.");
    }
}
