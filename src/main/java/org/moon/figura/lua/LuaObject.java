package org.moon.figura.lua;

import org.moon.figura.FiguraMod;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A LuaObject is a Java Object that can convert itself
 * to and from a table on the Lua stack.
 */
public abstract class LuaObject {

    /**
     * @return A UNIQUE (per class) string which will be used as
     * a key in the Lua registry. If two classes have the same
     * registry key, then one's metatable will be overwritten.
     */
    protected abstract String getRegistryKey();

    /**
     * Every LuaObject must have a default, empty constructor,
     * but I have no way to enforce this contractually. So the
     * mod will just log an error if it finds a LuaObject without
     * a no-argument constructor.
     */
    protected LuaObject() {}

    //OVERRIDEABLE METHODS FOR WRITING/READING
    //--------------------------------

    /**
     * Should write data from the class into a lua table.
     * You may assume that there is a table at the top of the
     * stack when this method begins, and you should leave
     * this table at the top of the stack when it exits
     * (do not pop the table).
     * Should use the putX helper methods for easier programming.
     * @param state The LuaState we're operating on.
     */
    protected void write(LuaState state) {}

    /**
     * Should check if the value at the given index is convertible
     * to an instance of this class. Default implementation checks if
     * the metatables match to confirm this.
     * If the value is not convertible to and instance of this
     * class, then throw a LuaRuntimeException.
     * @param state The LuaState we're operating on.
     * @param index The index of the item we're checking to see if it's convertible.
     */
    protected void checkValid(LuaState state, int index) throws LuaRuntimeException {
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

    /**
     * Read data from the LuaState, at the given index,
     * into this instance of the class. Should use the
     * helper methods readX for ease of programming.
     * @param state The LuaState we're operating on.
     * @param index The index of the item we're reading from.
     */
    protected void read(LuaState state, int index) {

    }
    //--------------------------------

    //METHODS FOR OTHER CLASSES TO USE
    //--------------------------------

    /**
     * Pushes a new table onto the stack, and writes the
     * contents of this instance into the table. Also
     * sets the metatable of the new table to be the
     * one defined by the class.
     * @param state The LuaState we're operating on.
     */
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
     * Generates the metatable for this class, looking at
     * whitelisted, static methods, and puts it in the Lua
     * registry, under the name returned by getRegistryKey().
     */
    private void generateMetatable(LuaState state) {
        state.pushString(getRegistryKey());
        state.newTable(); //metatable

        //Disable getmetatable() calls by default.
        state.pushBoolean(false);
        state.setField(-2, "__metatable");

        //Create __index and other metamethods!
        state.newTable();
        Class<? extends LuaObject> clazz = this.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(LuaWhitelist.class) && (method.getModifiers() & Modifier.STATIC) > 0) {
                if (method.getName().startsWith("__"))
                    generateMetamethod(state, method);
                else
                    generateMethod(state, method);
            }
        }
        state.setField(-2, "__index");

        //Store the metatable in the "registry", a special lua table that can't be accessed by users.
        state.setTable(state.REGISTRYINDEX);
    }

    /**
     * Generates a lua function from the provided Java method, and
     * puts it in the specified table. There are two tables at the
     * top of the stack. If meta is false, we put the method in
     * the table at the top of the stack. If it's true, then we put
     * it in the table just underneath.
     * @param state The LuaState we're operating on.
     * @param method The Java method we want to convert to a lua function.
     * @param retCount The number of values to be returned from this method, always 0 or 1.
     * @param meta Whether this function is a meta-function or not.
     */
    private void generateFunction(LuaState state, Method method, int retCount, boolean meta) {
        Class<?>[] argTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        if (!returnType.isPrimitive())
            if (!LuaObject.class.isAssignableFrom(returnType))
                FiguraMod.LOGGER.warn("Releasing a raw java object, not a LuaObject, into the lua state. " + method.getName() + "() in " + method.getDeclaringClass() + ".");
        state.pushJavaFunction(luaState -> {
            try {
                Object[] args = new Object[argTypes.length];
                for (int i = 0; i < argTypes.length; i++)
                    args[i] = LuaUtils.readFromLua(luaState, i+1, argTypes[i]);
                Object result = method.invoke(null, args);
                LuaUtils.pushToLua(luaState, result);
                return retCount;
            } catch (Exception e) {
                e.printStackTrace();
                throw new LuaRuntimeException(e);
            }
        });
        int index = meta ? -3 : -2;
        state.setField(index, method.getName());
    }

    /**
     * Generates a regular method for this LuaObject.
     * @param state The LuaState we're operating on
     * @param method The Java method we're converting into a lua function.
     */
    private void generateMethod(LuaState state, Method method) {
        int retCount = method.getReturnType() == void.class ? 0 : 1;
        generateFunction(state, method, retCount, false);
    }

    /**
     * Generates a metamethod for this LuaObject.
     * @param state The LuaState we're operating on
     * @param method The Java method we're converting into a lua function
     */
    private void generateMetamethod(LuaState state, Method method) {
        generateFunction(state, method, 1, true);
    }

    /**
     * Pushes the metatable onto the stack. If the metatable doesn't
     * yet exist, then it creates the metatable and then puts it
     * on the stack.
     * @param state The LuaState we're operating on.
     */
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
