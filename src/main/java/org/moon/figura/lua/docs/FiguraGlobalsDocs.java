package org.moon.figura.lua.docs;

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
public abstract class FiguraGlobalsDocs {

    @LuaFieldDoc(description = "globals.vec")
    public JavaFunction vec;

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
