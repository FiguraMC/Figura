package org.moon.figura.lua.api.nameplate;

import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;

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
    public void setText(@LuaNotNil String text) {
        for (NameplateCustomization customization : customizations)
            customization.setText(text);
    }

    @Override
    public String toString() {
        return "NameplateCustomizationGroup";
    }
}
