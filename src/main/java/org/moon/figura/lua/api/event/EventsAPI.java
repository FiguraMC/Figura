package org.moon.figura.lua.api.event;

import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMetamethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

@LuaType(typeName = "events")
@LuaTypeDoc(
        name = "EventsAPI",
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
    //Maybe in the __index comment we give a docs list of the events?

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

    @Override
    public String toString() {
        return "EventsAPI";
    }
}
