package org.moon.figura.lua.api;

import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.lua.LuaWhitelist;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.ArrayList;
import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "events",
        description = "A global API that contains all of the figura events."
)
public class EventsAPI {

    @LuaWhitelist
    public final LuaEvent tick = new LuaEvent("tick");
    @LuaWhitelist
    public final LuaEvent render = new LuaEvent("render");
    @LuaWhitelist
    public final LuaEvent postRender = new LuaEvent("postRender");


    @LuaWhitelist
    @LuaTypeDoc(
            name = "Event",
            description = "A hook for a certain event in Minecraft. " +
                    "You may register functions to one, and those functions will be " +
                    "called when the event occurs."
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
                description = "Registers the given function to the given event. When the event " +
                        "occurs, the function will be run. Functions are run in the order they were " +
                        "registered."
        )
        public static void register(LuaEvent event, LuaFunction function) {
            if (function == null)
                throw new LuaRuntimeException("Attempt to register nil in event \"" + event.name + "\".");
            event.functions.add(function);
        }

    }

}
