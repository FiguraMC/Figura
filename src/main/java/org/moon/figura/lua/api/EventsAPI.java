package org.moon.figura.lua.api;

import org.moon.figura.lua.LuaFunction;
import org.moon.figura.lua.LuaWhitelist;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.ArrayList;
import java.util.List;

@LuaWhitelist
public class EventsAPI {

    @LuaWhitelist
    public LuaEvent tick = new LuaEvent("tick");
    @LuaWhitelist
    public LuaEvent render = new LuaEvent("render");
    @LuaWhitelist
    public LuaEvent postRender = new LuaEvent("postRender");


    @LuaWhitelist
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
        public static void register(LuaEvent event, LuaFunction function) {
            if (function == null)
                throw new LuaRuntimeException("Attempt to register nil in event \"" + event.name + "\".");
            event.functions.add(function);
        }

    }

}
