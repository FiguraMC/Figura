package org.moon.figura.lua.api.model;

import net.minecraft.client.model.EntityModel;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(
        name = "VanillaModelGroup",
        description = "vanilla_group"
)
public class VanillaGroupPart implements VanillaPart {

    private boolean visible = true;
    private final VanillaPart[] parts;

    public VanillaGroupPart(VanillaPart... parts) {
        this.parts = parts;
    }

    @Override
    public void alter(EntityModel<?> model) {
        for (VanillaPart part : parts)
            part.alter(model);
    }

    @Override
    public void store(EntityModel<?> model) {
        for (VanillaPart part : parts)
            part.store(model);
    }

    @Override
    public void restore(EntityModel<?> model) {
        for (VanillaPart part : parts)
            part.restore(model);
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
        for (VanillaPart part : parts)
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

    @Override
    public String toString() {
        return "VanillaModelGroup";
    }
}
