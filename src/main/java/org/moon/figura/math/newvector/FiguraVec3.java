package org.moon.figura.math.newvector;

import net.minecraft.core.BlockPos;
import org.luaj.vm2.LuaDouble;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.moon.figura.math.newmatrix.FiguraMat3;
import org.moon.figura.newlua.LuaType;
import org.moon.figura.newlua.LuaWhitelist;
import org.moon.figura.newlua.docs.LuaFunctionOverload;
import org.moon.figura.newlua.docs.LuaMetamethodDoc;
import org.moon.figura.newlua.docs.LuaMethodDoc;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;

@LuaType(typeName = "vec3")
public class FiguraVec3 extends FiguraVector<FiguraVec3, FiguraMat3> {

    private final static CacheUtils.Cache<FiguraVec3> CACHE = CacheUtils.getCache(FiguraVec3::new, 500);
    public double x, y, z;

    public static FiguraVec3 of() {
        return CACHE.getFresh();
    }

    public static FiguraVec3 of(double x, double y, double z) {
        return CACHE.getFresh().set(x, y, z);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.length_squared"
    )
    public double lengthSquared() {
        return x*x + y*y + z*z;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.copy"
    )
    public FiguraVec3 copy() {
        return of(x, y, z);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.dot"
    )
    public double dot(FiguraVec3 other) {
        return x*other.x + y*other.y + z*other.z;
    }

