package org.moon.figura.lua;

import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaValueProxy;

/**
 * Provides an interface for interacting with Lua Functions from java
 */
public class LuaFunction implements LuaValueProxy {
    // -- Variables -- //
    private final FiguraLuaState state;
    private final LuaValueProxy actualProxy;

    // -- Constructors -- //

    //Constructs a LuaFunction from a given FiguraLuaState, creating a reference at the index
    public LuaFunction(FiguraLuaState state, int index){
        this.state = state;

        //Get a proxy for top of stack
        actualProxy = state.getProxy(index);
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