package org.moon.figura.lua;

import org.terasology.jnlua.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FiguraJavaReflector implements JavaReflector {

    public static final FiguraJavaReflector INSTANCE = new FiguraJavaReflector();
    private static final JavaFunction defaultIndexFunction = DefaultJavaReflector.getInstance().getMetamethod(Metamethod.INDEX);

    //Contains a cache of whitelisted methods and fields for every class.
    public static final Map<Class<?>, Map<String, List<MethodWrapper>>> functionCache = new HashMap<>();
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

            List<MethodWrapper> methods = functionCache.get(objectClass).get(key);
            if (methods != null) {
                int argCount = luaState.getTop() - 1;
                outer:
                for (MethodWrapper wrapper : methods) {
                    if (wrapper.argumentTypes.length != argCount)
                        continue;
                    for (int i = 0; i < argCount; i++) {
                        int dist = luaState.getConverter().getTypeDistance(luaState, i+1, wrapper.argumentTypes[i]);
                        if (dist == Integer.MAX_VALUE) continue outer;
                    }
                    luaState.pushJavaFunction(wrapper);
                    return 1;
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        luaState.pushNil();
        return 1;
    };

    private static void buildCachesIfNeeded(Class<?> clazz) {
        if (functionCache.containsKey(clazz)) return;

        Map<String, List<MethodWrapper>> methodMap = new HashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(LuaWhitelist.class) || !Modifier.isStatic(method.getModifiers()))
                continue;
            if (!methodMap.containsKey(method.getName()))
                methodMap.put(method.getName(), new ArrayList<>());
            methodMap.get(method.getName()).add(new MethodWrapper(method));
        }
        functionCache.put(clazz, methodMap);

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
            ret = method.getReturnType() == void.class ? 0 : 1;
        }

        @Override
        public int invoke(LuaState luaState) {
            try {
                Object[] args = new Object[argumentTypes.length];
                for (int i = 0; i < args.length; i++)
                    args[i] = luaState.toJavaObject(i+1, argumentTypes[i]);
                luaState.pushJavaObject(method.invoke(null, args));
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
