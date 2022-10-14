package org.moon.figura.lua.api.vanilla_model;

import net.minecraft.client.model.EntityModel;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;

import java.util.Collection;
import java.util.HashMap;

@LuaWhitelist
@LuaTypeDoc(
        name = "VanillaModelGroup",
        value = "vanilla_group"
)
public class VanillaGroupPart extends VanillaPart {

    private final Collection<VanillaPart> cachedParts;
    private final HashMap<String, VanillaPart> partMap;

    private boolean visible = true;

    public VanillaGroupPart(String name, VanillaPart... parts) {
        super(name);
        partMap = new HashMap<>();
        for (VanillaPart part : parts)
            partMap.put(part.name, part);
        cachedParts = partMap.values();
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
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "visible"
            ),
            value = "vanilla_group.set_visible"
    )
    public void setVisible(boolean visible) {
        this.visible = visible;
        for (VanillaPart part : cachedParts)
            part.setVisible(visible);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("vanilla_group.get_visible")
    public boolean getVisible() {
        return this.visible;
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
