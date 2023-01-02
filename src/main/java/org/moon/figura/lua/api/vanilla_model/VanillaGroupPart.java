package org.moon.figura.lua.api.vanilla_model;

import net.minecraft.client.model.EntityModel;
import org.moon.figura.avatar.Avatar;
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

    public VanillaGroupPart(Avatar owner, String name, VanillaPart... parts) {
        super(owner, name);
        partMap = new HashMap<>();
        for (VanillaPart part : parts)
            partMap.put(part.name, part);
        cachedParts = partMap.values();
    }

    @Override
    public void change(EntityModel<?> model) {
        for (VanillaPart part : cachedParts)
            part.change(model);
    }

    @Override
    public void save(EntityModel<?> model) {
        for (VanillaPart part : cachedParts)
            part.save(model);
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
    public void setVisible(Boolean visible) {
        this.visible = visible;
        for (VanillaPart part : cachedParts)
            part.setVisible(visible);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("vanilla_group.get_visible")
    public Boolean getVisible() {
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
