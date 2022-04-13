package org.moon.figura.lua;

import org.moon.figura.FiguraMod;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState;

public class LuaUtils {

    /**
     * Inspects the value at the given index of the stack, and returns a java
     * Object representing that value, if it can be converted to the
     * desired class.
     * @param state
     * @param clazz
     * @return
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

    public static void pushToLua(LuaState state, Object o) {
        if (o instanceof LuaObject obj)
            obj.pushToStack(state);
        else
            state.pushJavaObject(o);
    }



}