    @Override
    public boolean equals(FiguraVec3 other) {
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public FiguraVec3 set(FiguraVec3 other) {
        return set(other.x, other.y, other.z);
    }

    public FiguraVec3 set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "vector_n.set"
    )
    public FiguraVec3 set(Object x, double y, double z) {
        if (x instanceof FiguraVec3 vec)
            return set(vec);
        if (x instanceof Number n)
            return set(n.doubleValue(), y, z);
        throw new LuaError("Illegal type to set(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec3 add(FiguraVec3 other) {
        return add(other.x, other.y, other.z);
    }

    public FiguraVec3 add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "vector_n.add"
    )
    public FiguraVec3 add(Object x, double y, double z) {
        if (x instanceof FiguraVec3 vec)
            return add(vec);
        if (x instanceof Number n)
            return add(n.doubleValue(), y, z);
        throw new LuaError("Illegal type to add(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec3 subtract(FiguraVec3 other) {
        return subtract(other.x, other.y, other.z);
    }

    public FiguraVec3 subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "vector_n.sub"
    )
    public FiguraVec3 sub(Object x, double y, double z) {
        if (x instanceof FiguraVec3 vec)
            return subtract(vec);
        if (x instanceof Number n)
            return subtract(n.doubleValue(), y, z);
        throw new LuaError("Illegal type to sub(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec3 multiply(FiguraVec3 other) {
        return multiply(other.x, other.y, other.z);
    }

    @Override
    public FiguraVec3 transform(FiguraMat3 mat) {
        return set(
                mat.v11*x+mat.v12*y+mat.v13*z,
                mat.v21*x+mat.v22*y+mat.v23*z,
                mat.v31*x+mat.v32*y+mat.v33*z
        );
    }

    public FiguraVec3 multiply(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "vector_n.mul"
    )
    public FiguraVec3 mul(Object x, double y, double z) {
        if (x instanceof FiguraVec3 vec)
            return multiply(vec);
        if (x instanceof Number n)
            return multiply(n.doubleValue(), y, z);
        throw new LuaError("Illegal type to mul(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec3 divide(FiguraVec3 other) {
        return divide(other.x, other.y, other.z);
    }

    public FiguraVec3 divide(double x, double y, double z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "vector_n.div"
    )
    public FiguraVec3 div(Object x, double y, double z) {
        if (x instanceof FiguraVec3 vec)
            return divide(vec);
        if (x instanceof Number n)
            return divide(n.doubleValue(), y, z);
        throw new LuaError("Illegal type to div(): " + x.getClass().getSimpleName());
    }

    @Override
    public FiguraVec3 reduce(FiguraVec3 other) {
        return reduce(other.x, other.y, other.z);
    }

    public FiguraVec3 reduce(double x, double y, double z) {
        this.x = ((this.x % x) + x) % x;
        this.y = ((this.y % y) + y) % y;
        this.z = ((this.z % z) + z) % z;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "vector_n.reduce"
    )
    public FiguraVec3 reduce(Object x, double y, double z) {
        if (x instanceof FiguraVec3 vec)
            return reduce(vec);
        if (x instanceof Number n)
            return reduce(n.doubleValue(), y, z);
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
    public FiguraVec3 scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        return this;
    }

    @Override
    public FiguraVec3 reset() {
        x = y = z = 0;
        return this;
    }

    @Override
    //DO NOT WHITELIST THIS ONE!
    public void free() {
        CACHE.offerOld(this);
    }


    //Vec3 specific functions
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "other"
            ),
            description = "vector3.cross"
    )
    public FiguraVec3 cross(FiguraVec3 other) {
        double nx = y * other.z - z * other.y;
        double ny = z * other.x - x * other.z;
        double nz = x * other.y - y * other.x;
        set(nx, ny, nz);
        return this;
    }

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "other"
            ),
            description = "vector3.crossed"
    )
    public FiguraVec3 crossed(FiguraVec3 other) {
        double nx = y * other.z - z * other.y;
        double ny = z * other.x - x * other.z;
        double nz = x * other.y - y * other.x;
        return FiguraVec3.of(nx, ny, nz);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector3.augmented"
    )
    public FiguraVec4 augmented() {
        return FiguraVec4.of(x, y, z, 1);
    }

    public BlockPos asBlockPos() {
        return new BlockPos(x, y, z);
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
    public FiguraVec3 clamped(Double min, Double max) {
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
    public FiguraVec3 clampLength(Double min, Double max) {
        return super.clampLength(min, max);
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.floor"
    )
    public FiguraVec3 floor() {
        return FiguraVec3.of(Math.floor(x), Math.floor(y), Math.floor(z));
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.ceil"
    )
    public FiguraVec3 ceil() {
        return FiguraVec3.of(Math.ceil(x), Math.ceil(y), Math.ceil(z));
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "func"
            ),
            description = "vector_n.apply_func"
    )
    public FiguraVec3 applyFunc(LuaFunction function) {
        x = function.call(LuaDouble.valueOf(x)).todouble();
        y = function.call(LuaDouble.valueOf(y)).todouble();
        z = function.call(LuaDouble.valueOf(z)).todouble();
        return this;
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.normalized"
    )
    public FiguraVec3 normalized() {
        return super.normalized();
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.normalize"
    )
    public FiguraVec3 normalize() {
        return super.normalize();
    }
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.reset"
    )
    public static FiguraVec3 reset(FiguraVec3 vec) { //get around method conflict, need to return this for chaining
        vec.reset();
        return vec;
    }
    @LuaWhitelist
    public String toString() {
        return "{" + x + "," + y + "," + z + "}";
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.to_rad"
    )
    public FiguraVec3 toRad() {
        return super.toRad();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "vector_n.to_deg"
    )
    public FiguraVec3 toDeg() {
        return super.toDeg();
    }


    /*
    Metamethods
     */

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {FiguraVec3.class, FiguraVec3.class, FiguraVec3.class}
            )
    )
    public FiguraVec3 __add(FiguraVec3 other) {
        return plus(other);
    }
    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {FiguraVec3.class, FiguraVec3.class, FiguraVec3.class}
            )
    )
    public FiguraVec3 __sub(FiguraVec3 other) {
        return minus(other);
    }
    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec3.class, FiguraVec3.class, FiguraVec3.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec3.class, FiguraVec3.class, Double.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec3.class, Double.class, FiguraVec3.class}
                    )
            }
    )
    public static FiguraVec3 __mul(Object a, Object b) {
        if (a instanceof FiguraVec3 vec) {
            if (b instanceof FiguraVec3 vec2) {
                return vec.times(vec2);
            } else if (b instanceof Double d) {
                return vec.scaled(d);
            }
        } else if (a instanceof Double d) {
            return ((FiguraVec3) b).scaled(d);
        }
        throw new LuaError("Invalid types to __mul: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
    }
    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec3.class, FiguraVec3.class, FiguraVec3.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec3.class, FiguraVec3.class, Double.class}
                    )
            }
    )
    public FiguraVec3 __div(Object rhs) {
        if (rhs instanceof Double d) {
            if (d == 0)
                throw new LuaError("Attempt to divide vector by 0");
            return scaled(1/d);
        } else if (rhs instanceof FiguraVec3 vec) {
            return dividedBy(vec);
        }
        throw new LuaError("Invalid types to __div: " + getClass().getSimpleName() + ", " + rhs.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec3.class, FiguraVec3.class, FiguraVec3.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec3.class, FiguraVec3.class, Double.class}
                    )
            }
    )
    public FiguraVec3 __mod(Object rhs) {
        if (rhs instanceof Double d) {
            if (d == 0)
                throw new LuaError("Attempt to reduce vector by 0");
            FiguraVec3 modulus = of(d, d, d);
            FiguraVec3 result = mod(modulus);
            modulus.free();
            return result;
        } else if (rhs instanceof FiguraVec3 vec) {
            return mod(vec);
        }
        throw new LuaError("Invalid types to __mod: " + getClass().getSimpleName() + ", " + rhs.getClass().getSimpleName());
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec3.class, FiguraVec3.class}
            )
    )
    public boolean __eq(FiguraVec3 other) {
        return equals(other);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {FiguraVec3.class, FiguraVec3.class}
            )
    )
    public FiguraVec3 __unm() {
        return scaled(-1);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Integer.class, FiguraVec3.class}
            )
    )
    public int __len() {
        return 3;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec3.class, FiguraVec3.class}
            )
    )
    public boolean __lt(FiguraVec3 r) {
        return x < r.x && y < r.y && z < r.z;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec3.class, FiguraVec3.class}
            )
    )
    public boolean __le(FiguraVec3 r) {
        return x <= r.x && y <= r.y && z <= r.z;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {String.class, FiguraVec3.class}
            )
    )
    public String __tostring() {
        return toString();
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {Double.class, FiguraVec3.class, Integer.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {Double.class, FiguraVec3.class, String.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVector.class, FiguraVec3.class, String.class},
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
            case "3", "z", "b" -> z;
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
                case '_' -> 0;
                default -> {fail = true; yield 0;}
            };
        return fail ? null : MathUtils.sizedVector(vals);
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = {
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {void.class, FiguraVec3.class, Integer.class, Double.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {void.class, FiguraVec3.class, String.class, Double.class}
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
                case "3", "z", "b" -> z = n.doubleValue();
            }
            return;
        }
        throw new LuaError("Illegal key " + str + " to __newindex()");
    }

}
