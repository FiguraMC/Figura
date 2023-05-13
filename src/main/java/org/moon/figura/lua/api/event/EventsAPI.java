package org.moon.figura.lua.api.event;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMetamethodDoc;
import org.moon.figura.lua.docs.LuaMetamethodDoc.LuaMetamethodOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

import java.util.HashMap;
import java.util.Map;

@LuaWhitelist
@LuaTypeDoc(
        name = "EventsAPI",
        value = "events"
)
public class EventsAPI {

    //docs only :woozy:
    @LuaWhitelist
    @LuaFieldDoc("events.entity_init")
    public final LuaEvent ENTITY_INIT = null;
    @LuaWhitelist
    @LuaFieldDoc("events.tick")
    public final LuaEvent TICK = null;
    @LuaWhitelist
    @LuaFieldDoc("events.world_tick")
    public final LuaEvent WORLD_TICK = null;
    @LuaWhitelist
    @LuaFieldDoc("events.render")
    public final LuaEvent RENDER = null;
    @LuaWhitelist
    @LuaFieldDoc("events.post_render")
    public final LuaEvent POST_RENDER = null;
    @LuaWhitelist
    @LuaFieldDoc("events.world_render")
    public final LuaEvent WORLD_RENDER = null;
    @LuaWhitelist
    @LuaFieldDoc("events.post_world_render")
    public final LuaEvent POST_WORLD_RENDER = null;
    @LuaWhitelist
    @LuaFieldDoc("events.chat_send_message")
    public final LuaEvent CHAT_SEND_MESSAGE = null;
    @LuaWhitelist
    @LuaFieldDoc("events.chat_receive_message")
    public final LuaEvent CHAT_RECEIVE_MESSAGE = null;
    @LuaWhitelist
    @LuaFieldDoc("events.skull_render")
    public final LuaEvent SKULL_RENDER = null;
    @LuaWhitelist
    @LuaFieldDoc("events.mouse_scroll")
    public final LuaEvent MOUSE_SCROLL = null;
    @LuaWhitelist
    @LuaFieldDoc("events.mouse_move")
    public final LuaEvent MOUSE_MOVE = null;
    @LuaWhitelist
    @LuaFieldDoc("events.key_press")
    public final LuaEvent KEY_PRESS = null;
    @LuaWhitelist
    @LuaFieldDoc("events.mouse_press")
    public final LuaEvent MOUSE_PRESS = null;
    @LuaWhitelist
    @LuaFieldDoc("events.use_item")
    public final LuaEvent USE_ITEM = null;
    @LuaWhitelist
    @LuaFieldDoc("events.arrow_render")
    public final LuaEvent ARROW_RENDER = null;
    @LuaWhitelist
    @LuaFieldDoc("events.item_render")
    public final LuaEvent ITEM_RENDER = null;
    @LuaWhitelist
    @LuaFieldDoc("events.on_play_sound")
    public final LuaEvent ON_PLAY_SOUND = null;

    private final Map<String, LuaEvent> events = new HashMap<>() {{
            put("ENTITY_INIT", new LuaEvent());
            put("TICK", new LuaEvent());
            put("WORLD_TICK", new LuaEvent());
            put("RENDER", new LuaEvent());
            put("POST_RENDER", new LuaEvent());
            put("WORLD_RENDER", new LuaEvent());
            put("POST_WORLD_RENDER", new LuaEvent());
            put("CHAT_SEND_MESSAGE", new LuaEvent(true));
            put("CHAT_RECEIVE_MESSAGE", new LuaEvent());
            put("SKULL_RENDER", new LuaEvent());
            put("MOUSE_SCROLL", new LuaEvent());
            put("MOUSE_MOVE", new LuaEvent());
            put("MOUSE_PRESS", new LuaEvent());
            put("KEY_PRESS", new LuaEvent());
            put("USE_ITEM", new LuaEvent());
            put("ARROW_RENDER", new LuaEvent());
            put("ITEM_RENDER", new LuaEvent());
            put("ON_PLAY_SOUND", new LuaEvent());
    }};

    @LuaWhitelist
    @LuaMethodDoc("events.get_events")
    public Map<String, LuaEvent> getEvents() {
        return events;
    }

    @LuaWhitelist
    @LuaMetamethodDoc(overloads = @LuaMetamethodOverload(
            types = {LuaEvent.class, EventsAPI.class, String.class},
            comment = "events.__index.comment1"
    ))
    public LuaEvent __index(String key) {
        if (key == null) return null;
        return events.get(key.toUpperCase());
    }

    @LuaWhitelist
    public void __newindex(@LuaNotNil String key, LuaFunction func) {
        LuaEvent event = __index(key.toUpperCase());
        if (event != null)
            event.register(func, null);
        else throw new LuaError("Cannot assign value on key \"" + key + "\"");
    }

    @Override
    public String toString() {
        return "EventsAPI";
    }
}
