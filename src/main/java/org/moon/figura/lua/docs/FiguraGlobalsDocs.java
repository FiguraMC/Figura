package org.moon.figura.lua.docs;

import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.lua.api.EventsAPI;
import org.moon.figura.lua.api.ParticleAPI;
import org.moon.figura.lua.api.SoundAPI;
import org.moon.figura.lua.api.entity.PlayerEntityWrapper;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
import org.moon.figura.lua.api.model.VanillaModelAPI;
import org.moon.figura.lua.api.nameplate.NameplateAPI;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.lua.types.LuaTable;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.TypedJavaObject;

/**
 * Class only exists to have docs for the global figura
 * functions/fields! This class should never end up in
 * anyone's code. It's not even whitelisted!
 */
@LuaTypeDoc(
        name = "globals",
        description = "globals"
)
public class FiguraGlobalsDocs {

    @LuaFieldDoc(description = "globals.vectors")
    public VectorsAPI vectors;
    @LuaFieldDoc(description = "globals.vec")
    public JavaFunction vec;
    @LuaFieldDoc(description = "globals.matrices")
    public MatricesAPI matrices;
    @LuaFieldDoc(description = "globals.models")
    public FiguraModelPart models;
    @LuaFieldDoc(description = "globals.events")
    public EventsAPI events;
    @LuaFieldDoc(description = "globals.vanilla_model")
    public VanillaModelAPI vanilla_model;
    @LuaFieldDoc(description = "globals.world")
    public WorldAPI world;
    @LuaFieldDoc(description = "globals.player")
    public PlayerEntityWrapper player;
    @LuaFieldDoc(description = "globals.particle")
    public ParticleAPI particle;
    @LuaFieldDoc(description = "globals.sound")
    public SoundAPI sound;
    @LuaFieldDoc(description = "globals.nameplate")
    public NameplateAPI nameplate;

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "scriptName"
            ),
            description = "globals.require"
    )
    public static void require() {}

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
                            argumentTypes = TypedJavaObject.class,
                            argumentNames = "javaObject"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {LuaTable.class, Integer.class},
                            argumentNames = {"table", "maxDepth"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {TypedJavaObject.class, Integer.class},
                            argumentNames = {"javaObject", "maxDepth"}
                    )
            },
            description = "globals.printTable"
    )
    public static void printTable() {}

    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = LuaTable.class,
                            argumentNames = "table"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = TypedJavaObject.class,
                            argumentNames = "javaObject"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {LuaTable.class, Integer.class},
                            argumentNames = {"table", "maxDepth"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {TypedJavaObject.class, Integer.class},
                            argumentNames = {"javaObject", "maxDepth"}
                    )
            },
            description = "globals.logTable"
    )
    public static void logTable() {}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "json"
            ),
            description = "globals.printJson"
    )
    public static void printJson() {}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "json"
            ),
            description = "globals.logJson"
    )
    public static void logJson() {}
}
