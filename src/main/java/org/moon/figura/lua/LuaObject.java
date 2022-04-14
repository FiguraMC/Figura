package org.moon.figura.lua;

import org.moon.figura.FiguraMod;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaType;
import oshi.util.tuples.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A LuaObject is a Java Object that can convert itself
 * to and from a table on the Lua stack.
 * MUST implement a static method create(), which returns
 * an instance of the class itself.
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
     * If the value is not convertible to an instance of this
     * class, then throw a LuaRuntimeException.
     * @param state The LuaState we're operating on.
     * @param index The index of the item we're checking to see if it's convertible.
     */
    protected boolean checkValid(LuaState state, int index) {
        boolean hasMetatable = state.getMetatable(index);
        if (hasMetatable) {
            state.getField(state.REGISTRYINDEX, getRegistryKey());
            boolean fine = state.rawEqual(-1, -2);
            state.pop(2);
            return fine;
        }
        return false;
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
//        state.pushBoolean(false);
//        state.setField(-2, "__metatable");

        //Create indexTable

        //Initialize some variables:
        state.newTable();
        Class<? extends LuaObject> clazz = this.getClass();
        Map<String, List<Pair<Method, Class<?>[]>>> declaredMethods = new HashMap<>();

        //Iterate over the methods in the class. We only look at static methods that are LuaWhitelisted.
        //Group up all the methods based on their name, this is for implementing method overloading.
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(LuaWhitelist.class) && (method.getModifiers() & Modifier.STATIC) > 0) {
                if (method.getName().equals("__index"))
                    continue;
                Pair<Method, Class<?>[]> pair = new Pair<>(method, method.getParameterTypes());
                declaredMethods.computeIfAbsent(method.getName(), name -> new ArrayList<>()).add(pair);
            }
        }

        //Now we have all the methods grouped by name. For each one, we create a JavaFunction which will determine
        //The arguments on the stack, and call the correct overload of the function.
        //Metamethods are automatically stored in the metatable, while regular methods are stored in the
        //__index table. Metamethods are detected by the double underscore at the start of the name.
        for (List<Pair<Method, Class<?>[]>> methodList : declaredMethods.values())
            generateFunctionForMethod(state, methodList);

        state.setField(-2, "indexTable");

        //Set up index function.

        Method fallbackIndex;
        try {
            fallbackIndex = clazz.getDeclaredMethod("__index", clazz, String.class);
            if ((fallbackIndex.getModifiers() & Modifier.STATIC) == 0 || !fallbackIndex.isAnnotationPresent(LuaWhitelist.class)) {
                FiguraMod.LOGGER.warn("__index method in " + clazz.getName() + " is either not static or not whitelisted.");
                throw new NoSuchMethodException();
            }
        } catch (NoSuchMethodException ignored) {
            fallbackIndex = null;
        }

        final Method finalFallbackIndex = fallbackIndex; //Final for lambda
        if (finalFallbackIndex == null) {
            state.getField(-1, "indexTable");
        } else {
            state.pushJavaFunction(luaState -> {
                try {
                    String indexString = (String) LuaUtils.readFromLua(luaState, 2, String.class);
                    luaState.pushValue(1);
                    luaState.getMetatable(-1);
                    luaState.getField(-1, "indexTable");
                    luaState.getField(-1, indexString);

                    if (luaState.type(-1) != LuaType.NIL)
                        return 1;

                    luaState.pop(4);
                    Object luaObject = LuaUtils.readFromLua(luaState, 1, clazz);
                    Object result = finalFallbackIndex.invoke(null, luaObject, indexString);
                    LuaUtils.pushToLua(luaState, result);
                } catch (LuaRuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 1;
            });
        }
        state.setField(-2, "__index");


        //Store the metatable in the "registry", a special lua table that can't be accessed by users.
        state.setTable(state.REGISTRYINDEX);
    }

    /**
     * Creates a lua function which searches through all of the methods in methodData, finds
     * the first one which can be called using the arguments on the stack, and calls that one.
     * @param state The LuaState we're operating on
     * @param methodData The various methods we're working on, as well as their parameter types.
     */
    private void generateFunctionForMethod(LuaState state, List<Pair<Method, Class<?>[]>> methodData) {
        state.pushJavaFunction(luaState -> {
            int retCount = 0;
            for (Pair<Method, Class<?>[]> pair : methodData) {
                Class<?>[] argTypes = pair.getB();
                if (checkArgs(luaState, argTypes)) {
                    try {
                        Method method = pair.getA();
                        Object[] args = new Object[argTypes.length];
                        for (int i = 0; i < argTypes.length; i++)
                            args[i] = LuaUtils.readFromLua(luaState, i+1, argTypes[i]);
                        Object result = method.invoke(null, args);
                        LuaUtils.pushToLua(luaState, result);
                        retCount = method.getReturnType() == void.class ? 0 : 1;
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new LuaRuntimeException(e);
                    }
                }
            }
            return retCount;
        });
        String methodName = methodData.get(0).getA().getName();

        //If it's a metamethod, put it in the metatable instead of the __index table.
        if (methodName.startsWith("__"))
            state.setField(-3, methodName);
        else
            state.setField(-2, methodName);
    }

    /**
     * Checks whether or not the arguments in the LuaState can be
     * passed in to the given method.
     * @param state The LuaState we're operating on.
     * @param paramTypes The argument types of the method we're looking at.
     * @return Whether the arguments are valid or not
     */
    private boolean checkArgs(LuaState state, Class<?>[] paramTypes) {
        int i;
        outer_loop:
        for (i = 0; i < paramTypes.length; i++) {
            Class<?> argType = paramTypes[i];
            if (!state.isNil(i+1)) {
                if (LuaObject.class.isAssignableFrom(argType)) {
                    if (!LuaUtils.getOrComputeGenerator(argType).get().checkValid(state, i + 1))
                        break;
                } else {
                    //LuaUtils.printStack(state);
                    switch (state.type(i+1)) {
                        case NUMBER:
                            if (argType != double.class && argType != int.class && argType != String.class)
                                break outer_loop;
                            break;
                        case STRING:
                            if (argType != String.class)
                                break outer_loop;
                            break;
                        case BOOLEAN:
                            if (argType != boolean.class)
                                break outer_loop;
                            break;
                        default:
                            break outer_loop;
                    }
                }
            }
        }
        //Made it through the whole loop without breaking, or not?
        return i == paramTypes.length;
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
