package org.moon.figura.newlua;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
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
        if (!clazz.isAnnotationPresent(LuaType.class))
            throw new IllegalArgumentException("Tried to generate metatable for un-whitelisted class " + clazz.getName() + "!");

        //Ensure that all whitelisted superclasses are loaded before this one
        try {
            generateMetatableFor(clazz.getSuperclass());
        } catch (IllegalArgumentException ignored) {}

        LuaTable metatable = new LuaTable();

        LuaTable indexTable = new LuaTable();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(LuaWhitelist.class)) {
                continue;
            }
            String name = method.getName();
            if (name.equals("toString") && method.getParameterTypes().length == 0) { //toString special case
                metatable.set("__tostring", getWrapper(method));
            } else if (name.startsWith("__")) { //metamethods
                if (name.equals("__index")) {
                    LuaTable metatable2 = new LuaTable();
                    metatable2.set("__index", getWrapper(method));
                    indexTable.setmetatable(metatable2);
                } else {
                    metatable.set(name, getWrapper(method));
                }
            } else { //regular methods
                indexTable.set(name, getWrapper(method));
            }
        }
        metatable.set("__index", indexTable);

        //if we don't have a special toString, then have our toString give the type name from the annotation
        if (metatable.get("__tostring") == LuaValue.NIL) {
            metatable.set("__tostring", new OneArgFunction() {
                private final String val = clazz.getAnnotation(LuaType.class).typeName();
                @Override
                public LuaValue call(LuaValue arg) {
                    return LuaString.valueOf(val);
                }
            });
        }

        //if we don't have a special __index, then have our indexer look in the next metatable up in the java inheritance.
        if (indexTable.get("__index") == LuaValue.NIL) {
            LuaTable superclassMetatable = metatables.get(clazz.getSuperclass());
            if (superclassMetatable != null) {
                LuaTable newMetatable = new LuaTable();
                newMetatable.set("__index", superclassMetatable.get("__index"));
            }
        }

        metatables.put(clazz, metatable);
    }

    public void dumpMetatables(LuaTable table) {
        for (Map.Entry<Class<?>, LuaTable> entry : metatables.entrySet()) {
            String name = entry.getKey().getAnnotation(LuaType.class).typeName();
            if (table.get(name) != LuaValue.NIL)
                throw new IllegalStateException("Two classes have the same type name: " + name);
            table.set(name, entry.getValue());
        }
    }

    private LuaValue getWrapper(Method method) {
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

                    if (i < args.narg()) {
                        actualArgs[i] = switch (argumentTypes[i].getName()) {
                            case "Double", "double" -> args.checkdouble(argIndex);
                            case "String" -> args.checkjstring(argIndex);
                            case "Float", "float" -> (float) args.checkdouble(argIndex);
                            case "Integer", "int" -> args.checkint(argIndex);
                            case "Long", "long" -> args.checklong(argIndex);
                            case "LuaTable" -> args.checktable(argIndex);
                            case "LuaFunction" -> args.checkfunction(argIndex);
                            default -> args.checkuserdata(argIndex, argumentTypes[i]);
                        };
                    } else {
                        actualArgs[i] = switch (argumentTypes[i].getName()) {
                            case "double" -> 0D;
                            case "int" -> 0;
                            case "long" -> 0L;
                            case "float" -> 0f;
                            default -> null;
                        };
                    }
                }

                //Invoke the wrapped method
                Object result;
                try {
                    result = method.invoke(caller, actualArgs);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new LuaError(e);
                }

                //Convert the return value
                return switch (method.getReturnType().getName()) {
                    case "Double", "double" -> LuaDouble.valueOf((Double) result);
                    case "Integer", "int" -> LuaInteger.valueOf((Integer) result);
                    case "Long", "long" -> LuaInteger.valueOf((Long) result);
                    case "Float", "float" -> LuaDouble.valueOf((Float) result);
                    case "void" -> LuaValue.NIL;
                    default -> wrap(result);
                };
            }
        };
    }

    public LuaValue wrap(Object instance) {
        if (instance == null)
            return LuaValue.NIL;
        LuaTable metatable = metatables.get(instance.getClass());
        if (metatable == null)
            throw new RuntimeException("Attempt to wrap illegal type (not registered in LuaTypeManager's METATABLE map)!");
        LuaUserdata result = new LuaUserdata(instance);
        result.setmetatable(metatable);
        return result;
    }

}
