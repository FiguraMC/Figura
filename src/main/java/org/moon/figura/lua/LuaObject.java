package org.moon.figura.lua;

import org.moon.figura.FiguraMod;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class LuaObject {


    protected abstract String getRegistryKey();
    protected LuaObject() {}

    //OVERRIDEABLE METHODS FOR WRITING/READING
    //--------------------------------
    protected void write(LuaState state) {
        state.newTable();
    }
    protected void checkValid(LuaState state, int index) {
        state.getMetatable(index);
        if (!state.isNil(-1)) {
            state.getField(state.REGISTRYINDEX, getRegistryKey());
            boolean fine = state.rawEqual(-1, -2);
            state.pop(2);
            if (fine) return;
        } else {
            state.pop(1);
        }
        throw new LuaRuntimeException("Invalid parameter"); //TODO: more detailed errors
    }
    protected void read(LuaState state, int index) {

    }
    //--------------------------------

    //METHODS FOR OTHER CLASSES TO USE
    //--------------------------------
    public final void pushToStack(LuaState state) {
        state.newTable();
        write(state);
        pushMetatable(state);
        state.setMetatable(-2);
    }
    //--------------------------------

    //HELPER METHODS FOR WRITING
    //--------------------------------
    final protected void putInteger(LuaState state, String key, long value) {
        state.pushInteger(value);
        state.setField(-2, key);
    }
    final protected void putDouble(LuaState state, String key, double value) {
        state.pushNumber(value);
        state.setField(-2, key);
    }
    final protected void putLuaObject(LuaState state, String key, LuaObject object) {
        object.pushToStack(state);
        state.setField(-2, key);
    }
    //--------------------------------

    //HELPER METHODS FOR READING
    //--------------------------------
    final protected long readInteger(LuaState state, int index, String key) {
        state.getField(index, key);
        long ret = state.checkInteger(-1);
        state.pop(1);
        return ret;
    }
    final protected double readDouble(LuaState state, int index, String key) {
        state.getField(index, key);
        double ret = state.checkNumber(-1);
        state.pop(1);
        return ret;
    }
    //--------------------------------

    //METATABLE LUA PRIVATE HELPER METHODS
    //--------------------------------
    /**
     * Generates the metatable and puts it in the Lua registry, under the name
     * returned by getRegistryKey().
     */
    private void generateMetatable(LuaState state) {
        state.pushString(getRegistryKey());
        state.newTable(); //metatable

        //Create __index and other metamethods!
        state.newTable();
        Class<? extends LuaObject> clazz = this.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(LuaWhitelist.class) && (method.getModifiers() & Modifier.STATIC) > 0) {
                if (method.getName().startsWith("__"))
                    generateMetamethod(state, method);
                else
                    generateFunction(state, method);
            }
        }
        state.setField(-2, "__index");

        //Store the metatable in the "registry", a special lua table that can't be accessed by users.
        state.setTable(state.REGISTRYINDEX);
    }

    private void generateFunction(LuaState state, Method method) {
        Class<?>[] argTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        if (!returnType.isPrimitive())
            if (!LuaObject.class.isAssignableFrom(returnType))
                FiguraMod.LOGGER.warn("Releasing a raw java object, not a LuaObject, into the lua state. " + method.getName() + "() in " + method.getDeclaringClass() + ".");
        state.pushJavaFunction(luaState -> {
            try {
                Object[] args = new Object[argTypes.length];
                for (int i = 0; i < argTypes.length; i++) {
                    args[i] = LuaUtils.readFromLua(luaState, i+1, argTypes[i]);
                }

                Object result = method.invoke(null, args);
                LuaUtils.pushToLua(luaState, result);

                return returnType == void.class ? 0 : 1;
            } catch (Exception e) {
                e.printStackTrace();
                throw new LuaRuntimeException(e);
            }
        });
        state.setField(-2, method.getName());
    }

    private void generateMetamethod(LuaState state, Method method) {
        Class<?>[] argTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        if (!returnType.isPrimitive())
            if (!LuaObject.class.isAssignableFrom(returnType))
                FiguraMod.LOGGER.warn("Releasing a raw java object, not a LuaObject, into the lua state. " + method.getName() + "() in " + method.getDeclaringClass() + ".");
        state.pushJavaFunction(luaState -> {
            try {
                Object[] args = new Object[argTypes.length];
                for (int i = 0; i < argTypes.length; i++) {
                    args[i] = LuaUtils.readFromLua(luaState, i+1, argTypes[i]);
                }
                Object result = method.invoke(null, args);
                LuaUtils.pushToLua(luaState, result);
                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                throw new LuaRuntimeException(e);
            }
        });
        state.setField(-3, method.getName());
    }

    private void pushMetatable(LuaState state) {
        state.getField(state.REGISTRYINDEX, getRegistryKey());
        if (state.isNil(-1)) {
            //If the metatable doesn't exist, generate the metatable
            //and try again.
            state.pop(1); //pop the nil
            generateMetatable(state);
            state.getField(state.REGISTRYINDEX, getRegistryKey());
        }
    }
    //--------------------------------

}
