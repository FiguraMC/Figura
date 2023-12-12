package org.figuramc.figura.lua.api.json;

import com.google.gson.JsonArray;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.ArrayList;

import static org.figuramc.figura.lua.api.json.JsonAPI.isSerializable;

@LuaWhitelist
@LuaTypeDoc(name = "JsonArray", value = "json_array")
public class FiguraJsonArray implements FiguraJsonSerializer.JsonValue {
    private final ArrayList<LuaValue> contents = new ArrayList<>();

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.get",
            overloads = @LuaMethodOverload(argumentTypes = Integer.class, argumentNames = "index", returnType = LuaValue.class)
    )
    public LuaValue get(@LuaNotNil Integer i) {
        return contents.get(i-1);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.size",
            overloads = @LuaMethodOverload(returnType = Integer.class)
    )
    public int size() {
        return contents.size();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.contains",
            overloads = @LuaMethodOverload(argumentTypes = LuaValue.class, argumentNames = "elem", returnType = Boolean.class)
    )
    public boolean contains(LuaValue o) {
        return contents.contains(o);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.add",
            overloads = @LuaMethodOverload(argumentTypes = LuaValue.class, argumentNames = "elem", returnType = Boolean.class)
    )
    public boolean add(LuaValue o) {
        if (!isSerializable(o)) throw new IllegalArgumentException("Type %s can't be serialized".formatted(o.typename()));
        return contents.add(o);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.insert",
            overloads = @LuaMethodOverload(
                    argumentTypes = {Integer.class, LuaValue.class},
                    argumentNames = {"index", "elem"}
            )
    )
    public void insert(@LuaNotNil Integer i, LuaValue o) {
        if (!isSerializable(o)) throw new IllegalArgumentException("Type %s can't be serialized".formatted(o.typename()));
        contents.add(i-1, o);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.set",
            overloads = @LuaMethodOverload(
                    argumentTypes = {Integer.class, LuaValue.class},
                    argumentNames = {"index", "elem"}
            )
    )
    public void set(@LuaNotNil Integer i, LuaValue o) {
        if (!isSerializable(o)) throw new IllegalArgumentException("Type %s can't be serialized".formatted(o.typename()));
        contents.set(i-1, o);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.remove_at",
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "index",
                    returnType = LuaValue.class
            )
    )
    public LuaValue removeAt(@LuaNotNil Integer index) {
        return contents.remove(index-1);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.remove",
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaValue.class,
                    argumentNames = "elem",
                    returnType = Boolean.class
            )
    )
    public boolean remove(LuaValue o) {
        return contents.remove(o);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.index_of",
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaValue.class,
                    argumentNames = "elem",
                    returnType = Integer.class
            )
    )
    public int indexOf(LuaValue o) {
        return contents.indexOf(o)+1;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.last_index_of",
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaValue.class,
                    argumentNames = "elem",
                    returnType = Integer.class
            )
    )
    public int lastIndexOf(LuaValue o) {
        return contents.lastIndexOf(o)+1;
    }

    @LuaWhitelist
    @LuaMethodDoc("json_array.clear")
    public void clear() {
        contents.clear();
    }

    @LuaWhitelist
    public Object __index(LuaValue k) {
        if (k.isint()) {
            int ind = k.checkint();
            return get(ind);
        }
        return null;
    }

    @LuaWhitelist
    public void __newindex(LuaValue k, LuaValue o) {
        if (k.isint()) {
            int ind = k.checkint();
            if (ind < 1 || ind > size() + 1) throw new IndexOutOfBoundsException("Index must be in range [1; %s+1], got %s".formatted(size(), ind));
            if (ind == size()+1) add(o);
            else set(ind, o);
        }
    }

    @LuaWhitelist
    public int __len() {
        return size();
    }

    @LuaWhitelist
    public Object[] __ipairs() {
        return new Object[] {
                new JsonArrayIterator(this), this, 0
        };
    }

    @LuaWhitelist
    public Object[] __pairs() {
        return __ipairs();
    }

    @Override
    public JsonArray getElement() {
        JsonArray arr = new JsonArray();
        for (LuaValue o :
                contents) {
            arr.add(LuaUtils.asJsonValue(o));
        }
        return arr;
    }


    private static class JsonArrayIterator extends LuaFunction {
        private final FiguraJsonArray array;
        public JsonArrayIterator(FiguraJsonArray array) {
            this.array = array;
        }
        @Override
        public Varargs invoke(Varargs args) {
            LuaValue tbl = args.arg1();
            int ind = args.checkint(2) + 1;
            if (ind > array.size()) return LuaValue.NIL;
            LuaValue v = tbl.get(ind);
            return LuaValue.varargsOf(LuaValue.valueOf(ind), v);
        }
    }

    @Override
    public String toString() {
        return "JsonArray";
    }
}
