package org.figuramc.figura.lua.api.nameplate;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.util.Arrays;
import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "NameplateCustomizationGroup",
        value = "nameplate_group"
)
public class NameplateCustomizationGroup {

    private final List<NameplateCustomization> customizations;

    public NameplateCustomizationGroup(NameplateCustomization... customizations) {
        this.customizations = Arrays.stream(customizations).toList();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            value = "nameplate_group.set_text"
    )
    public NameplateCustomizationGroup setText(String text) {
        for (NameplateCustomization customization : customizations)
            customization.setText(text);
        return this;
    }

    @Override
    public String toString() {
        return "NameplateCustomizationGroup";
    }
}
