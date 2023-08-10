package org.figuramc.figura.lua.api.data.json;

import com.google.gson.*;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(name = "JsonSerializer", value = "json_serializer")
public class FiguraJsonSerializer {
    public interface JsonValue {
        JsonElement getElement();
    }

    public static JsonElement getElementFromObject(Object o) {
        if (o == null) return JsonNull.INSTANCE;
        else if (o instanceof JsonValue v) return v.getElement();
        else if (o instanceof Boolean b) return new JsonPrimitive(b);
        else if (o instanceof Number n) return new JsonPrimitive(n);
        else if (o instanceof String s) return new JsonPrimitive(s);
        else if (o instanceof LuaTable tbl) return getElementFromTable(tbl);
        throw new IllegalArgumentException("Illegal argument type: %s".formatted(o.getClass().getSimpleName()));
    }

    private static JsonElement getElementFromLuaValue(LuaValue v) {
        JsonElement elem;
        if (v.isuserdata()) {
            if (v.checkuserdata() instanceof JsonValue val) {
                elem = val.getElement();
            }
            else throw new IllegalArgumentException("Illegal argument type: " + v.typename());
        }
        else {
            elem = switch (v.type()) {
                case LuaValue.TBOOLEAN -> getElementFromObject(v.checkboolean());
                case LuaValue.TINT, LuaValue.TNUMBER -> getElementFromObject(v.checkdouble());
                case LuaValue.TSTRING -> getElementFromObject(v.checkjstring());
                case LuaValue.TTABLE -> getElementFromTable(v.checktable());
                default -> throw new IllegalArgumentException("Illegal argument type: " + v.typename());
            };
        }
        return elem;
    }

    private static JsonElement getElementFromTable(LuaTable tbl) {
        if (tbl.length() > 0) {
            JsonArray arr = new JsonArray();
            for (int i = 0; i < tbl.length(); i++) {
                LuaValue v = tbl.get(i + 1);
                arr.add(getElementFromLuaValue(v));
            }
            return arr;
        }
        JsonObject obj = new JsonObject();
        for (LuaValue k :
                tbl.keys()) {
            String key = k.tojstring();
            JsonElement elem = getElementFromLuaValue(tbl.get(k));
            obj.add(key, elem);
        }
        return obj;
    }
}
