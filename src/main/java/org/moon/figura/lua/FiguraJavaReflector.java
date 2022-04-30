package org.moon.figura.lua;

import org.moon.figura.FiguraMod;
import org.terasology.jnlua.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class FiguraJavaReflector implements JavaReflector {

    public static final FiguraJavaReflector INSTANCE = new FiguraJavaReflector();
    private static final JavaFunction defaultIndexFunction = DefaultJavaReflector.getInstance().getMetamethod(Metamethod.INDEX);
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
            default -> luaState -> callMetamethod(luaState, metamethod);
        };
    }

    private static final JavaFunction FIGURA_INDEX = luaState -> {
        try {
            Object object = luaState.toJavaObject(1, Object.class);
            Class<?> objectClass = getObjectClass(object);
            String key = luaState.toString(-1);

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

            if (objectClass.isAnnotationPresent(LuaWhitelist.class))
                buildCachesIfNeeded(objectClass);
            else
                return defaultIndexFunction.invoke(luaState);

            Field f = fieldCache.get(objectClass).get(key);
            if (f != null && !Modifier.isFinal(f.getModifiers()))
                f.set(object, luaState.toJavaObject(3, f.getType()));

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
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

        return 0;
    }

    //If you try to overload any of these metamethods, building caches will
    //log an error.
    private static final Set<Metamethod> NON_OVERLOADABLE = new HashSet<>() {{
        add(Metamethod.INDEX);
        add(Metamethod.NEWINDEX);
        add(Metamethod.TOSTRING);
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
                        || method.getName().startsWith("__")
                        || !Modifier.isStatic(method.getModifiers()))
                    continue;
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
            int i;
            try {
                Object[] args = new Object[argumentTypes.length];
                for (i = 0; i < luaState.getTop() && i < args.length; i++)
                    args[i] = luaState.toJavaObject(i + 1, argumentTypes[i]);
                luaState.pushJavaObject(method.invoke(null, args));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                StringBuilder errorBuilder = new StringBuilder();
                errorBuilder.append("Illegal argument types to ");
                errorBuilder.append(method.getDeclaringClass().getName());
                errorBuilder.append("$");
                errorBuilder.append(method.getName());
                errorBuilder.append(". Expected (");
                for (int j = 0; j < argumentTypes.length; j++) {
                    errorBuilder.append(argumentTypes[j].getName());
                    if (j != argumentTypes.length - 1)
                        errorBuilder.append(", ");
                }
                errorBuilder.append(").");
                throw new LuaRuntimeException(errorBuilder.toString());
            } catch (RuntimeException | InvocationTargetException e) {
                throw new LuaRuntimeException(e);
            }
            return ret;
        }
    }

    private static Class<?> getObjectClass(Object object) {
        return object instanceof Class<?> ? (Class<?>) object : object
                .getClass();
    }
}
