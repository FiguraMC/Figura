package org.moon.figura.lua;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * One LuaTypeManager per LuaRuntime, so that people can be allowed to edit the metatables within.
 */
public class LuaTypeManager {

    private final Map<Class<?>, LuaTable> metatables = new HashMap<>();

    public void generateMetatableFor(Class<?> clazz) {
        if (metatables.containsKey(clazz))
            return;
        if (!clazz.isAnnotationPresent(LuaWhitelist.class))
            throw new IllegalArgumentException("Tried to generate metatable for un-whitelisted class " + clazz.getName() + "!");

        //Ensure that all whitelisted superclasses are loaded before this one
        try {
            generateMetatableFor(clazz.getSuperclass());
        } catch (IllegalArgumentException ignored) {}

        LuaTable metatable = new LuaTable();

        LuaTable indexTable = new LuaTable();
        Class<?> currentClass = clazz;
        while (currentClass.isAnnotationPresent(LuaWhitelist.class)) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(LuaWhitelist.class)) {
                    continue;
                }
                String name = method.getName();
                if (name.startsWith("__")) { //metamethods
                    if (metatable.rawget(name) == LuaValue.NIL) { //Only add the most recently declared metamethod, in the most specific subclass.
                        if (name.equals("__index")) {
                            //Custom __index implementation. First checks the regular __index table, and if it gets NIL, then calls the custom-defined __index function.
                            metatable.set("__index", new TwoArgFunction() {
                                final LuaFunction wrappedIndexer = getWrapper(method);
                                @Override
                                public LuaValue call(LuaValue arg1, LuaValue arg2) {
                                    LuaValue result = indexTable.get(arg2);
                                    if (result == LuaValue.NIL)
                                        result = wrappedIndexer.call(arg1, arg2);
                                    return result;
                                }
                            });
                        } else {
                            metatable.set(name, getWrapper(method));
                        }
                    }
                } else { //regular methods
                    indexTable.set(name, getWrapper(method));
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        if (metatable.rawget("__index") == LuaValue.NIL)
            metatable.set("__index", indexTable);

        //if we don't have a special toString, then have our toString give the type name from the annotation
        if (metatable.rawget("__tostring") == LuaValue.NIL) {
            metatable.set("__tostring", new OneArgFunction() {
                private final LuaString val = LuaString.valueOf(clazz.getName());
                @Override
                public LuaValue call(LuaValue arg) {
                    return val;
                }
            });
        }

        //if we don't have a special __index, then have our indexer look in the next metatable up in the java inheritance.
        if (indexTable.rawget("__index") == LuaValue.NIL) {
            LuaTable superclassMetatable = metatables.get(clazz.getSuperclass());
            if (superclassMetatable != null) {
                LuaTable newMetatable = new LuaTable();
                newMetatable.set("__index", superclassMetatable.get("__index"));
                indexTable.setmetatable(newMetatable);
            }
        }

        metatables.put(clazz, metatable);
    }

    public void dumpMetatables(LuaTable table) {
        for (Map.Entry<Class<?>, LuaTable> entry : metatables.entrySet()) {
            String name = entry.getKey().getSimpleName();
            if (table.get(name) != LuaValue.NIL)
                throw new IllegalStateException("Two classes have the same type name: " + name);
            table.set(name, entry.getValue());
        }
    }

