package org.figuramc.figura.lua.api.json;

import com.google.gson.GsonBuilder;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(name = "JsonBuilder", value = "json_builder")
public class FiguraJsonBuilder {
    @LuaFieldDoc("json_builder.pretty_printing")
    public boolean prettyPrinting = false;
    @LuaFieldDoc("json_builder.html_escaping")
    public boolean htmlEscaping = true;
    @LuaFieldDoc("json_builder.serialize_nils")
    public boolean serializeNils = false;

    @LuaWhitelist
    @LuaMethodDoc("json_builder.build")
    public FiguraJsonSerializer build() {
        GsonBuilder builder = new GsonBuilder();
        if (prettyPrinting) builder.setPrettyPrinting();
        if (!htmlEscaping) builder.disableHtmlEscaping();
        if (serializeNils) builder.serializeNulls();
        return new FiguraJsonSerializer(builder.create());
    }

    @LuaWhitelist
    public Object __index(LuaValue k) {
        if (k.type() != LuaValue.TSTRING)
            return null;
        switch (k.checkjstring()) {
            case "prettyPrinting":
                return prettyPrinting;
            case "htmlEscaping":
                return htmlEscaping;
            case "serializeNils":
                return serializeNils;
            default:
                return null;
        }
    }

    @LuaWhitelist
    public void __newindex(LuaValue k, LuaValue v) {
        if (k.type() == LuaValue.TSTRING) {
            switch (k.checkjstring()) {
                case "prettyPrinting":
                    prettyPrinting = v.checkboolean();
                    break;
                case "htmlEscaping":
                    htmlEscaping = v.checkboolean();
                    break;
                case "serializeNils":
                    serializeNils = v.checkboolean();
                    break;
            }
        }
    }

    @Override
    public String toString() {
        return "JsonBuilder";
    }
}
