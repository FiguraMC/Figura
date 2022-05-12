package org.moon.figura.lua.api;

import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.lua.types.LuaPairsIterator;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.ArrayList;
import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "EventsAPI",
        description = "events"
)
public class EventsAPI {

    @LuaWhitelist
    @LuaFieldDoc(description = "events.tick")
    public final LuaEvent TICK = new LuaEvent("TICK");
    @LuaWhitelist
    @LuaFieldDoc(description = "events.render")
    public final LuaEvent RENDER = new LuaEvent("RENDER");
    @LuaWhitelist
    @LuaFieldDoc(description = "events.post_render")
    public final LuaEvent POST_RENDER = new LuaEvent("POST_RENDER");
    @LuaWhitelist
    @LuaFieldDoc(description = "events.world_render")
    public final LuaEvent WORLD_RENDER = new LuaEvent("WORLD_RENDER");
    @LuaWhitelist
    @LuaFieldDoc(description = "events.post_world_render")
    public final LuaEvent POST_WORLD_RENDER = new LuaEvent("POST_WORLD_RENDER");


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
        private final List<LuaFunction> functions;

        public LuaEvent(String name) {
            this.name = name;
            functions = new ArrayList<>();
        }

        public void call(Object... args) {
            for (LuaFunction function : functions)
                function.call(args);
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
            event.functions.add(function);
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
            event.functions.clear();
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
            if (index == null) index = 1;
            if (index <= 0 || index > event.functions.size())
                throw new LuaRuntimeException("Illegal index to remove(): " + index);
            event.functions.remove(index - 1);
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
            return event.functions.size();
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
