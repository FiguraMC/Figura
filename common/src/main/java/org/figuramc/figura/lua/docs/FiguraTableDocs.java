package org.figuramc.figura.lua.docs;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.data.DeepCopyTransformer;
import org.luaj.vm2.LuaValue;

/**
 * Documentation for additions to {@code table}
 */
@LuaTypeDoc(
        name = "table",
        value = "table"
)
public class FiguraTableDocs {

    /**
     * @deprecated Do not actually use - this is a stub!
     */
    @LuaWhitelist
    @LuaMethodDoc(
            value = "table.deepcopy",
            overloads = @LuaMethodOverload(
                    returnType = LuaValue.class,
                    argumentNames = {"input", "metatables", "copyExtra"},
                    argumentTypes = {LuaValue.class, DeepCopyTransformer.MetatableRule.class, Boolean.class}
            )
    )
    @Deprecated
    public LuaValue deepcopy() { return null; }
}
