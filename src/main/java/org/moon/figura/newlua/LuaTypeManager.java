package org.moon.figura.newlua;

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
        if (!clazz.isAnnotationPresent(LuaType.class))
            throw new IllegalArgumentException("Tried to generate metatable for un-whitelisted class " + clazz.getName() + "!");

        //Ensure that all whitelisted superclasses are loaded before this one
        try {
            generateMetatableFor(clazz.getSuperclass());
        } catch (IllegalArgumentException ignored) {}

        LuaTable metatable = new LuaTable();

        LuaTable indexTable = new LuaTable();
        for (Method method : clazz.getMethods()) {
            if (!method.isAnnotationPresent(LuaWhitelist.class)) {
                continue;
            }
            String name = method.getName();
            if (name.startsWith("__")) { //metamethods
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
            } else { //regular methods
                indexTable.set(name, getWrapper(method));
            }
        }
        if (metatable.rawget("__index") == LuaValue.NIL)
            metatable.set("__index", indexTable);

        //if we don't have a special toString, then have our toString give the type name from the annotation
        if (metatable.rawget("__tostring") == LuaValue.NIL) {
            metatable.set("__tostring", new OneArgFunction() {
                private final LuaString val = LuaString.valueOf(clazz.getAnnotation(LuaType.class).typeName());
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
            String name = entry.getKey().getAnnotation(LuaType.class).typeName();
            if (table.get(name) != LuaValue.NIL)
                throw new IllegalStateException("Two classes have the same type name: " + name);
            table.set(name, entry.getValue());
        }
    }

    private VarArgFunction getWrapper(Method method) {
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

                    if (i < args.narg() && !args.isnil(i+1)) {
                        actualArgs[i] = switch (argumentTypes[i].getName()) {
                            case "java.lang.Double", "double" -> args.checkdouble(argIndex);
                            case "java.lang.String" -> args.checkjstring(argIndex);
                            case "java.lang.Boolean", "boolean" -> args.toboolean(argIndex);
                            case "java.lang.Float", "float" -> (float) args.checkdouble(argIndex);
                            case "java.lang.Integer", "int" -> args.checkint(argIndex);
                            case "java.lang.Long", "long" -> args.checklong(argIndex);
                            case "org.luaj.vm2.LuaTable" -> args.checktable(argIndex);
                            case "org.luaj.vm2.LuaFunction" -> args.checkfunction(argIndex);
                            case "java.lang.Object" -> convertLua2Java(args.arg(argIndex));
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
                if (result == null) {return LuaValue.NIL;}
                return switch (result.getClass().getName()) {
                    case "java.lang.Double", "double" -> LuaDouble.valueOf((Double) result);
                    case "java.lang.String" -> LuaString.valueOf((String) result);
                    case "java.lang.Boolean", "boolean" -> LuaBoolean.valueOf((Boolean) result);
                    case "java.lang.Integer", "int" -> LuaInteger.valueOf((Integer) result);
                    case "java.lang.Long", "long" -> LuaInteger.valueOf((Long) result);
                    case "java.lang.Float", "float" -> LuaDouble.valueOf((Float) result);
                    case "org.luaj.vm2.LuaTable" -> (LuaTable) result;
                    case "org.luaj.vm2.LuaFunction" -> (LuaFunction) result;
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
            throw new RuntimeException("Attempt to wrap illegal type " + instance.getClass().getName() + " (not registered in LuaTypeManager's \"metatables\" map)!");
        LuaUserdata result = new LuaUserdata(instance);
        result.setmetatable(metatable);
        return result;
    }

    private static Object convertLua2Java(LuaValue val) {
        return switch (val.type()) {
            case LuaValue.TBOOLEAN -> val.checkboolean();
            case LuaValue.TLIGHTUSERDATA, LuaValue.TUSERDATA -> val.checkuserdata(Object.class);
            case LuaValue.TNUMBER -> val.isint() ? val.checkint() : val.checkdouble();
            case LuaValue.TSTRING -> val.checkstring();
            case LuaValue.TTABLE -> val.checktable();
            case LuaValue.TFUNCTION -> val.checkfunction();
            default -> null;
        };
    }

}
