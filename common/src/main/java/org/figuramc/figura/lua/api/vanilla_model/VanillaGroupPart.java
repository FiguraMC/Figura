package org.figuramc.figura.lua.api.vanilla_model;

import net.minecraft.client.model.EntityModel;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;

import java.util.Collection;
import java.util.HashMap;

@LuaWhitelist
@LuaTypeDoc(
        name = "VanillaModelGroup",
        value = "vanilla_group_part"
)
public class VanillaGroupPart extends VanillaPart {

    private final Collection<VanillaPart> cachedParts;
    private final HashMap<String, VanillaPart> partMap;

    public VanillaGroupPart(Avatar owner, String name, VanillaPart... parts) {
        super(owner, name);
        partMap = new HashMap<>();
        for (VanillaPart part : parts)
            partMap.put(part.name, part);
        cachedParts = partMap.values();
    }

    @Override
    public void save(EntityModel<?> model) {
        for (VanillaPart part : cachedParts)
            part.save(model);
    }

    @Override
    public void preTransform(EntityModel<?> model) {
        for (VanillaPart part : cachedParts)
            part.preTransform(model);
    }

    @Override
    public void posTransform(EntityModel<?> model) {
        for (VanillaPart part : cachedParts)
            part.posTransform(model);
    }

    @Override
    public void restore(EntityModel<?> model) {
        for (VanillaPart part : cachedParts)
            part.restore(model);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "visible"
            ),
            aliases = "visible",
            value = "vanilla_group_part.set_visible"
    )
    public VanillaPart setVisible(Boolean visible) {
        for (VanillaPart part : cachedParts)
            part.setVisible(visible);
        return super.setVisible(visible);
    }

    @Override
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
            value = "vanilla_group_part.set_pos"
    )
    public VanillaPart setPos(Object x, Double y, Double z) {
        for (VanillaPart part : cachedParts)
            part.setPos(x, y, z);
        return super.setPos(x, y, z);
    }

    @Override
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
            value = "vanilla_group_part.set_rot"
    )
    public VanillaPart setRot(Object x, Double y, Double z) {
        for (VanillaPart part : cachedParts)
            part.setRot(x, y, z);
        return super.setRot(x, y, z);
    }

    @Override
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
            value = "vanilla_group_part.set_offset_rot"
    )
    public VanillaPart setOffsetRot(Object x, Double y, Double z) {
        for (VanillaPart part : cachedParts)
            part.setOffsetRot(x, y, z);
        return super.setOffsetRot(x, y, z);
    }

    @Override
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
            value = "vanilla_group_part.set_scale"
    )
    public VanillaPart setScale(Object x, Double y, Double z) {
        for (VanillaPart part : cachedParts)
            part.setScale(x, y, z);
        return super.setScale(x, y, z);
    }

    @Override
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
            value = "vanilla_group_part.set_offset_scale"
    )
    public VanillaPart setOffsetScale(Object x, Double y, Double z) {
        for (VanillaPart part : cachedParts)
            part.setOffsetScale(x, y, z);
        return super.setOffsetScale(x, y, z);
    }

    @LuaWhitelist
    public Object __index(String key) {
        return partMap.get(key);
    }

    @Override
    public String toString() {
        return "VanillaModelGroup";
    }
}
