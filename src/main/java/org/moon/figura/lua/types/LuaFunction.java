package org.moon.figura.lua.types;

import org.moon.figura.lua.FiguraLuaState;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaValueProxy;

/**
 * Provides an interface for interacting with Lua Functions from java
 */
public class LuaFunction implements LuaValueProxy {
    // -- Variables -- //
    private final LuaState state;
    private final LuaValueProxy actualProxy;

    // -- Constructors -- //

    //Constructs a LuaFunction from a given FiguraLuaState, creating a reference at the index
    public LuaFunction(LuaState state, int index){
        this.state = state;

        //Get a proxy for top of stack
        actualProxy = state.getProxy(index);
    }

    //Constructs a LuaFunction from a JavaFunction, pushing the function on the stack and using it as the reference.
    public LuaFunction(LuaState state, JavaFunction func) {
        this.state = state;
        state.pushJavaFunction(func);
        actualProxy = state.getProxy(-1);
        state.pop(1);
    }

    // -- Functions -- //

    @Override
    public LuaState getLuaState() {
        return state;
    }

    @Override
    public void pushValue() {
        actualProxy.pushValue();
    }

    /**
     * Calls the lua function with the given arguments, and discards all return values.
     */
    public void call(Object... args) {
        //Put function on stack
        pushValue();

        //Push arguments
        for (Object arg : args)
            state.pushJavaObject(arg);

        //Call function with arg count and 0 returns
        state.call(args.length, 0);
    }

    /**
     * Calls the lua function with the given args, and returns the first value from lua as the given type.
     */
    public <T> T callAndConvert(Class<T> type, Object... args){
        //Put function on stack
        pushValue();

        //Push arguments
        for (Object arg : args)
            state.pushJavaObject(arg);

        //Call function with arg count and 0 returns
        state.call(args.length, 1);

        //Convert and pop object
        T ret = state.toJavaObject(-1, type);
        state.pop(1);

        //Return object
        return ret;
    }
}