    public VarArgFunction getWrapper(Method method) {
        return new VarArgFunction() {

            private final boolean isStatic = Modifier.isStatic(method.getModifiers());
            private Object caller;


            private final Class<?> clazz = method.getDeclaringClass();
            private final Class<?>[] argumentTypes = method.getParameterTypes();
            private final Object[] actualArgs = new Object[argumentTypes.length];

            @Override
            public Varargs invoke(Varargs args) {

                if (!isStatic)
                    caller = args.checkuserdata(1, clazz);

                //Fill in actualArgs from args
                for (int i = 0; i < argumentTypes.length; i++) {
                    int argIndex = i + (isStatic ? 1 : 2);

                    if (argIndex <= args.narg() && !args.isnil(argIndex)) {
                        actualArgs[i] = switch (argumentTypes[i].getName()) {
                            case "java.lang.Double", "double" -> args.checkdouble(argIndex);
                            case "java.lang.String" -> args.checkjstring(argIndex);
                            case "java.lang.Boolean", "boolean" -> args.toboolean(argIndex);
                            case "java.lang.Float", "float" -> (float) args.checkdouble(argIndex);
                            case "java.lang.Integer", "int" -> args.checkint(argIndex);
                            case "java.lang.Long", "long" -> args.checklong(argIndex);
                            case "org.luaj.vm2.LuaTable" -> args.checktable(argIndex);
                            case "org.luaj.vm2.LuaFunction" -> args.checkfunction(argIndex);
                            case "org.luaj.vm2.LuaValue" -> args.arg(argIndex);
                            case "java.lang.Object" -> lua2Java(args.arg(argIndex));
                            default -> args.checkuserdata(argIndex, argumentTypes[i]);
                        };
                    } else {
                        actualArgs[i] = switch (argumentTypes[i].getName()) {
                            case "double" -> 0D;
                            case "int" -> 0;
                            case "long" -> 0L;
                            case "float" -> 0f;
                            case "boolean" -> false;
                            default -> null;
                        };
                    }
                }

                //Invoke the wrapped method
                Object result;
                try {
                    result = method.invoke(caller, actualArgs);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw e.getCause() instanceof LuaError l ? l : new LuaError(e.getCause());
                }

                //Convert the return value
                return java2Lua(LuaTypeManager.this, result);
            }
        };
    }

    private LuaValue wrap(Object instance) {
        if (instance == null)
            return LuaValue.NIL;
        else if (instance instanceof Map<?,?> map)
            return wrapMap(map);
        Class<?> clazz = instance.getClass();
        LuaTable metatable = metatables.get(clazz);
        while (metatable == null) {
            clazz = clazz.getSuperclass();
            if (clazz == Object.class)
                throw new RuntimeException("Attempt to wrap illegal type " + instance.getClass().getName() + " (not registered in LuaTypeManager's \"metatables\" map)!");
            metatable = metatables.get(clazz);
        }

        LuaUserdata result = new LuaUserdata(instance);
        result.setmetatable(metatable);
        return result;
    }

    private LuaValue wrapMap(Map<?, ?> map) {
        LuaTable table = new LuaTable();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            LuaValue key = java2Lua(this, entry.getKey());
            LuaValue val = java2Lua(this, entry.getValue());
            table.set(key, val);
        }

        return table;
    }

    public static Object lua2Java(LuaValue val) {
        return switch (val.type()) {
            case LuaValue.TBOOLEAN -> val.checkboolean();
            case LuaValue.TLIGHTUSERDATA, LuaValue.TUSERDATA -> val.checkuserdata(Object.class);
            case LuaValue.TNUMBER -> val.isint() ? val.checkint() : val.checkdouble();
            case LuaValue.TSTRING -> val.checkjstring();
            case LuaValue.TTABLE -> val.checktable();
            case LuaValue.TFUNCTION -> val.checkfunction();
            default -> null;
        };
    }

    public static LuaValue java2Lua(LuaTypeManager typeManager, Object val) {
        if (val instanceof LuaValue l)
            return l;
        else if (val instanceof Double d)
            return LuaValue.valueOf(d);
        else if (val instanceof String s)
            return LuaValue.valueOf(s);
        else if (val instanceof Boolean b)
            return LuaValue.valueOf(b);
        else if (val instanceof Integer i)
            return LuaValue.valueOf(i);
        else if (val instanceof Float f)
            return LuaValue.valueOf(f);
        else if (val instanceof Byte b)
            return LuaValue.valueOf(b);
        else if (val instanceof Long l)
            return LuaValue.valueOf(l);
        else if (val instanceof Character c)
            return LuaValue.valueOf(c);
        else if (val instanceof Short s)
            return LuaValue.valueOf(s);
        else if (val == null)
            return LuaValue.NIL;
        else
            return typeManager.wrap(val);
    }
}
