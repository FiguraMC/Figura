package org.moon.figura.lua.api;

import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.lua.types.LuaOwnedList;
import org.moon.figura.lua.types.LuaPairsIterator;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "EventsAPI",
        description = "events"
)
public class EventsAPI {

    public EventsAPI(LuaState state) {
        TICK = new LuaEvent(state, "TICK");
        RENDER = new LuaEvent(state, "RENDER");
        POST_RENDER = new LuaEvent(state, "POST_RENDER");
        WORLD_RENDER = new LuaEvent(state, "WORLD_RENDER");
        POST_WORLD_RENDER = new LuaEvent(state, "POST_WORLD_RENDER");
        CHAT_SEND_MESSAGE = new LuaEvent(state, "CHAT_SEND_MESSAGE");
        CHAT_RECEIVED_MESSAGE = new LuaEvent(state, "CHAT_RECEIVED_MESSAGE");
    }

    @LuaWhitelist
    @LuaFieldDoc(description = "events.tick")
    public final LuaEvent TICK;
    @LuaWhitelist
    @LuaFieldDoc(description = "events.render")
    public final LuaEvent RENDER;
    @LuaWhitelist
    @LuaFieldDoc(description = "events.post_render")
    public final LuaEvent POST_RENDER;
    @LuaWhitelist
    @LuaFieldDoc(description = "events.world_render")
    public final LuaEvent WORLD_RENDER;
    @LuaWhitelist
    @LuaFieldDoc(description = "events.post_world_render")
    public final LuaEvent POST_WORLD_RENDER;
    @LuaWhitelist
    @LuaFieldDoc(description = "events.chat_send_message")
    public final LuaEvent CHAT_SEND_MESSAGE;
    @LuaWhitelist
    @LuaFieldDoc(description = "events.chat_received_message")
    public final LuaEvent CHAT_RECEIVED_MESSAGE;

    //Metamethods

    @LuaWhitelist
    public static LuaPairsIterator<EventsAPI, String> __pairs(EventsAPI api) {
        return pairsIterator;
    }
    private static final LuaPairsIterator<EventsAPI, String> pairsIterator = new LuaPairsIterator<>(
            List.of("TICK", "RENDER", "POST_RENDER"), EventsAPI.class, String.class);


    @LuaWhitelist
    @LuaTypeDoc(
            name = "Event",
            description = "event"
    )
    public static class LuaEvent {

        @LuaWhitelist
        public String name;

        private static final int MAX_FUNCTIONS = 3000;

        private final LuaOwnedList<LuaFunction> functionList;
        private final LuaOwnedList<LuaFunction> functionQueue; //To avoid concurrent modification issues

        public LuaEvent(LuaState state, String name) {
            this.name = name;
            functionList = new LuaOwnedList<>(state, "EVENT_" + name, LuaFunction.class);
            functionQueue = new LuaOwnedList<>(state, "QUEUE_EVENT_" + name, LuaFunction.class);
        }

        private void flushQueue() {
            //Add all waiting functions from queue
            while (functionQueue.size() > 0)
                functionList.add(functionQueue.remove(1));
        }

        public void call(Object... args) {
            flushQueue();

            //Call all functions
            for (int i = 1; i <= functionList.size(); i++)
                functionList.get(i).call(args);
        }

        @LuaWhitelist
        @LuaMethodDoc(
                overloads = @LuaFunctionOverload(
                        argumentTypes = {LuaEvent.class, LuaFunction.class},
                        argumentNames = {"event", "function"}
                ),
                description = "event.register"
        )
        public static void register(@LuaNotNil LuaEvent event, @LuaNotNil LuaFunction function) {
            if (event.functionQueue.size() + event.functionList.size() >= MAX_FUNCTIONS)
                throw new LuaRuntimeException("Reached maximum limit of " + MAX_FUNCTIONS + " functions in an event!");
            event.functionQueue.add(function);
        }

        @LuaWhitelist
        @LuaMethodDoc(
                overloads = @LuaFunctionOverload(
                        argumentTypes = LuaEvent.class,
                        argumentNames = "event"
                ),
                description = "event.clear"
        )
        public static void clear(@LuaNotNil LuaEvent event) {
            event.functionQueue.clear();
            event.functionList.clear();
        }

        @LuaWhitelist
        @LuaMethodDoc(
                overloads = {
                        @LuaFunctionOverload(
                                argumentTypes = LuaEvent.class,
                                argumentNames = "event"
                        ),
                        @LuaFunctionOverload(
                                argumentTypes = {LuaEvent.class, Integer.class},
                                argumentNames = {"event", "index"}
                        )
                },
                description = "event.remove"
        )
        public static void remove(@LuaNotNil LuaEvent event, Integer index) {
            event.flushQueue();
            if (index == null) index = 1;
            if (index <= 0 || index > event.functionList.size())
                throw new LuaRuntimeException("Illegal index to remove(): " + index);
            event.functionList.remove(index - 1);
        }

        @LuaWhitelist
        @LuaMethodDoc(
                overloads = @LuaFunctionOverload(
                        argumentTypes = LuaEvent.class,
                        argumentNames = "event"
                ),
                description = "event.get_count"
        )
        public static int getCount(@LuaNotNil LuaEvent event) {
            return event.functionQueue.size() + event.functionList.size();
        }

        @Override
        public String toString() {
            return name + " (Event)";
        }
    }

    @Override
    public String toString() {
        return "EventsAPI";
    }
}
