package org.moon.figura.lua;

import org.moon.figura.FiguraMod;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState;

public class LuaUtils {

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
                try {
                    LuaObject result = (LuaObject) clazz.getDeclaredConstructor().newInstance();
                    result.checkValid(state, index);
                    result.read(state, index);
                    return result;
                } catch (NoSuchMethodException e) {
                    FiguraMod.LOGGER.error("No default constructor in LuaObject class " + clazz.getName() + "!");
                } catch (Exception e) {
                    if (e instanceof LuaRuntimeException e1)
                        throw e1;
                    else
                        e.printStackTrace();
                }
            }
        }
        return state.toJavaObject(index, clazz);
    }

    /**
     * Pushes an object to lua. If it's a Lua Object, then
     * it converts it to a table and pushes that; if it's just
     * a regular java object, pushes it directly.
     * @param state The LuaState we're operating on
     * @param o The object we want to push.
     */
    public static void pushToLua(LuaState state, Object o) {
        if (o instanceof LuaObject obj)
            obj.pushToStack(state);
        else
            state.pushJavaObject(o);
    }



}
