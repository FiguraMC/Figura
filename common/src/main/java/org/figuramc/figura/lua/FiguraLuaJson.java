package org.figuramc.figura.lua;

import com.google.gson.*;
import org.figuramc.figura.utils.JsonUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.Objects;
import java.util.function.Function;

public class FiguraLuaJson {
    public static void loadFunctions(FiguraLuaRuntime runtime) {
        LuaValue print = PARSE_JSON_FUNCTION.apply(runtime);
        runtime.setGlobal("parseJson", print);

        LuaValue printJson = TO_JSON_FUNCTION.apply(runtime);
        runtime.setGlobal("toJson", printJson);
    }

    private static final Function<FiguraLuaRuntime, LuaValue> PARSE_JSON_FUNCTION = runtime -> new VarArgFunction() {
        @Override
        public Varargs invoke(Varargs args) {
            return jsonStringToTable(args.checkjstring(1));
        }

        @Override
        public String tojstring() {
            return "function: parseJson";
        }
    };
    private static final Function<FiguraLuaRuntime, LuaValue> TO_JSON_FUNCTION = runtime -> new VarArgFunction() {
        @Override
        public Varargs invoke(Varargs args) {
            return LuaValue.valueOf(tableToJsonString(args.arg(1)));
        }

        @Override
        public String tojstring() {
            return "function: toJson";
        }
    };

    public static LuaValue jsonStringToTable(String json) {
        JsonElement jsonElement;
        try {
            jsonElement = JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            throw new LuaError("Failed to parse malformed Json: " + e.getCause());
        }
        return JsonUtils.asLuaValue(jsonElement);
    }

    public static String tableToJsonString(LuaValue value) {
        return Objects.requireNonNullElse(LuaUtils.asJsonValue(value), "null").toString();
    }
}
