package org.moon.figura.lua.api.model;

import net.minecraft.client.model.EntityModel;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaPairsIterator;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "VanillaModelGroup",
        description = "vanilla_group"
)
public class VanillaGroupPart extends VanillaPart {

    private final List<String> cachedKeyList;
    private final Collection<VanillaPart> cachedParts;
    private final HashMap<String, VanillaPart> partMap;

    private boolean visible = true;

    public VanillaGroupPart(String name, VanillaPart... parts) {
        super(name);
        partMap = new HashMap<>();
        for (VanillaPart part : parts)
            partMap.put(part.name, part);
        cachedParts = partMap.values();
        cachedKeyList = partMap.keySet().stream().toList();
    }

    @Override
    public void alter(EntityModel<?> model) {
        for (VanillaPart part : cachedParts)
            part.alter(model);
    }

    @Override
    public void store(EntityModel<?> model) {
        for (VanillaPart part : cachedParts)
            part.store(model);
    }

    @Override
    public void restore(EntityModel<?> model) {
        for (VanillaPart part : cachedParts)
            part.restore(model);
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
        for (VanillaPart part : cachedParts)
            part.setVisible(visible);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {VanillaGroupPart.class, Boolean.class},
                    argumentNames = {"vanillaGroup", "visible"}
            ),
            description = "vanilla_group.set_visible"
    )
    public static void setVisible(@LuaNotNil VanillaGroupPart group, @LuaNotNil Boolean visible) {
        group.setVisible(visible);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = VanillaGroupPart.class,
                    argumentNames = "vanillaGroup"
            ),
            description = "vanilla_group.get_visible"
    )
    public static Boolean getVisible(@LuaNotNil VanillaGroupPart group) {
        return group.isVisible();
    }

    @LuaWhitelist
    public static Object __index(@LuaNotNil VanillaGroupPart part, @LuaNotNil String arg) {
        return part.partMap.get(arg);
    }

    @LuaWhitelist
    public static LuaPairsIterator<VanillaGroupPart, String> __pairs(@LuaNotNil VanillaGroupPart group) {
        return new LuaPairsIterator<>(group.cachedKeyList, VanillaGroupPart.class, String.class);
    }

    @Override
    public String toString() {
        return "VanillaModelGroup";
    }
}
