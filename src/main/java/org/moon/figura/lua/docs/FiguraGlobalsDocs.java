package org.moon.figura.lua.docs;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.lua.api.*;
import org.moon.figura.lua.api.action_wheel.ActionWheelAPI;
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
        value = "globals"
)
public abstract class FiguraGlobalsDocs {

    @LuaFieldDoc("globals.vec")
    public LuaFunction vec;
    @LuaFieldDoc("globals.action_wheel")
    public ActionWheelAPI action_wheel;
    @LuaFieldDoc("globals.animations")
    public LuaTable animations;
    @LuaFieldDoc("globals.figura_metatables")
    public LuaTable figuraMetatables;
    @LuaFieldDoc("globals.nameplate")
    public NameplateAPI nameplate;
    @LuaFieldDoc("globals.world")
    public WorldAPI world;
    @LuaFieldDoc("globals.vanilla_model")
    public VanillaModelAPI vanilla_model;
    @LuaFieldDoc("globals.models")
    public FiguraModelPart models;
    @LuaFieldDoc("globals.player")
    public PlayerAPI player;
    @LuaFieldDoc("globals.events")
    public EventsAPI events;
    @LuaFieldDoc("globals.keybind")
    public KeybindAPI keybind;
    @LuaFieldDoc("globals.vectors")
    public VectorsAPI vectors;
    @LuaFieldDoc("globals.matrices")
    public MatricesAPI matrices;
    @LuaFieldDoc("globals.client")
    public ClientAPI client;
    @LuaFieldDoc("globals.host")
    public HostAPI host;
    @LuaFieldDoc("globals.avatar")
    public AvatarAPI avatar;
    @LuaFieldDoc("globals.particles")
    public ParticleAPI particles;
    @LuaFieldDoc("globals.sounds")
    public SoundAPI sounds;
    @LuaFieldDoc("globals.renderer")
    public RendererAPI renderer;
    @LuaFieldDoc("globals.user")
    public EntityAPI<?> user;
    @LuaFieldDoc("globals.pings")
    public PingAPI pings;
    @LuaFieldDoc("globals.type")
    public LuaFunction type;

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "scriptName"
            ),
            value = "globals.require"
    )
    public static Object require() {return null;}

    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload,
                    @LuaFunctionOverload(
                            argumentTypes = String.class,
                            argumentNames = "folder"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {String.class, Boolean.class},
                            argumentNames = {"folder", "subFolders"}
                    )
            },
            value = "globals.list_files"
    )
    public static LuaTable listFiles(String folder, boolean subFolders) {return null;}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Object.class,
                    argumentNames = "arg"
            ),
            value = "globals.print"
    )
    public static void print() {}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Object.class,
                    argumentNames = "arg"
            ),
            value = "globals.log"
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
            value = "globals.print_table"
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
            value = "globals.log_table"
    )
    public static void logTable() {}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "json"
            ),
            value = "globals.print_json"
    )
    public static void printJson() {}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "json"
            ),
            value = "globals.log_json"
    )
    public static void logJson() {}

}
