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
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState;

import java.util.List;
import java.util.UUID;

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
        CHAT_RECEIVE_MESSAGE = new LuaEvent(state, "CHAT_RECEIVE_MESSAGE");
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
    @LuaFieldDoc(description = "events.chat_receive_message")
    public final LuaEvent CHAT_RECEIVE_MESSAGE;

    //Functions
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {EventsAPI.class, LuaFunction.class, LuaFunction.class},
                            argumentNames = {"api", "predicate", "toRun"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {EventsAPI.class, LuaFunction.class, LuaFunction.class, LuaEvent.class},
                            argumentNames = {"api", "predicate", "toRun", "event"}
                    )
            },
            description = "events.run_once"
    )
    public static void runOnce(@LuaNotNil EventsAPI api, @LuaNotNil LuaFunction predicate, @LuaNotNil LuaFunction toRun, LuaEvent event) {
        //Lambdas
        final LuaEvent finalEvent = event == null ? api.TICK : event;
        //Random name to remove this function later specifically
        final String name = UUID.randomUUID().toString();
        JavaFunction poller = luaState -> {
            Object[] stack = LuaUtils.getStack(luaState);
            Boolean success = predicate.callAndConvert(Boolean.class, stack);
            if (success) {
                toRun.call(stack);
                LuaEvent.remove(finalEvent, name);
            }
            return 0;
        };
        LuaEvent.register(finalEvent, new LuaFunction(finalEvent.state, poller), name);
    }


    //Metamethods

    @LuaWhitelist
    public static LuaPairsIterator<EventsAPI, String> __pairs(@LuaNotNil EventsAPI api) {
        return pairsIterator;
    }
    private static final LuaPairsIterator<EventsAPI, String> pairsIterator = new LuaPairsIterator<>(
            List.of("TICK", "WORLD_RENDER", "RENDER",
                    "POST_RENDER", "POST_WORLD_RENDER",
                    "CHAT_SEND_MESSAGE", "CHAT_RECEIVE_MESSAGE"), EventsAPI.class, String.class);


    @LuaWhitelist
    @LuaTypeDoc(
            name = "Event",
            description = "event"
    )
    public static class LuaEvent {

        @LuaWhitelist
        public final String name;

        public final LuaState state;

        private static final int MAX_FUNCTIONS = 3000;

        private final LuaOwnedList<LuaFunction> functionList;
        private final LuaOwnedList<LuaFunction> functionQueue; //To avoid concurrent modification issues

        private final LuaOwnedList<String> nameList;
        private final LuaOwnedList<String> nameQueue; //To avoid concurrent modification issues

        public LuaEvent(LuaState state, String name) {
            this.state = state;
            this.name = name;
            functionList = new LuaOwnedList<>(state, "EVENT_" + name, LuaFunction.class);
            functionQueue = new LuaOwnedList<>(state, "QUEUE_EVENT_" + name, LuaFunction.class);

            nameList = new LuaOwnedList<>(state, "NAMES_EVENT_" + name, String.class);
            nameQueue = new LuaOwnedList<>(state, "NAMES_QUEUE_EVENT_" + name, String.class);
        }

        private void flushQueue() {
            //Add all waiting functions from queue
            while (functionQueue.size() > 0)
                functionList.add(functionQueue.remove(1));
            while (nameQueue.size() > 0)
                nameList.add(nameQueue.remove(1));
        }

        public void call(Object... args) {
            flushQueue();

            //Call all functions
            for (int i = 1; i <= functionList.size(); i++)
                functionList.get(i).call(args);
        }

        @LuaWhitelist
        @LuaMethodDoc(
                overloads = {
                        @LuaFunctionOverload(
                                argumentTypes = {LuaEvent.class, LuaFunction.class},
                                argumentNames = {"event", "function"}
                        ),
                        @LuaFunctionOverload(
                                argumentTypes = {LuaEvent.class, LuaFunction.class, String.class},
                                argumentNames = {"event", "function", "name"}
                        )
                },
                description = "event.register"
        )
        public static void register(@LuaNotNil LuaEvent event, @LuaNotNil LuaFunction function, String name) {
            if (event.functionQueue.size() + event.functionList.size() >= MAX_FUNCTIONS)
                throw new LuaRuntimeException("Reached maximum limit of " + MAX_FUNCTIONS + " functions in an event!");
            event.functionQueue.add(function);
            event.nameQueue.add(name);
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
            event.nameQueue.clear();
            event.nameList.clear();
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
                        ),
                        @LuaFunctionOverload(
                                argumentTypes = {LuaEvent.class, String.class},
                                argumentNames = {"event", "name"}
                        )
                },
                description = "event.remove"
        )
        public static boolean remove(@LuaNotNil LuaEvent event, Object which) {
            event.flushQueue();
            boolean ret = false;
            if (which == null) which = 1;
            if (which instanceof Number n) {
                int index = n.intValue();
                if (index <= 0 || index > event.functionList.size())
                    throw new LuaRuntimeException("Illegal index to remove(): " + index);
                event.nameList.remove(index);
                event.functionList.remove(index);
                ret = true;
            } else if (which instanceof String name) {
                for (int i = event.nameList.size(); i >= 1; i--) {
                    if (name.equals(event.nameList.get(i))) {
                        event.nameList.remove(i);
                        event.functionList.remove(i);
                        ret = true;
                    }
                }
            } else {
                throw new LuaRuntimeException("Illegal type argument to remove(): must be integer or string!");
            }
            return ret;
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
