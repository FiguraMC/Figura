package org.moon.figura.lua.api.event;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMetamethodDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(
        name = "Event",
        value = "event"
)
public class LuaEvent {

    private static final int MAX_FUNCTIONS = 1000;

    LuaTable functionList = new LuaTable();
    LuaTable nameList = new LuaTable();
    LuaTable queuedFunctions = new LuaTable();
    LuaTable queuedNames = new LuaTable();

    protected void flushQueue() {
        //Add all waiting functions from the queue
        int nQueued = queuedFunctions.rawlen();
        int nAdded = functionList.rawlen();
        for (int i = 1; i <= nQueued; i++) {
            functionList.set(nAdded + i, queuedFunctions.get(i));
            nameList.set(nAdded + i, queuedNames.get(i));
        }
        queuedNames = new LuaTable();
        queuedFunctions = new LuaTable();
    }

    //Calls all the functions in the order they were registered, using the given args for all calls.
    public void call(Varargs args) {
        flushQueue();
        int len = functionList.rawlen();
        for (int i = 1; i <= len; i++)
            functionList.get(i).invoke(args);
    }

    //The result of one function is passed through to the next, repeatedly, eventually returning the result.
    //Used for CHAT_SEND_MESSAGE.
    public Varargs pipedCall(Varargs args) {
        flushQueue();
        int len = functionList.rawlen();
        for (int i = 1; i <= len; i++)
            args = functionList.get(i).invoke(args);
        return args;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LuaFunction.class,
                            argumentNames = "func"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {LuaFunction.class, String.class},
                            argumentNames = {"func", "name"}
                    )
            },
            value = "event.register"
    )
    public void register(@LuaNotNil LuaFunction func, String name) {
        if (functionList.rawlen() + queuedFunctions.rawlen() >= MAX_FUNCTIONS)
            throw new LuaError("Reached maximum limit of " + MAX_FUNCTIONS + " functions in one event!");
        if (name == null)
            name = "";
        queuedFunctions.set(queuedFunctions.rawlen() + 1, func);
        queuedNames.set(queuedNames.rawlen() + 1, name);
    }

    @LuaWhitelist
    @LuaMethodDoc("event.clear")
    public void clear() {
        functionList = new LuaTable();
        nameList = new LuaTable();
        queuedFunctions = new LuaTable();
        queuedNames = new LuaTable();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "name"
            ),
            value = "event.remove"
    )
    public int remove(@LuaNotNil String name) {
        flushQueue();
        LuaTable newFunctions = new LuaTable();
        LuaTable newNames = new LuaTable();
        int numRemoved = 0;

        int funcCount = functionList.rawlen();
        for (int i = 1; i <= funcCount; i++) {
            String funcName = nameList.get(i).checkjstring();
            if (funcName.equals(name)) {
                numRemoved++;
            } else {
                newFunctions.set(i - numRemoved, functionList.get(i));
                newNames.set(i - numRemoved, funcName);
            }
        }

        functionList = newFunctions;
        nameList = newNames;
        return numRemoved;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(overloads = {
            @LuaMetamethodDoc.LuaMetamethodOverload(
                    types = {int.class, LuaEvent.class}
            )
    })
    public int __len() {
        return functionList.rawlen() + queuedFunctions.rawlen();
    }

    @Override
    public String toString() {
        return "Event";
    }
}
