package org.moon.figura.math;

import org.moon.figura.lua.LuaObject;
import org.moon.figura.lua.LuaWhitelist;
import org.terasology.jnlua.LuaState;

public class FiguraVec6 extends LuaObject {

    double x, y, z, w, t, h;

    public FiguraVec6() {
        this(0,0,0,0,0,0);
    }

    public FiguraVec6(double x, double y, double z, double w, double t, double h) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.t = t;
        this.h = h;
    }

    @Override
    protected String getRegistryKey() {
        return "figura_vector_6";
    }

    @Override
    protected void read(LuaState state, int index) {
        x = readDouble(state, index, "x");
        y = readDouble(state, index, "y");
        z = readDouble(state, index, "z");
        w = readDouble(state, index, "w");
        t = readDouble(state, index, "t");
        h = readDouble(state, index, "h");
    }

    @Override
    protected void write(LuaState state) {
        putDouble(state, "x", x);
        putDouble(state, "y", y);
        putDouble(state, "z", z);
        putDouble(state, "w", w);
        putDouble(state, "t", t);
        putDouble(state, "h", h);
    }


    //Metamethods

    @LuaWhitelist
    public static FiguraVec6 __add(FiguraVec6 arg1, FiguraVec6 arg2) {
        return new FiguraVec6(
                arg1.x + arg2.x,
                arg1.y + arg2.y,
                arg1.z + arg2.z,
                arg1.w + arg2.w,
                arg1.t + arg2.t,
                arg1.h + arg2.h
        );
    }

    @LuaWhitelist
    public static int __len(FiguraVec6 arg1) {
        return 6;
    }

    //Regular lua functions

    @LuaWhitelist
    public static double length(FiguraVec6 arg) {
        return Math.sqrt(lengthSquared(arg));
    }

    @LuaWhitelist
    public static double lengthSquared(FiguraVec6 arg) {
        return arg.x*arg.x + arg.y*arg.y + arg.z*arg.z + arg.w*arg.w + arg.t*arg.t + arg.h*arg.h;
    }
}
