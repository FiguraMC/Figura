package org.moon.figura.newlua.api.event;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.ast.Str;
import org.moon.figura.newlua.LuaType;
import org.moon.figura.newlua.LuaWhitelist;
import org.moon.figura.newlua.docs.LuaFunctionOverload;
import org.moon.figura.newlua.docs.LuaMetamethodDoc;
import org.moon.figura.newlua.docs.LuaMethodDoc;
import org.moon.figura.newlua.docs.LuaTypeDoc;

@LuaType(typeName = "events")
@LuaTypeDoc(
        name = "Events",
        description = "events"
)
public class EventsAPI {

    public EventsAPI() {
        TICK = new LuaEvent();
        WORLD_TICK = new LuaEvent();
        RENDER = new LuaEvent();
        POST_RENDER = new LuaEvent();
        WORLD_RENDER = new LuaEvent();
        POST_WORLD_RENDER = new LuaEvent();
        CHAT_SEND_MESSAGE = new LuaEvent();
        CHAT_RECEIVE_MESSAGE = new LuaEvent();
    }

    //Unsure on how to do the docs for these fields. Maybe we keep the @LuaFieldDoc, just don't allow them to be
    //whitelisted and accessed automatically?

    public final LuaEvent TICK;
    public final LuaEvent WORLD_TICK;
    public final LuaEvent RENDER;
    public final LuaEvent POST_RENDER;
    public final LuaEvent WORLD_RENDER;
    public final LuaEvent POST_WORLD_RENDER;
    public final LuaEvent CHAT_SEND_MESSAGE;
    public final LuaEvent CHAT_RECEIVE_MESSAGE;


    @LuaWhitelist
    @LuaMetamethodDoc(overloads = @LuaMetamethodDoc.LuaMetamethodOverload(
            types = {LuaEvent.class, EventsAPI.class, String.class},
            comment = "events.__index.comment1"
    ))
    public Object __index(String key) {
        return switch (key) {
            case "TICK" -> TICK;
            case "WORLD_TICK" -> WORLD_TICK;
            case "RENDER" -> RENDER;
            case "POST_RENDER" -> POST_RENDER;
            case "WORLD_RENDER" -> WORLD_RENDER;
            case "POST_WORLD_RENDER" -> POST_WORLD_RENDER;
            case "CHAT_SEND_MESSAGE" -> CHAT_SEND_MESSAGE;
            case "CHAT_RECEIVE_MESSAGE" -> CHAT_RECEIVE_MESSAGE;
            default -> null;
        };
    }

    @LuaType(typeName = "event")
    @LuaTypeDoc(
            name = "Event",
            description = "event"
    )
    public static class LuaEvent {

        private static final int MAX_FUNCTIONS = 1000;

        LuaTable functionList = new LuaTable();
        LuaTable nameList = new LuaTable();
        LuaTable queuedFunctions = new LuaTable();
        LuaTable queuedNames = new LuaTable();

        private void flushQueue() {
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
                description = "event.register"
        )
        public void register(LuaFunction func, String name) {
            if (functionList.rawlen() + queuedFunctions.rawlen() >= MAX_FUNCTIONS)
                throw new LuaError("Reached maximum limit of " + MAX_FUNCTIONS + " functions in one event!");
            if (name == null)
                name = "";
            queuedFunctions.set(queuedFunctions.rawlen() + 1, func);
            queuedNames.set(queuedNames.rawlen() + 1, name);
        }

        @LuaWhitelist
        @LuaMethodDoc(description = "event.clear")
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
                description = "event.remove"
        )
        public int remove(String name) {
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

    }

}
