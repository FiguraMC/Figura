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
    public final LuaEvent ENTITY_INIT = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.tick")
    public final LuaEvent TICK = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.world_tick")
    public final LuaEvent WORLD_TICK = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.render")
    public final LuaEvent RENDER = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.post_render")
    public final LuaEvent POST_RENDER = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.world_render")
    public final LuaEvent WORLD_RENDER = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.post_world_render")
    public final LuaEvent POST_WORLD_RENDER = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.chat_send_message")
    public final LuaEvent CHAT_SEND_MESSAGE = new LuaEvent(true);
    @LuaWhitelist
    @LuaFieldDoc("events.chat_receive_message")
    public final LuaEvent CHAT_RECEIVE_MESSAGE = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.skull_render")
    public final LuaEvent SKULL_RENDER = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.mouse_scroll")
    public final LuaEvent MOUSE_SCROLL = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.mouse_move")
    public final LuaEvent MOUSE_MOVE = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.mouse_press")
    public final LuaEvent MOUSE_PRESS = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.key_press")
    public final LuaEvent KEY_PRESS = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.char_typed")
    public final LuaEvent CHAR_TYPED = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.use_item")
    public final LuaEvent USE_ITEM = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.arrow_render")
    public final LuaEvent ARROW_RENDER = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.item_render")
    public final LuaEvent ITEM_RENDER = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.on_play_sound")
    public final LuaEvent ON_PLAY_SOUND = new LuaEvent();
    @LuaWhitelist
    @LuaFieldDoc("events.resource_reload")
    public final LuaEvent RESOURCE_RELOAD = new LuaEvent();

    private final Map<String, LuaEvent> events = new HashMap<>() {{
            put("ENTITY_INIT", ENTITY_INIT);
            put("TICK", TICK);
            put("WORLD_TICK", WORLD_TICK);
            put("RENDER", RENDER);
            put("POST_RENDER", POST_RENDER);
            put("WORLD_RENDER", WORLD_RENDER);
            put("POST_WORLD_RENDER", POST_WORLD_RENDER);
            put("CHAT_SEND_MESSAGE", CHAT_SEND_MESSAGE);
            put("CHAT_RECEIVE_MESSAGE", CHAT_RECEIVE_MESSAGE);
            put("SKULL_RENDER", SKULL_RENDER);
            put("MOUSE_SCROLL", MOUSE_SCROLL);
            put("MOUSE_MOVE", MOUSE_MOVE);
            put("MOUSE_PRESS", MOUSE_PRESS);
            put("KEY_PRESS", KEY_PRESS);
            put("CHAR_TYPED", CHAR_TYPED);
            put("USE_ITEM", USE_ITEM);
            put("ARROW_RENDER", ARROW_RENDER);
            put("ITEM_RENDER", ITEM_RENDER);
            put("ON_PLAY_SOUND", ON_PLAY_SOUND);
            put("RESOURCE_RELOAD", RESOURCE_RELOAD);
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
