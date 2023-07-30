package org.figuramc.figura.lua.api.vanilla_model;

import net.minecraft.client.model.EntityModel;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "VanillaPart",
        value = "vanilla_part"
)
public abstract class VanillaPart {

    protected final String name;
    protected final Avatar owner;

    // transforms
    protected Boolean visible;
    protected FiguraVec3 pos, rot, scale;
    protected FiguraVec3 offsetRot, offsetScale;

    public VanillaPart(Avatar owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public boolean checkVisible() {
        return visible == null || visible;
    }

    public abstract void save(EntityModel<?> model);
    public abstract void preTransform(EntityModel<?> model);
    public abstract void posTransform(EntityModel<?> model);
    public abstract void restore(EntityModel<?> model);

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_visible")
    public Boolean getVisible() {
        return this.visible;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "visible"
            ),
            aliases = "visible",
            value = "vanilla_part.set_visible"
    )
    public VanillaPart setVisible(Boolean visible) {
        this.visible = visible;
        return this;
    }

    @LuaWhitelist
    public VanillaPart visible(Boolean visible) {
        return setVisible(visible);
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_pos")
    public FiguraVec3 getPos() {
        return pos;
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
            value = "vanilla_part.set_pos"
    )
    public VanillaPart setPos(Object x, Double y, Double z) {
        pos = LuaUtils.nullableVec3("setPos", x, y, z);
        return this;
    }

    @LuaWhitelist
    public VanillaPart pos(Object x, Double y, Double z) {
        setPos(x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_rot")
    public FiguraVec3 getRot() {
        return rot;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "rot",
            value = "vanilla_part.set_rot"
    )
    public VanillaPart setRot(Object x, Double y, Double z) {
        rot = LuaUtils.nullableVec3("setRot", x, y, z);
        return this;
    }

    @LuaWhitelist
    public VanillaPart rot(Object x, Double y, Double z) {
        setRot(x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_offset_rot")
    public FiguraVec3 getOffsetRot() {
        return offsetRot;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "offsetRot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "offsetRot",
            value = "vanilla_part.set_offset_rot"
    )
    public VanillaPart setOffsetRot(Object x, Double y, Double z) {
        offsetRot = LuaUtils.nullableVec3("setOffsetRot", x, y, z);
        return this;
    }

    @LuaWhitelist
    public VanillaPart offsetRot(Object x, Double y, Double z) {
        return setOffsetRot(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_scale")
    public FiguraVec3 getScale() {
        return scale;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "scale"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "scale",
            value = "vanilla_part.set_scale"
    )
    public VanillaPart setScale(Object x, Double y, Double z) {
        scale = x == null ? null : LuaUtils.parseOneArgVec("setScale", x, y, z, 1d);
        return this;
    }

    @LuaWhitelist
    public VanillaPart scale(Object x, Double y, Double z) {
        setScale(x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("vanilla_part.get_offset_scale")
    public FiguraVec3 getOffsetScale() {
        return offsetScale;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "offsetScale"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "offsetScale",
            value = "vanilla_part.set_offset_scale"
    )
    public VanillaPart setOffsetScale(Object x, Double y, Double z) {
        offsetScale = x == null ? null : LuaUtils.parseOneArgVec("setOffsetScale", x, y, z, 1d);
        return this;
    }

    @LuaWhitelist
    public VanillaPart offsetScale(Object x, Double y, Double z) {
        return setOffsetScale(x, y, z);
    }

    @Override
    public String toString() {
        return "VanillaPart";
    }
}
