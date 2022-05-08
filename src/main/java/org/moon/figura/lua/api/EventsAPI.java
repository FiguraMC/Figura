package org.moon.figura.lua.api;

import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.lua.LuaWhitelist;
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
    @LuaFieldDoc(
            canEdit = false,
            description = "events.tick"
    )
    public final LuaEvent TICK = new LuaEvent("TICK");
    @LuaWhitelist
    @LuaFieldDoc(
            canEdit = false,
            description = "events.render"
    )
    public final LuaEvent RENDER = new LuaEvent("RENDER");
    @LuaWhitelist
    @LuaFieldDoc(
            canEdit = false,
            description = "events.post_render"
    )
    public final LuaEvent POST_RENDER = new LuaEvent("POST_RENDER");

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
                        argumentNames = {"event", "function"},
                        returnType = void.class
                ),
                description = "event.register"
        )
        public static void register(LuaEvent event, LuaFunction function) {
            if (function == null)
                throw new LuaRuntimeException("Attempt to register nil in event \"" + event.name + "\".");
            event.functions.add(function);
        }

    }

}
