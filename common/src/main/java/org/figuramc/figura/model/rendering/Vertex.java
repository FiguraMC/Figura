package org.figuramc.figura.model.rendering;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "Vertex",
        value = "vertex"
)
public class Vertex {

    public float x, y, z;
    public float u, v;
    public float nx, ny, nz;

    public Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        this.x = x; this.y = y; this.z = z;
        this.u = u; this.v = v;
        this.nx = nx; this.ny = ny; this.nz = nz;
    }

    public Vertex copy() {
        return new Vertex(x, y, z, u, v, nx, ny, nz);
    }

    @LuaWhitelist
    @LuaMethodDoc("vertex.get_pos")
    public FiguraVec3 getPos() {
        return FiguraVec3.of(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "pos",
            value = "vertex.set_pos"
    )
    public Vertex setPos(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setPos", x, y, z);
        this.x = (float) vec.x;
        this.y = (float) vec.y;
        this.z = (float) vec.z;
        return this;
    }

    @LuaWhitelist
    public Vertex pos(Object x, Double y, Double z) {
        return setPos(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("vertex.get_uv")
    public FiguraVec2 getUV() {
        return FiguraVec2.of(u, v);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "UV"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"u", "v"}
                    )
            },
            aliases = "uv",
            value = "vertex.set_uv"
    )
    public Vertex setUV(Object x, Double y) {
        FiguraVec2 vec = LuaUtils.parseVec2("setUV", x, y);
        this.u = (float) vec.x;
        this.v = (float) vec.y;
        return this;
    }

    @LuaWhitelist
    public Vertex uv(Object x, Double y) {
        return setUV(x, y);
    }

    @LuaWhitelist
    @LuaMethodDoc("vertex.get_normal")
    public FiguraVec3 getNormal() {
        return FiguraVec3.of(nx, ny, nz);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "normal"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "normal",
            value = "vertex.set_normal"
    )
    public Vertex setNormal(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setNormal", x, y, z);
        setNormal(vec);
        return this;
    }

    public void setNormal(FiguraVec3 vec) {
        this.nx = (float) vec.x;
        this.ny = (float) vec.y;
        this.nz = (float) vec.z;
    }

    @LuaWhitelist
    public Vertex normal(Object x, Double y, Double z) {
        return setNormal(x, y, z);
    }

    @Override
    public String toString() {
        return "Vertex";
    }
}
