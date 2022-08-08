package org.moon.figura.lua.api.event;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMetamethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
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

    @LuaWhitelist
    @LuaFieldDoc(description = "events.tick")
    public final LuaEvent TICK;
    @LuaWhitelist
    @LuaFieldDoc(description = "events.world_tick")
    public final LuaEvent WORLD_TICK;
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
