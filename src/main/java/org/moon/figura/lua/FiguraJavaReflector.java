package org.moon.figura.lua;

import org.moon.figura.FiguraMod;
import org.moon.figura.lua.docs.FiguraDocsManager;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaTable;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class FiguraJavaReflector implements JavaReflector {

    public static final FiguraJavaReflector INSTANCE = new FiguraJavaReflector();
    private static final JavaFunction defaultIndexFunction = DefaultJavaReflector.getInstance().getMetamethod(Metamethod.INDEX);
    private static final JavaFunction defaultNewIndexFunction = DefaultJavaReflector.getInstance().getMetamethod(Metamethod.NEWINDEX);
    private static final JavaFunction defaultToStringFunction = DefaultJavaReflector.getInstance().getMetamethod(Metamethod.TOSTRING);

    //Contains a cache of whitelisted methods and fields for every class.
    public static final Map<Class<?>, Map<String, MethodWrapper>> methodCache = new HashMap<>();
    public static final Map<Class<?>, Map<String, List<MethodWrapper>>> metamethodCache = new HashMap<>();
    public static final Map<Class<?>, Map<String, Field>> fieldCache = new HashMap<>();

    @Override
    public JavaFunction getMetamethod(Metamethod metamethod) {
        return switch (metamethod) {
            case INDEX -> FIGURA_INDEX;
            case NEWINDEX -> FIGURA_NEW_INDEX;
            case TOSTRING -> defaultToStringFunction;
            case IPAIRS -> FIGURA_IPAIRS;
            case PAIRS -> FIGURA_PAIRS;
            default -> luaState -> callMetamethod(luaState, metamethod);
        };
    }

    private static final JavaFunction FIGURA_INDEX = luaState -> {
        try {
            Object object = luaState.toJavaObject(1, Object.class);
            Class<?> objectClass = getObjectClass(object);
            String key = luaState.toString(2); //was -1
            if (key == null)
                return 0;

            if (objectClass.isAnnotationPresent(LuaWhitelist.class))
                buildCachesIfNeeded(objectClass);
            else
                return defaultIndexFunction.invoke(luaState);

            Field field = fieldCache.get(objectClass).get(key);
            if (field != null) {
                luaState.pushJavaObject(field.get(object));
                return 1;
            }

            MethodWrapper method = methodCache.get(objectClass).get(key);
            if (method != null) {
                luaState.pushJavaFunction(method);
                return 1;
            }

            int i = callMetamethod(luaState, Metamethod.INDEX);
            if (i != 0) return i;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        luaState.pushNil();
        return 1;
    };

    private static final JavaFunction FIGURA_NEW_INDEX = luaState -> {
        try {
            Object object = luaState.toJavaObject(1, Object.class);
            Class<?> objectClass = getObjectClass(object);
            String key = luaState.toString(2);
            if (key == null)
                return 0;

            if (objectClass.isAnnotationPresent(LuaWhitelist.class))
                buildCachesIfNeeded(objectClass);
            else
                return defaultNewIndexFunction.invoke(luaState);

            Field f = fieldCache.get(objectClass).get(key);
            if (f != null && !Modifier.isFinal(f.getModifiers()))
                f.set(object, luaState.toJavaObject(3, f.getType()));
            else
                throw new LuaRuntimeException("Attempt to assign invalid value " + key + ".");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    };

    private static final JavaFunction FIGURA_IPAIRS = luaState -> {
        Object object = luaState.toJavaObject(1, Object.class);
        if (object == null)
            return 0;
        Class<?> objectClass = getObjectClass(object);

        if (objectClass.isAnnotationPresent(LuaWhitelist.class))
            buildCachesIfNeeded(objectClass);
        else
            return 0;
        if (!metamethodCache.get(objectClass).containsKey("__ipairs"))
            return 0;

        MethodWrapper methodWrapper = metamethodCache.get(objectClass).get("__ipairs").get(0);
        methodWrapper.invoke(luaState);

        luaState.pushValue(1);
        luaState.remove(1);
        luaState.pushInteger(0);
        return 3;
    };

    private static final JavaFunction FIGURA_PAIRS = luaState -> {
        Object object = luaState.toJavaObject(1, Object.class);
        if (object == null)
            return 0;
        Class<?> objectClass = getObjectClass(object);

        if (objectClass.isAnnotationPresent(LuaWhitelist.class))
            buildCachesIfNeeded(objectClass);
        else
            return 0;
        if (!metamethodCache.get(objectClass).containsKey("__pairs"))
            return 0;

        LuaUtils.printStack(luaState);
        MethodWrapper methodWrapper = metamethodCache.get(objectClass).get("__pairs").get(0);
        methodWrapper.invoke(luaState);

        LuaUtils.printStack(luaState);

        luaState.pushValue(1);
        luaState.remove(1);
        luaState.pushNil();
        return 3;
    };

    private static int callMetamethod(LuaState luaState, Metamethod metamethod) {
        String name = metamethod.getMetamethodName();
        Object object = luaState.toJavaObject(1, Object.class);
        Class<?> objectClass = getObjectClass(object);

        if (objectClass == Double.class || objectClass == Integer.class ||
            objectClass == String.class || objectClass == Boolean.class ||
            objectClass == Float.class || objectClass == Long.class ||
            objectClass == Character.class || objectClass == Short.class
            || objectClass == Byte.class) {
            object = luaState.toJavaObject(2, Object.class);
            objectClass = getObjectClass(object);
        }

        if (objectClass.isAnnotationPresent(LuaWhitelist.class))
            buildCachesIfNeeded(objectClass);
        else
            return 0;


        List<MethodWrapper> candidates = metamethodCache.get(objectClass).get(name);
        //LuaUtils.printStack(luaState);

        if (candidates != null) {
            outer:
            for (MethodWrapper method : candidates) {
                for (int i = 0; i < method.argumentTypes.length; i++) {
                    Class<?> clazz = method.argumentTypes[i];
                    if (luaState.getConverter().getTypeDistance(luaState, i+1, clazz) == Integer.MAX_VALUE)
                        continue outer;
                }
                //If we made it through that for loop, then we've found our correct overloaded method!
                return method.invoke(luaState);
            }
        }
        //If we made it through the whole loop without returning anything, then none of the metamethods were correct.
        //Hence, we should error.
        StringBuilder errorBuilder = new StringBuilder();
        errorBuilder.append("No applicable metamethod ")
                .append(metamethod.getMetamethodName())
                .append(" in type ")
                .append(FiguraDocsManager.NAME_MAP.getOrDefault(objectClass, objectClass.getName()))
                .append(" for arguments of type (");

        for (int i = 0; i < luaState.getTop(); i++) {
            Class<?> argClass = luaState.toJavaObject(i+1, Object.class).getClass();
            errorBuilder.append(FiguraDocsManager.NAME_MAP.getOrDefault(argClass, argClass.getName()));
            if (i < luaState.getTop() - 1)
                errorBuilder.append(", ");
        }
        errorBuilder.append(").");

        throw new LuaRuntimeException(errorBuilder.toString());
    }

    public static LuaTable getTableRepresentation(Object o) {
        Class<?> clazz = o.getClass();
        buildCachesIfNeeded(clazz);
        if (!clazz.isAnnotationPresent(LuaWhitelist.class))
            return null;
        try {
            if (clazz.getMethod("toString").getDeclaringClass() == clazz)
                return null;
        } catch (Exception ignored) {}

        LuaTable result = new LuaTable();
        try {
            for (Map.Entry<String, Field> fieldEntry : fieldCache.get(clazz).entrySet())
                result.put(fieldEntry.getKey(), fieldEntry.getValue().get(o));
            for (Map.Entry<String, MethodWrapper> methodsEntry : methodCache.get(clazz).entrySet())
                result.put(methodsEntry.getKey(), methodsEntry.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //If you try to overload any of these metamethods, building caches will
    //log an error.
    private static final Set<Metamethod> NON_OVERLOADABLE = new HashSet<>() {{
        add(Metamethod.INDEX);
        add(Metamethod.NEWINDEX);
        add(Metamethod.TOSTRING);
        add(Metamethod.PAIRS);
        add(Metamethod.IPAIRS);
        add(Metamethod.LEN);
        add(Metamethod.UNM);
    }};

    private static void buildCachesIfNeeded(Class<?> clazz) {
        if (methodCache.containsKey(clazz)) return;

        //Build regular (non-meta) method cache, and field cache
        Map<String, MethodWrapper> methodMap = new HashMap<>();
        Map<String, Field> fieldMap = new HashMap<>();
        Class<?> currentClazz = clazz;
        do {
            for (Method method : currentClazz.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(LuaWhitelist.class)
                        || method.getName().startsWith("__")) {
                    continue;
                }
                if (!Modifier.isStatic(method.getModifiers())) {
                    FiguraMod.LOGGER.warn("Found non-static whitelisted method " + method + "! Class " + clazz);
                    continue;
                }

                if (!methodMap.containsKey(method.getName()))
                    methodMap.put(method.getName(), new MethodWrapper(method));
                else
                    FiguraMod.LOGGER.error("Two whitelisted methods with the same name, " + method.getName() +
                            ", in class " + clazz.getCanonicalName() + "!");
            }
            for (Field field : currentClazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(LuaWhitelist.class))
                    continue;
                fieldMap.put(field.getName(), field);
            }
            currentClazz = currentClazz.getSuperclass();
        } while (currentClazz.isAnnotationPresent(LuaWhitelist.class)); //Check whitelisted superclasses as well

        methodCache.put(clazz, methodMap);
        fieldCache.put(clazz, fieldMap);

        //Build metamethod cache
        Map<String, List<MethodWrapper>> metamethodMap = new HashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().startsWith("__")
                    || !method.isAnnotationPresent(LuaWhitelist.class)
                    || !Modifier.isStatic(method.getModifiers()))
                continue;
            if (!metamethodMap.containsKey(method.getName()))
                metamethodMap.put(method.getName(), new ArrayList<>());
            metamethodMap.get(method.getName()).add(new MethodWrapper(method));
        }
        //Ensure non-overloadable methods aren't overloaded
        for (Metamethod m : NON_OVERLOADABLE) {
            String n = m.getMetamethodName();
            if (metamethodMap.get(n) != null && metamethodMap.get(n).size() > 1)
                FiguraMod.LOGGER.error("Metamethod " + n + " cannot be overloaded! In class " + clazz.getCanonicalName());
        }

        metamethodCache.put(clazz, metamethodMap);
    }

    private static class MethodWrapper implements JavaFunction {

        private final Method method;
        private final Class<?>[] argumentTypes;
        private final int ret;

        public MethodWrapper(Method method) {
            this.method = method;
            this.argumentTypes = method.getParameterTypes();
            for (int i = 0; i < argumentTypes.length; i++) {
                if (argumentTypes[i].isPrimitive()) {
                    FiguraMod.LOGGER.error("Method " + method.getName() + " in class " + method.getDeclaringClass().getCanonicalName() + " has primitive parameters. This can cause errors if nil is passed in, so use the wrapper classes instead!");
                    break;
                }
            }
            ret = method.getReturnType() == void.class ? 0 : 1;
        }

        @Override
        public int invoke(LuaState luaState) {
            try {
                Object[] args = new Object[argumentTypes.length];
                for (int i = luaState.getTop(); i < args.length; i++)
                    luaState.pushNil();
                for (int i = 0; i < luaState.getTop() && i < args.length; i++)
                    args[i] = luaState.toJavaObject(i + 1, argumentTypes[i]);
                luaState.pushJavaObject(method.invoke(null, args));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new LuaRuntimeException(e);
            } catch (IllegalArgumentException | ClassCastException e) {
                e.printStackTrace();
                StringBuilder errorBuilder = new StringBuilder();
                errorBuilder.append("Illegal argument types to ");
                errorBuilder.append(method.getDeclaringClass().getAnnotation(LuaTypeDoc.class).name());
                errorBuilder.append(".");
                errorBuilder.append(method.getName());
                errorBuilder.append(". Expected (");
                for (int j = 0; j < argumentTypes.length; j++) {
                    errorBuilder.append(FiguraDocsManager.NAME_MAP.getOrDefault(argumentTypes[j], argumentTypes[j].getName()));
                    if (j != argumentTypes.length - 1)
                        errorBuilder.append(", ");
                }
                errorBuilder.append(").");
                throw new LuaRuntimeException(errorBuilder.toString());
            } catch (InvocationTargetException e) {
                throw new LuaRuntimeException(e.getTargetException().getMessage());
            }
            return ret;
        }

        @Override
        public String toString() {
            return "JavaFunction";
        }
    }

    private static Class<?> getObjectClass(Object object) {
        return object instanceof Class<?> ? (Class<?>) object : object
                .getClass();
    }
}
