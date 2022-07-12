package org.moon.figura.lua.api;

import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(
        name = "AnimationAPI",
        description = "animation_api"
)
public class AnimationAPI {

    private final Avatar owner;

    public AnimationAPI(Avatar owner) {
        this.owner = owner;
    }

    @LuaWhitelist
    public static Object __index(@LuaNotNil AnimationAPI api, @LuaNotNil String arg) {
        return api.owner.animations.get(arg);
    }

    @Override
    public String toString() {
        return "AnimationAPI";
    }
}
