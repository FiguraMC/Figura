package org.figuramc.figura.math.vector;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.*;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.MathUtils;
import org.joml.Vector3f;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(
        name = "Vector3",
        value = "vector3"
)
public class FiguraVec3 extends FiguraVector<FiguraVec3, FiguraMat3> {

    @LuaWhitelist
    @LuaFieldDoc("vector_n.x")
    public double x;
    @LuaWhitelist
    @LuaFieldDoc("vector_n.y")
    public double y;
    @LuaWhitelist
    @LuaFieldDoc("vector_n.z")
    public double z;

    // -- cache -- // 

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec3.class
            ),
            value = "vector_n.reset"
    )
    public FiguraVec3 reset() {
        x = y = z = 0;
        return this;
    }

    public static FiguraVec3 of() {
        return new FiguraVec3();
    }

    public static FiguraVec3 of(double x, double y, double z) {
        return of().set(x, y, z);
    }

    // -- basic math -- // 

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
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "vector_n.set"
    )
    public FiguraVec3 set(Object x, double y, double z) {
        return set(LuaUtils.parseVec3("set", x, y, z));
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
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "vector_n.add"
    )
    public FiguraVec3 add(Object x, double y, double z) {
        return add(LuaUtils.parseVec3("add", x, y, z));
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
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "vector_n.sub"
    )
    public FiguraVec3 sub(Object x, double y, double z) {
        return subtract(LuaUtils.parseVec3("sub", x, y, z));
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "factor",
                    returnType = FiguraVec3.class
            ),
            value = "vector_n.offset"
    )
    public FiguraVec3 offset(double factor) {
        this.x += factor;
        this.y += factor;
        this.z += factor;
        return this;
    }

    @Override
    public FiguraVec3 multiply(FiguraVec3 other) {
        return multiply(other.x, other.y, other.z);
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
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "vector_n.mul"
    )
    public FiguraVec3 mul(Object x, double y, double z) {
        return multiply(LuaUtils.parseVec3("mul", x, y, z));
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
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "vector_n.div"
    )
    public FiguraVec3 div(Object x, double y, double z) {
        return divide(LuaUtils.parseVec3("div", x, y, z));
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
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "vector_n.reduce"
    )
    public FiguraVec3 reduce(Object x, double y, double z) {
        return reduce(LuaUtils.parseVec3("reduce", x, y, z));
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "factor",
                    returnType = FiguraVec3.class
            ),
            value = "vector_n.scale"
    )
    public FiguraVec3 scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("vector_n.unpack")
    public double[] unpack() {
        return new double[]{x, y, z};
    }

    // -- utility methods -- // 

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "mat",
                    returnType = FiguraVec3.class
            ),
            value = "vector_n.transform"
    )
    public FiguraVec3 transform(@LuaNotNil FiguraMat3 mat) {
        return set(
                mat.v11 * x + mat.v12 * y + mat.v13 * z,
                mat.v21 * x + mat.v22 * y + mat.v23 * z,
                mat.v31 * x + mat.v32 * y + mat.v33 * z
        );
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("vector_n.length_squared")
    public double lengthSquared() {
        return x * x + y * y + z * z;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec3.class
            ),
            value = "vector_n.copy"
    )
    public FiguraVec3 copy() {
        return of(x, y, z);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec"
            ),
            value = "vector_n.dot"
    )
    public double dot(@LuaNotNil FiguraVec3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec3.class
            ),
            value = "vector_n.normalize"
    )
    public FiguraVec3 normalize() {
        return super.normalize();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec3.class
            ),
            value = "vector_n.normalized"
    )
    public FiguraVec3 normalized() {
        return super.normalized();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class},
                    argumentNames = {"minLength", "maxLength"},
                    returnType = FiguraVec3.class
            ),
            value = "vector_n.clamp_length"
    )
    public FiguraVec3 clampLength(Double min, Double max) {
        return super.clampLength(min, max);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class},
                    argumentNames = {"minLength", "maxLength"},
                    returnType = FiguraVec3.class
            ),
            value = "vector_n.clamped"
    )
    public FiguraVec3 clamped(Double min, Double max) {
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
                    returnType = FiguraVec3.class
            ),
            value = "vector_n.to_rad"
    )
    public FiguraVec3 toRad() {
        return super.toRad();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraVec3.class
            ),
            value = "vector_n.to_deg"
    )
    public FiguraVec3 toDeg() {
        return super.toDeg();
    }

    @LuaWhitelist
    @LuaMethodDoc("vector_n.floor")
    public FiguraVec3 floor() {
        return FiguraVec3.of(Math.floor(x), Math.floor(y), Math.floor(z));
    }

    @LuaWhitelist
    @LuaMethodDoc("vector_n.ceil")
    public FiguraVec3 ceil() {
        return FiguraVec3.of(Math.ceil(x), Math.ceil(y), Math.ceil(z));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "func"
            ),
            value = "vector_n.apply_func"
    )
    public FiguraVec3 applyFunc(@LuaNotNil LuaFunction function) {
        x = function.call(LuaValue.valueOf(x), LuaValue.valueOf(1)).todouble();
        y = function.call(LuaValue.valueOf(y), LuaValue.valueOf(2)).todouble();
        z = function.call(LuaValue.valueOf(z), LuaValue.valueOf(3)).todouble();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "value"
                    )
            },
            value = "vector_n.augmented"
    )
    public FiguraVec4 augmented(Double d) {
        return FiguraVec4.of(x, y, z, d == null ? 1 : d);
    }

    @Override
    public int size() {
        return 3;
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

    @Override
    public double index(int i) {
        return switch (i) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            default -> throw new IndexOutOfBoundsException(i);
        };
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FiguraVec3 vec && x == vec.x && y == vec.y && z == vec.z;
    }

    @Override
    @LuaWhitelist
    public String toString() {
        return getString(x, y, z);
    }

    // -- vec3 specific -- // 

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "other"
            ),
            value = "vector3.cross"
    )
    public FiguraVec3 cross(@LuaNotNil FiguraVec3 other) {
        double nx = y * other.z - z * other.y;
        double ny = z * other.x - x * other.z;
        double nz = x * other.y - y * other.x;
        set(nx, ny, nz);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "other"
            ),
            value = "vector3.crossed"
    )
    public FiguraVec3 crossed(@LuaNotNil FiguraVec3 other) {
        return this.copy().cross(other);
    }

    public BlockPos asBlockPos() {
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }
    public static FiguraVec3 fromBlockPos(BlockPos pos) {
        return of(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vec3 asVec3() {
        return new Vec3(x, y, z);
    }
    public static FiguraVec3 fromVec3(Vec3 vec) {
        return of(vec.x, vec.y, vec.z);
    }

    public Vector3f asVec3f() {
        return new Vector3f((float) x, (float) y, (float) z);
    }
    public static FiguraVec3 fromVec3f(Vector3f vec) {
        return of(vec.x(), vec.y(), vec.z());
    }

    public boolean notNaN() {
        return !Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(z);
    }

    // -- metamethods -- // 

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
    public static FiguraVec3 __add(@LuaNotNil Object a, @LuaNotNil Object b) {
        if (a instanceof FiguraVec3 vec) {
            if (b instanceof FiguraVec3 vec2)
                return vec.plus(vec2);
            else if (b instanceof Number d)
                return vec.offseted(d.doubleValue());
        } else if (a instanceof Number d && b instanceof FiguraVec3 vec) {
            return vec.offseted(d.doubleValue());
        }
        throw new LuaError("Invalid types to __add: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
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
    public static FiguraVec3 __sub(@LuaNotNil Object a, @LuaNotNil Object b) {
        if (a instanceof FiguraVec3 vec) {
            if (b instanceof FiguraVec3 vec2)
                return vec.minus(vec2);
            else if (b instanceof Number d)
                return vec.offseted(-d.doubleValue());
        } else if (a instanceof Number d && b instanceof FiguraVec3 vec) {
            return vec.scaled(-1).offset(d.doubleValue());
        }
        throw new LuaError("Invalid types to __sub: " + a.getClass().getSimpleName() + ", " + b.getClass().getSimpleName());
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
                            types = {FiguraVec3.class, FiguraVec3.class, FiguraMat3.class}
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {FiguraVec3.class, Double.class, FiguraVec3.class}
                    )
            }
    )
    public static FiguraVec3 __mul(@LuaNotNil Object a, @LuaNotNil Object b) {
        if (a instanceof FiguraVec3 vec) {
            if (b instanceof FiguraVec3 vec2)
                return vec.times(vec2);
            else if (b instanceof Number d)
                return vec.scaled(d.doubleValue());
            else if (b instanceof FiguraMat3 mat)
                return vec.copy().transform(mat);
        } else if (a instanceof Number d && b instanceof FiguraVec3 vec) {
            return (vec.scaled(d.doubleValue()));
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
    public FiguraVec3 __div(@LuaNotNil Object rhs) {
        if (rhs instanceof Number n) {
            double d = n.doubleValue();
            if (d == 0)
                throw new LuaError("Attempt to divide vector by 0");
            return scaled(1 / d);
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
    public FiguraVec3 __mod(@LuaNotNil Object rhs) {
        if (rhs instanceof Number n) {
            double d = n.doubleValue();
            if (d == 0)
                throw new LuaError("Attempt to reduce vector by 0");
            FiguraVec3 modulus = of(d, d, d);
            return mod(modulus);
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
        return size();
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec3.class, FiguraVec3.class}
            )
    )
    public boolean __lt(@LuaNotNil FiguraVec3 r) {
        return x < r.x && y < r.y && z < r.z;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(
            overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {Boolean.class, FiguraVec3.class, FiguraVec3.class}
            )
    )
    public boolean __le(@LuaNotNil FiguraVec3 r) {
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
        if (len == 1) return switch(str.charAt(0)) {
            case '1', 'x', 'r' -> x;
            case '2', 'y', 'g' -> y;
            case '3', 'z', 'b' -> z;
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
                    ),
                    @LuaMetamethodDoc.LuaMetamethodOverload(
                            types = {void.class, FiguraVec3.class, String.class, FiguraVector.class}
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
                    case '_' -> {}
                    default -> throw new LuaError("Invalid key to __newindex: invalid swizzle character: " + key.charAt(i));
                }
            }
            return;
        }
        throw new LuaError("Invalid call to __newindex - vector swizzles must be the same size.");
    }
}
