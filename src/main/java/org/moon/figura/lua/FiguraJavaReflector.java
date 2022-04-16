package org.moon.figura.lua;

import org.moon.figura.FiguraMod;
import org.terasology.jnlua.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class FiguraJavaReflector implements JavaReflector {

    public static final FiguraJavaReflector INSTANCE = new FiguraJavaReflector();
    private static final JavaFunction defaultIndexFunction = DefaultJavaReflector.getInstance().getMetamethod(Metamethod.INDEX);

    //Contains a cache of whitelisted methods and fields for every class.
    public static final Map<Class<?>, Map<String, MethodWrapper>> methodCache = new HashMap<>();
    public static final Map<Class<?>, Map<String, List<MethodWrapper>>> metaMethodCache = new HashMap<>();
    public static final Map<Class<?>, Map<String, Field>> fieldCache = new HashMap<>();

    @Override
    public JavaFunction getMetamethod(Metamethod metamethod) {
        return switch (metamethod) {
            case INDEX -> FIGURA_INDEX;
            default -> null;
        };
    }

    public static final JavaFunction FIGURA_INDEX = luaState -> {
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
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        luaState.pushNil();
        return 1;
    };

    private static void buildCachesIfNeeded(Class<?> clazz) {
        if (methodCache.containsKey(clazz)) return;

        //Build regular (non-meta) method cache
        Map<String, MethodWrapper> methodMap = new HashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
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
        methodCache.put(clazz, methodMap);

        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(LuaWhitelist.class))
                continue;
            fieldMap.put(field.getName(), field);
        }
        fieldCache.put(clazz, fieldMap);
    }

    private static class MethodWrapper implements JavaFunction {

        private final Method method;
        private final Class<?>[] argumentTypes;
        private final int ret;

        public MethodWrapper(Method method) {
            this.method = method;
            this.argumentTypes = method.getParameterTypes();
            for (int i = 0; i < argumentTypes.length; i++) {
                if (argumentTypes[i].equals(int.class)
                    || argumentTypes[i].equals(double.class)
                    || argumentTypes[i].equals(float.class)
                    || argumentTypes[i].equals(boolean.class)
                    || argumentTypes[i].equals(char.class)
                    || argumentTypes[i].equals(long.class)
                    || argumentTypes[i].equals(short.class)
                    || argumentTypes[i].equals(byte.class)
                ) {
                    FiguraMod.LOGGER.error("Method " + method.getName() + " in class " + method.getClass().getCanonicalName() + "has primitive parameters. This can cause errors if nil is passed in, so use the wrapper classes instead!");
                    break;
                }
            }
            ret = method.getReturnType() == void.class ? 0 : 1;
        }

        @Override
        public int invoke(LuaState luaState) {
            try {
                Object[] args = new Object[argumentTypes.length];
                for (int i = 0; i < luaState.getTop() && i < args.length; i++)
                    args[i] = luaState.toJavaObject(i + 1, argumentTypes[i]);
                luaState.pushJavaObject(method.invoke(null, args));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                throw new LuaRuntimeException(e);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ret;
        }
    }

    private static Class<?> getObjectClass(Object object) {
        return object instanceof Class<?> ? (Class<?>) object : object
                .getClass();
    }
}
