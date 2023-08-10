package org.figuramc.figura.lua.api.data.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.ArrayList;

@LuaWhitelist
@LuaTypeDoc(name = "JsonArray", value = "json_array")
public class FiguraJsonArray implements FiguraJsonSerializer.JsonValue {
    private final ArrayList<Object> contents = new ArrayList<>();

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.get",
            overloads = @LuaMethodOverload(argumentTypes = Integer.class, argumentNames = "index", returnType = Object.class)
    )
    public Object get(int i) {
        return contents.get(i-1);
    }

    @LuaWhitelist
    @LuaMethodDoc("json_array.size")
    public int size() {
        return contents.size();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.size",
            overloads = @LuaMethodOverload(argumentTypes = Object.class, argumentNames = "elem", returnType = Boolean.class)
    )
    public boolean contains(Object o) {
        return contents.contains(o);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.add",
            overloads = @LuaMethodOverload(argumentTypes = Object.class, argumentNames = "elem", returnType = Boolean.class)
    )
    public boolean add(Object o) {
        return contents.add(o);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.insert",
            overloads = @LuaMethodOverload(
                    argumentTypes = {Integer.class, Object.class},
                    argumentNames = {"index", "elem"}
            )
    )
    public void insert(int i, Object o) {
        contents.add(i-1, o);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.set",
            overloads = @LuaMethodOverload(
                    argumentTypes = {Integer.class, Object.class},
                    argumentNames = {"index", "elem"}
            )
    )
    public void set(int i, Object o) {
        contents.set(i-1, o);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.remove_at",
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "index",
                    returnType = Object.class
            )
    )
    public Object removeAt(int index) {
        return contents.remove(index-1);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.remove",
            overloads = @LuaMethodOverload(
                    argumentTypes = Object.class,
                    argumentNames = "elem",
                    returnType = Boolean.class
            )
    )
    public boolean remove(Object o) {
        return contents.remove(o);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.index_of",
            overloads = @LuaMethodOverload(
                    argumentTypes = Object.class,
                    argumentNames = "elem",
                    returnType = Integer.class
            )
    )
    public int indexOf(Object o) {
        return contents.indexOf(o)+1;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "json_array.last_index_of",
            overloads = @LuaMethodOverload(
                    argumentTypes = Object.class,
                    argumentNames = "elem",
                    returnType = Integer.class
            )
    )
    public int lastIndexOf(Object o) {
        return contents.lastIndexOf(o)+1;
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
    public void __newindex(LuaValue k, Object o) {
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

    @Override
    public JsonArray getElement() {
        JsonArray arr = new JsonArray();
        for (Object o :
                contents) {
            arr.add(FiguraJsonSerializer.getElementFromObject(o));
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
            if (ind > array.size() + 1) return LuaValue.NIL;
            LuaValue v = tbl.get(ind);
            return LuaValue.varargsOf(LuaValue.valueOf(ind), v);
        }
    }
}
