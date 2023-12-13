package org.figuramc.figura.lua.api.json;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(name = "JsonAPI", value = "json")
public class JsonAPI {

    public static JsonAPI INSTANCE = new JsonAPI();

    private JsonAPI() { }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json.new_builder",
            overloads = @LuaMethodOverload(
                    returnType = FiguraJsonBuilder.class
            )
    )
    public static FiguraJsonBuilder newBuilder() {
        return new FiguraJsonBuilder();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json.new_array",
            overloads = @LuaMethodOverload(
                    returnType = FiguraJsonArray.class
            )
    )
    public static FiguraJsonArray newArray() {
        return new FiguraJsonArray();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json.new_object",
            overloads = @LuaMethodOverload(
                    returnType = FiguraJsonObject.class
            )
    )
    public static FiguraJsonObject newObject() {
        return new FiguraJsonObject();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json.is_serializable",
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaValue.class,
                    argumentNames = "val",
                    returnType = Boolean.class
            )
    )
    public static boolean isSerializable(LuaValue val) {
        return switch (val.type()) {
            case LuaValue.TNIL, LuaValue.TBOOLEAN, LuaValue.TINT, LuaValue.TNUMBER, LuaValue.TSTRING,
                    LuaValue.TTABLE -> true;
            case LuaValue.TUSERDATA -> val.checkuserdata() instanceof FiguraJsonSerializer.JsonValue;
            default -> false;
        };
    }

    @Override
    public String toString() {
        return "JsonAPI";
    }
}
