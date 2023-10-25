package org.figuramc.figura.lua.api.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@LuaWhitelist
@LuaTypeDoc(name = "JsonObject", value = "json_object")
public class FiguraJsonObject implements FiguraJsonSerializer.JsonValue {
    private final HashMap<String, LuaValue> contents = new HashMap<>();

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_object.size",
            overloads = @LuaMethodOverload(returnType = Integer.class)
    )
    public int size() {
        return contents.size();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_object.get",
            overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "key", returnType = LuaValue.class)
    )
    public LuaValue get(@LuaNotNil String key) {
        return contents.get(key);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_object.contains_key",
            overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "key", returnType = Boolean.class)
    )
    public boolean containsKey(@LuaNotNil String key) {
        return contents.containsKey(key);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_object.put",
            overloads = @LuaMethodOverload(
                    argumentTypes = {String.class, LuaValue.class},
                    argumentNames = {"key", "value"},
                    returnType = LuaValue.class
            )
    )
    public LuaValue put(@LuaNotNil String key, LuaValue value) {
        if (!JsonAPI.isSerializable(value))
            throw new IllegalArgumentException("Type %s can't be serialized".formatted(value));
        return contents.put(key, value);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_object.remove",
            overloads = @LuaMethodOverload(argumentTypes = String.class, argumentNames = "key", returnType = LuaValue.class)
    )
    public LuaValue remove(@LuaNotNil String key) {
        return contents.remove(key);
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "json_object.clear")
    public void clear() {
        contents.clear();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_object.contains_value",
            overloads = @LuaMethodOverload(argumentTypes = LuaValue.class, argumentNames = "value", returnType = boolean.class)
    )
    public boolean containsValue(LuaValue value) {
        return contents.containsValue(value);
    }

    @Override
    public JsonElement getElement() {
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, LuaValue> val :
                contents.entrySet()) {
            obj.add(val.getKey(), LuaUtils.asJsonValue(val.getValue()));
        }
        return obj;
    }

    @LuaWhitelist
    public LuaValue __index(LuaValue k) {
        if (k.type() != LuaValue.TSTRING) return null;
        String key = k.checkjstring();
        return contents.get(key);
    }

    @LuaWhitelist
    public void __newindex(LuaValue k, LuaValue v) {
        if (k.type() != LuaValue.TSTRING) return;
        String key = k.checkjstring();
        put(key, v);
    }

    @LuaWhitelist
    public Object[] __pairs() {
        return new Object[] {
            new JsonObjectIterator(this), this, LuaValue.NIL
        };
    }

    @LuaWhitelist
    public Object[] __ipairs() {
        return new Object[] {
                new EmptyIterator(), this, 0
        };
    }

    private static class JsonObjectIterator extends LuaFunction {
        private final ArrayList<String> keys = new ArrayList<>();
        public JsonObjectIterator(FiguraJsonObject obj) {
            keys.addAll(obj.contents.keySet());
        }
        @Override
        public Varargs invoke(Varargs args) {
            if (keys.size() == 0) return LuaValue.NIL;
            Object o = args.checkuserdata(1);
            if (!(o instanceof FiguraJsonObject obj)) return LuaValue.NIL;
            LuaValue a = args.arg(2);
            String k;
            if (a.isnil() || a.type() != LuaValue.TSTRING) k = keys.get(0);
            else {
                String oldKey = a.checkjstring();
                int ind = keys.indexOf(oldKey);
                if (ind < keys.size() - 1) {
                    k = keys.get(ind + 1);
                }
                else return LuaValue.NIL;
            }
            LuaValue v = obj.get(k);
            return LuaValue.varargsOf(LuaValue.valueOf(k), v);
        }
    }

    private static class EmptyIterator extends LuaFunction {
        @Override
        public Varargs invoke(Varargs args) {
            return LuaValue.NIL;
        }
    }

    @Override
    public String toString() {
        return "JsonObject";
    }
}
