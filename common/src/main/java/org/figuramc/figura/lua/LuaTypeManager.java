package org.figuramc.figura.lua;

import net.minecraft.network.chat.Component;
import org.figuramc.figura.lua.docs.FiguraDocsManager;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
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

        // Ensure that all whitelisted superclasses are loaded before this one
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
                if (name.startsWith("__")) { // metamethods
                    if (metatable.rawget(name) == LuaValue.NIL) { // Only add the most recently declared metamethod, in the most specific subclass.
                        if (name.equals("__index")) {
                            // Custom __index implementation. First checks the regular __index table, and if it gets NIL, then calls the custom-defined __index function.
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
                } else { // regular methods
                    indexTable.set(name, getWrapper(method));
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        if (metatable.rawget("__index") == LuaValue.NIL)
            metatable.set("__index", indexTable);

        // if we don't have a special toString, then have our toString give the type name from the annotation
        if (metatable.rawget("__tostring") == LuaValue.NIL) {
            metatable.set("__tostring", new OneArgFunction() {
                private final LuaString val = LuaString.valueOf(clazz.getName());
                @Override
                public LuaValue call(LuaValue arg) {
                    return val;
                }
            });
        }

        // if we don't have a special __index, then have our indexer look in the next metatable up in the java inheritance.
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
            if (!entry.getKey().isAnnotationPresent(LuaTypeDoc.class))
                continue;
            String name = entry.getKey().getAnnotation(LuaTypeDoc.class).name();
            if (table.get(name) != LuaValue.NIL)
                throw new IllegalStateException("Two classes have the same type name: " + name);
            table.set(name, entry.getValue());
        }
    }

    private final Map<Class<?>, String> namesCache = new HashMap<>();
    public String getTypeName(Class<?> clazz) {
        return namesCache.computeIfAbsent(clazz, someClass -> {
            if (someClass.isAnnotationPresent(LuaTypeDoc.class))
                return someClass.getAnnotation(LuaTypeDoc.class).name();
            return someClass.getSimpleName();
        });
    }

    private static boolean[] getRequiredNotNil(Method method) {
        Parameter[] params = method.getParameters();
        boolean[] result = new boolean[params.length];
        for (int i = 0; i < params.length; i++)
            if (params[i].isAnnotationPresent(LuaNotNil.class))
                result[i] = true;
        return result;
    }

    public VarArgFunction getWrapper(Method method) {
        return new VarArgFunction() {

            private final boolean isStatic = Modifier.isStatic(method.getModifiers());
            private Object caller;


            private final Class<?> clazz = method.getDeclaringClass();
            private final Class<?>[] argumentTypes = method.getParameterTypes();
            private final Object[] actualArgs = new Object[argumentTypes.length];
            private final boolean[] requiredNotNil = getRequiredNotNil(method);

            @Override
            public Varargs invoke(Varargs args) {

                if (!isStatic)
                    caller = args.checkuserdata(1, clazz);

                // dirty hack for QOL of ignoring the first argument if the method is static and the arg matches the class type
                int offset = isStatic && argumentTypes.length > 0 && !argumentTypes[0].isAssignableFrom(clazz) && args.isuserdata(1) && clazz.isAssignableFrom(args.checkuserdata(1).getClass()) ? 1 : 0;

                // Fill in actualArgs from args
                for (int i = 0; i < argumentTypes.length; i++) {
                    int argIndex = i + (isStatic ? 1 : 2) + offset;
                    boolean nil = args.isnil(argIndex);
                    if (nil && requiredNotNil[i])
                        throw new LuaError("bad argument: " + method.getName() + " " + argIndex + " do not allow nil values, expected " + FiguraDocsManager.getNameFor(argumentTypes[i]));
                    if (argIndex <= args.narg() && !nil) {
                        try {
                            switch (argumentTypes[i].getName()) {
                                case "java.lang.Number":
                                case "java.lang.Double":
                                case "double":
                                    actualArgs[i] = args.checkdouble(argIndex);
                                    break;
                                case "java.lang.String":
                                    actualArgs[i] = args.checkjstring(argIndex);
                                    break;
                                case "java.lang.Boolean":
                                case "boolean":
                                    actualArgs[i] = args.toboolean(argIndex);
                                    break;
                                case "java.lang.Float":
                                case "float":
                                    actualArgs[i] = (float) args.checkdouble(argIndex);
                                    break;
                                case "java.lang.Integer":
                                case "int":
                                    actualArgs[i] = args.checkint(argIndex);
                                    break;
                                case "java.lang.Long":
                                case "long":
                                    actualArgs[i] = args.checklong(argIndex);
                                    break;
                                case "org.luaj.vm2.LuaTable":
                                    actualArgs[i] = args.checktable(argIndex);
                                    break;
                                case "org.luaj.vm2.LuaFunction":
                                    actualArgs[i] = args.checkfunction(argIndex);
                                    break;
                                case "org.luaj.vm2.LuaValue":
                                    actualArgs[i] = args.arg(argIndex);
                                    break;
                                case "java.lang.Object":
                                    actualArgs[i] = luaToJava(args.arg(argIndex));
                                    break;
                                default:
                                    actualArgs[i] = argumentTypes[i].getName().startsWith("[") ? luaVarargToJava(args, argIndex, argumentTypes[i]) : args.checkuserdata(argIndex, argumentTypes[i]);
                                    break;
                            }
                            ;
                        } catch (LuaError err) {
                            String expectedType = FiguraDocsManager.getNameFor(argumentTypes[i]);
                            String actualType;
                            if (args.arg(argIndex).type() == LuaValue.TUSERDATA)
                                actualType = FiguraDocsManager.getNameFor(args.arg(argIndex).checkuserdata().getClass());
                            else
                                actualType = args.arg(argIndex).typename();
                            throw new LuaError("Invalid argument " + argIndex + " to function " + method.getName() + ". Expected " + expectedType + ", but got " + actualType);
                        }
                    } else {
                        switch (argumentTypes[i].getName()) {
                            case "double":
                                actualArgs[i] = 0D;
                                break;
                            case "int":
                                actualArgs[i] = 0;
                                break;
                            case "long":
                                actualArgs[i] = 0L;
                                break;
                            case "float":
                                actualArgs[i] = 0f;
                                break;
                            case "boolean":
                                actualArgs[i] = false;
                                break;
                            default:
                                actualArgs[i] = null;
                                break;
                        }
                        ;
                    }
                }

                // Invoke the wrapped method
                Object result;
                try {
                    result = method.invoke(caller, actualArgs);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw e.getCause() instanceof LuaError ? (LuaError) e.getCause() : new LuaError(e.getCause());
                }

                // Convert the return value
                return result instanceof Varargs ? (Varargs) result : javaToLua(result);
            }

            @Override
            public String tojstring() {
                return "function: " + method.getName();
            }
        };
    }

    private LuaValue wrap(Object instance) {
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
            LuaValue key = javaToLua(entry.getKey()).arg1();
            LuaValue val = javaToLua(entry.getValue()).arg1();
            table.set(key, val);
        }

        return table;
    }

    private LuaValue wrapCollection(Collection<?> collection) {
        LuaTable table = new LuaTable();

        int i = 1;
        for (Object o : collection) {
            table.set(i++, javaToLua(o).arg1());
        }

        return table;
    }

    private Varargs wrapArray(Object array) {
        int len = Array.getLength(array);
        LuaValue[] args = new LuaValue[len];

        for (int i = 0; i < len; i++)
            args[i] = javaToLua(Array.get(array, i)).arg1();

        return LuaValue.varargsOf(args);
    }

    public Object luaVarargToJava(Varargs args, int argIndex, Class<?> argumentType) {
        if (args.arg(argIndex).istable()) {
            return luaVarargToJava(args.checktable(argIndex).unpack(), 1, argumentType);
        } else {
            Object[] obj = new Object[args.narg() - argIndex + 1];
            for (int start = argIndex; argIndex <= args.narg(); argIndex++) {
                switch (argumentType.getName()) {
                    case "[Ljava.lang.Number;":
                    case "[Ljava.lang.Double;":
                    case "[D":
                        obj[argIndex - start] = args.checkdouble(argIndex);
                        break;
                    case "[Ljava.lang.String;":
                        obj[argIndex - start] = args.checkjstring(argIndex);
                        break;
                    case "[Ljava.lang.Boolean;":
                    case "[B":
                        obj[argIndex - start] = args.toboolean(argIndex);
                        break;
                    case "[Ljava.lang.Float;":
                    case "[F":
                        obj[argIndex - start] = (float) args.checkdouble(argIndex);
                        break;
                    case "[Ljava.lang.Integer;":
                    case "[I":
                        obj[argIndex - start] = args.checkint(argIndex);
                        break;
                    case "[Ljava.lang.Long;":
                    case "[J":
                        obj[argIndex - start] = args.checklong(argIndex);
                        break;
                    case "[Lorg.luaj.vm2.LuaTable;":
                        obj[argIndex - start] = args.checktable(argIndex);
                        break;
                    case "[Lorg.luaj.vm2.LuaFunction;":
                        obj[argIndex - start] = args.checkfunction(argIndex);
                        break;
                    case "[Lorg.luaj.vm2.LuaValue;":
                        obj[argIndex - start] = args.arg(argIndex);
                        break;
                    case "[Ljava.lang.Object;":
                        obj[argIndex - start] = luaToJava(args.arg(argIndex));
                        break;
                    default:
                        obj[argIndex - start] = args.checkuserdata(argIndex, argumentType);
                        break;
                }
                ;
            }
            return Arrays.copyOf(obj, obj.length, (Class<? extends Object[]>) argumentType);
        }
    }

    // we need to allow string being numbers here
    // however in places like pings and print we should keep strings as strings
    public Object luaToJava(LuaValue val) {
        if (val.istable())
            return val.checktable();
        else if (val.isnumber())
            if (val instanceof LuaInteger) // dumb
            {
                LuaInteger i = (LuaInteger) val;
                return i.checkint();
            } else if (val.isint() && val instanceof LuaString) // very dumb
            {
                LuaString s = (LuaString) val;
                return s.checkint();
            } else
                return val.checkdouble();
        else if (val.isstring())
            return val.checkjstring();
        else if (val.isboolean())
            return val.checkboolean();
        else if (val.isfunction())
            return val.checkfunction();
        else if (val.isuserdata())
            return val.checkuserdata(Object.class);
        else
            return null;
    }

    public Varargs javaToLua(Object val) {
        if (val == null)
            return LuaValue.NIL;
        else if (val instanceof LuaValue) {
            LuaValue l = (LuaValue) val;
            return l;
        } else if (val instanceof Double) {
            Double d = (Double) val;
            return LuaValue.valueOf(d);
        } else if (val instanceof String) {
            String s = (String) val;
            return LuaValue.valueOf(s);
        } else if (val instanceof Boolean) {
            Boolean b = (Boolean) val;
            return LuaValue.valueOf(b);
        } else if (val instanceof Integer) {
            Integer i = (Integer) val;
            return LuaValue.valueOf(i);
        } else if (val instanceof Float) {
            Float f = (Float) val;
            return LuaValue.valueOf(f);
        } else if (val instanceof Byte) {
            Byte b = (Byte) val;
            return LuaValue.valueOf(b);
        } else if (val instanceof Long) {
            Long l = (Long) val;
            return LuaValue.valueOf(l);
        } else if (val instanceof Character) {
            Character c = (Character) val;
            return LuaValue.valueOf(c);
        } else if (val instanceof Short) {
            Short s = (Short) val;
            return LuaValue.valueOf(s);
        } else if (val instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) val;
            return wrapMap(map);
        } else if (val instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) val;
            return wrapCollection(collection);
        } else if (val.getClass().isArray())
            return wrapArray(val);
        else if (val instanceof Component) {
            Component c = (Component) val;
            return LuaValue.valueOf(Component.Serializer.toJson(c));
        } else
            return wrap(val);
    }
}
