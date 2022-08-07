package org.moon.figura.lua.api.nameplate;

import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

import java.util.Arrays;
import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "NameplateGroupCust",
        description = "nameplate_group"
)
public class NameplateGroupCust {

    private final List<NameplateCustomization> customizations;

    public NameplateGroupCust(NameplateCustomization... customizations) {
        this.customizations = Arrays.stream(customizations).toList();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            description = "nameplate_group.set_text"
    )
    public void setText(@LuaNotNil String text) {
        for (NameplateCustomization customization : customizations)
            customization.setText(text);
    }

    @Override
    public String toString() {
        return "NameplateGroupCust";
    }
}
