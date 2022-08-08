package org.moon.figura.lua.docs;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.lua.api.*;
import org.moon.figura.lua.api.entity.EntityAPI;
import org.moon.figura.lua.api.entity.PlayerAPI;
import org.moon.figura.lua.api.event.EventsAPI;
import org.moon.figura.lua.api.keybind.KeybindAPI;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
import org.moon.figura.lua.api.nameplate.NameplateAPI;
import org.moon.figura.lua.api.particle.ParticleAPI;
import org.moon.figura.lua.api.ping.PingAPI;
import org.moon.figura.lua.api.sound.SoundAPI;
import org.moon.figura.lua.api.vanilla_model.VanillaModelAPI;
import org.moon.figura.lua.api.world.WorldAPI;

/**
 * Class only exists to have docs for the global figura
 * functions/fields! This class should never end up in
 * anyone's code. It's not even whitelisted!
 */
@LuaTypeDoc(
        name = "globals",
        description = "globals"
)
public abstract class FiguraGlobalsDocs {

    @LuaFieldDoc(description = "globals.vec")
    public LuaFunction vec;
    @LuaFieldDoc(description = "globals.action_wheel")
    public ActionWheelAPI action_wheel;
    @LuaFieldDoc(description = "globals.animations")
    public LuaTable animations;
    @LuaFieldDoc(description = "globals.figura_metatables")
    public LuaTable figuraMetatables;
    @LuaFieldDoc(description = "globals.nameplate")
    public NameplateAPI nameplate;
    @LuaFieldDoc(description = "globals.world")
    public WorldAPI world;
    @LuaFieldDoc(description = "globals.vanilla_model")
    public VanillaModelAPI vanilla_model;
    @LuaFieldDoc(description = "globals.models")
    public FiguraModelPart models;
    @LuaFieldDoc(description = "globals.player")
    public PlayerAPI player;
    @LuaFieldDoc(description = "globals.events")
    public EventsAPI events;
    @LuaFieldDoc(description = "globals.keybind")
    public KeybindAPI keybind;
    @LuaFieldDoc(description = "globals.vectors")
    public VectorsAPI vectors;
    @LuaFieldDoc(description = "globals.matrices")
    public MatricesAPI matrices;
    @LuaFieldDoc(description = "globals.client")
    public ClientAPI client;
    @LuaFieldDoc(description = "globals.host")
    public HostAPI host;
    @LuaFieldDoc(description = "globals.avatar")
    public AvatarAPI avatar;
    @LuaFieldDoc(description = "globals.particles")
    public ParticleAPI particles;
    @LuaFieldDoc(description = "globals.sounds")
    public SoundAPI sounds;
    @LuaFieldDoc(description = "globals.renderer")
    public RendererAPI renderer;
    @LuaFieldDoc(description = "globals.user")
    public EntityAPI<?> user;
    @LuaFieldDoc(description = "globals.pings")
    public PingAPI pings;

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "scriptName"
            ),
            description = "globals.require"
    )
    public static Object require() {return null;}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Object.class,
                    argumentNames = "arg"
            ),
            description = "globals.print"
    )
    public static void print() {}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Object.class,
                    argumentNames = "arg"
            ),
            description = "globals.log"
    )
    public static void log() {}

    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LuaTable.class,
                            argumentNames = "table"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = LuaUserdata.class,
                            argumentNames = "javaObject"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {LuaTable.class, Integer.class},
                            argumentNames = {"table", "maxDepth"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {LuaUserdata.class, Integer.class},
                            argumentNames = {"javaObject", "maxDepth"}
                    )
            },
            description = "globals.print_table"
    )
    public static void printTable() {}

    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LuaTable.class,
                            argumentNames = "table"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = LuaUserdata.class,
                            argumentNames = "javaObject"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {LuaTable.class, Integer.class},
                            argumentNames = {"table", "maxDepth"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {LuaUserdata.class, Integer.class},
                            argumentNames = {"javaObject", "maxDepth"}
                    )
            },
            description = "globals.log_table"
    )
    public static void logTable() {}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "json"
            ),
            description = "globals.print_json"
    )
    public static void printJson() {}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "json"
            ),
            description = "globals.log_json"
    )
    public static void logJson() {}

}
