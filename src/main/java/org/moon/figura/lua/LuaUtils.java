package org.moon.figura.lua;

import org.moon.figura.FiguraMod;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.function.Supplier;

public class LuaUtils {

    /**
     * Keeps a map from classes to functions to provide a fresh instance of the class.
     * For most classes, this should just be a call to a constructor, but some
     * classes may have other methods of providing a fresh instance.
     */
    private static HashMap<Class<?>, Supplier<? extends LuaObject>> generatorCache = new HashMap<>();

    /**
     * Inspects the value at the given index of the stack, and returns a java
     * Object representing that value, if it can be converted to the
     * desired LuaObject class.
     * @param state The LuaState we're operating on.
     * @param index The index of the value we want to convert to our class
     * @param clazz The class we want to convert the value to
     * @return The converted object.
     */
    public static Object readFromLua(LuaState state, int index, Class<?> clazz) {
        if (!state.isNil(index)) {
            if (LuaObject.class.isAssignableFrom(clazz)) {
                LuaObject result = getOrComputeGenerator(clazz).get();
                if (result.checkValid(state, index)) {
                    result.read(state, index);
                    return result;
                }
                throw new LuaRuntimeException("Invalid parameter");
            }
        }
        return state.toJavaObject(index, clazz);
    }

    /**
     * Locates the "create" method for the given class, and stores it in
     * a map to cache it. This way we don't need to do reflection lookups
     * every time we want to instantiate a class.
     * @param clazzz The class to search for the create() method in.
     * @return A supplier that wraps the create method call and deals
     * with Exceptions that may result.
     */
    public static Supplier<? extends LuaObject> getOrComputeGenerator(Class<?> clazzz) {
        return generatorCache.computeIfAbsent(clazzz, (clazz) -> {
            try {
                Method createMethod = clazz.getDeclaredMethod("create");
                if ((createMethod.getModifiers() & Modifier.STATIC) == 0)
                    throw new NoSuchMethodException();
                if (!clazz.isAssignableFrom(createMethod.getReturnType())) //If the method doesn't return an object of type clazzz...
                    throw new ClassCastException(createMethod.getReturnType().getName() + " instead of " + clazz.getName() + ".");
                return () -> {
                    try {
                        return (LuaObject) createMethod.invoke(null);
                    } catch (ClassCastException e) {
                        //This should never happen.
                        FiguraMod.LOGGER.error("Illegal return type for the create method in class " + clazz.getName() +
                                "! Must return a LuaObject. NOTE: THIS ERROR SHOULD NEVER PRINT. IF IT DOES, SOMETHING IS VERY WRONG.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                };
            } catch (NoSuchMethodException e) {
                FiguraMod.LOGGER.error("No static create() method in LuaObject class " + clazz.getName() + "!");
            } catch (ClassCastException e) {
                FiguraMod.LOGGER.error("Invalid return type for create method in class " + clazz.getName() + ", returns " + e.getMessage());
            }
            return () -> null;
        });
    }

    /**
     * Pushes an object to lua. If it's a Lua Object, then
     * it converts it to a table and pushes that; if it's just
     * a regular java object, pushes it directly.
     * @param state The LuaState we're operating on
     * @param o The object we want to push.
     */
    public static void pushToLua(LuaState state, Object o) {
        if (o instanceof LuaObject obj) {
            obj.pushToStack(state);
        } else {
            if (!(o instanceof String))
                FiguraMod.LOGGER.warn("Sending regular java object of type " + o.getClass().getName() + " into the lua state. Is this correct?");
            state.pushJavaObject(o);
        }
    }

    public static void printStack(LuaState state) {
        System.out.println("--Top of Stack--");
        for (int i = state.getTop(); i > 0; i--) {
            System.out.println(getString(state, i));
        }
        System.out.println("--Bottom of Stack--");
    }

    private static String getString(LuaState state, int index) {
        Object o = state.toJavaObject(index, Object.class);
        return o == null ? "null" : o.toString();
    }

}
