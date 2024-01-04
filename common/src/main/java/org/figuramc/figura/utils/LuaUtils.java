package org.figuramc.figura.utils;

import com.google.gson.*;
import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.Arrays;

import org.figuramc.figura.lua.api.json.FiguraJsonSerializer;
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.math.vector.FiguraVector;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class LuaUtils {

    /**
     * This is a generic vector parsing function that also parses the arguments after the vectors, allowing vectors to be at the beginning of the function signature
     * @param methodName The name of the function that is calling this function. Used for readable errors.
     * @param vectorSizes The sizes of Vectors to parse. The number of vectors is determined by the size of the array.
     * @param defaultValues When a Vector or a Vector argument is nil, it will be filled in with the value in this array at the correct index.
     * @param expectedReturns An array of Classes for what the extra arguments are supposed to be. Used for readable errors.
     * @param args The arguments of the function, passed in as varargs. 
     * @return The new args list with multi-number-argument Vectors being returned as real Vectors.
     */
    public static Object[] parseVec(String methodName, int[] vectorSizes, double[] defaultValues, Class<?>[] expectedReturns, Object ...args) {
        ArrayList<Object> ret = new ArrayList<Object>(args.length);
        int i=0;
        for(int size : vectorSizes) {
            if (args[i] instanceof FiguraVector vec){
                if(vec.size()!=size)
                    throw new LuaError("Illegal argument at position " + (i + 1) + " to " + methodName + "(): Expected Vector" + size + ", recieved Vector" + vec.size());
                ret.add(vec);
                i += 1;
            }
            else if (args[i]==null || args[i] instanceof Number) {
                double[] vec = new double[size];
                for (int o=0;o<size;o++){
                    if (args[i+o] instanceof Number n)
                        vec[o]=n.doubleValue();
                    else if(args[i+o] == null)
                        vec[o]=defaultValues[o];
                    else
                        throw new LuaError("Illegal argument at position " + (i + o + 1) + " to " + methodName + "():" + 
                            " Expected Number, recieved " + args[i+o].getClass().getSimpleName() + " (" + args[i+o] + ")"
                        );
                }
                ret.add(
                    switch(size){
                        case 2->FiguraVec2.of(vec[0], vec[1]);
                        case 3->FiguraVec3.of(vec[0], vec[1], vec[2]);
                        case 4->FiguraVec4.of(vec[0], vec[1], vec[2], vec[3]);
                        default->throw new IllegalArgumentException("Illegal vector size: " + size);
                    }
                );
                i += size;
            }
            else if(args[i]==null) {
                ret.add(
                    switch(size){
                        case 2->FiguraVec2.of(defaultValues[0], defaultValues[1]);
                        case 3->FiguraVec3.of(defaultValues[0], defaultValues[1], defaultValues[2]);
                        case 4->FiguraVec4.of(defaultValues[0], defaultValues[1], defaultValues[2], defaultValues[3]);
                        default->throw new IllegalArgumentException("Illegal vector size: " + size);
                    }
                );
                i += 1;
            }
            else
                throw new LuaError("Illegal argument at position " + (i + 1) + " to " + methodName + "():" + 
                    " Expected Vector" + size + " or Number, recieved " + args[i].getClass().getSimpleName() + " (" + args[i] + ")"
                );
        }
        for(int o = i; o < args.length; o++) {
            if(args[o] != null && (o-i) < expectedReturns.length && !expectedReturns[o-i].isAssignableFrom(args[o].getClass()))
                throw new LuaError("Illegal argument at position " + (o + 1) + " to " + methodName + "():" + 
                    " Expected " + expectedReturns[o-i].getSimpleName() + ", recieved " + args[o].getClass().getSimpleName() + " (" + args[o] + ")"
                );
            ret.add(args[o]);
        }
        return ret.toArray();
    }

    public static Object[] parseVec(String methodName, int[] vectorSizes, Class<?>[] expectedReturns, Object ...args) {
        return parseVec(methodName, vectorSizes, new double[]{0,0,0,0}, expectedReturns, args);
    }

    public static FiguraVec2 parseVec2(String methodName, Object x, Number y) {
        return parseVec2(methodName, x, y, 0, 0);
    }

    public static FiguraVec2 parseVec2(String methodName, Object x, Number y, double defaultX, double defaultY) {
        if (x instanceof FiguraVec2 vec)
            return vec.copy();
        if (x == null || x instanceof Number) {
            if (x == null) x = defaultX;
            if (y == null) y = defaultY;
            return FiguraVec2.of(((Number) x).doubleValue(), y.doubleValue());
        }
        throw new LuaError("Illegal argument to " + methodName + "(): " + x.getClass().getSimpleName());
    }

    /**
     * This code gets repeated SO MUCH that I decided to put it in the utils class.
     * @param x Either the x coordinate of a vector, or a vector itself.
     * @param y The y coordinate of a vector, used if the first parameter was a number.
     * @param z The z coordinate of a vector, used if the first parameter was a number.
     * @return A FiguraVec3 representing the data passed in.
     */
    public static FiguraVec3 parseVec3(String methodName, Object x, Number y, Number z) {
        return parseVec3(methodName, x, y, z, 0, 0, 0);
    }

    public static FiguraVec3 parseVec3(String methodName, Object x, Number y, Number z, double defaultX, double defaultY, double defaultZ) {
        if (x instanceof FiguraVec3 vec)
            return vec.copy();
        if (x == null || x instanceof Number) {
            if (x == null) x = defaultX;
            if (y == null) y = defaultY;
            if (z == null) z = defaultZ;
            return FiguraVec3.of(((Number) x).doubleValue(), y.doubleValue(), z.doubleValue());
        }
        throw new LuaError("Illegal argument to " + methodName + "(): " + x.getClass().getSimpleName());
    }

    public static FiguraVec3 parseOneArgVec(String methodName, Object x, Number y, Number z, double defaultArg) {
        double d = x instanceof Number n ? n.doubleValue() : defaultArg;
        return parseVec3(methodName, x, y, z, d, d, d);
    }

    public static FiguraVec3 nullableVec3(String methodName, Object x, Number y, Number z) {
        return x == null ? null : parseVec3(methodName, x, y, z);
    }

    public static Pair<FiguraVec3, FiguraVec3> parse2Vec3(String methodName, Object x, Object y, Number z, Object w, Number t, Number h, int xIndex) {
        FiguraVec3 a, b;

        if (x instanceof FiguraVec3 vec1) {
            a = vec1.copy();
            if (y instanceof FiguraVec3 vec2) {
                b = vec2.copy();
            } else if (y == null || y instanceof Number) {
                if (w == null || w instanceof Number) {
                    b = parseVec3(methodName, y, z, (Number) w);
                } else {
                    throw new LuaError("Illegal argument at position" + xIndex+3 + "to " + methodName + "(): " + w);
                }
            } else {
                throw new LuaError("Illegal argument at position "+ xIndex+1 + " to " + methodName + "(): " + y);
            }
        } else if (x instanceof Number && y == null || y instanceof Number) {
            a = parseVec3(methodName, x, (Number) y, z);
            if (w instanceof FiguraVec3 vec1) {
                b = vec1.copy();
            } else if (w == null || w instanceof Number) {
                b = parseVec3(methodName, w, t, h);
            } else {
                throw new LuaError("Illegal argument at position "+ xIndex+3 + " to " + methodName + "(): " + w);
            }
        } else {
            throw new LuaError("Illegal argument at position "+ xIndex + " to " + methodName + "(): " + x);
        }

        return Pair.of(a, b);
    }

    // These functions allow having vector parsing at the beggining of the function, taking into account other arguments.
    public static Pair<FiguraVec3, Object[]> parseVec3(String methodName, Class<?>[] expectedReturns, Object ...args) {
        Object[] parsed = parseVec(methodName, new int[]{3}, expectedReturns, args);
        return Pair.of((FiguraVec3)parsed[0], Arrays.copyOfRange(parsed, 1, parsed.length));
    }

    public static Pair<Pair<FiguraVec3, FiguraVec3>, Object[]> parse2Vec3(String methodName, Class<?>[] expectedReturns, Object ...args) {
        Object[] parsed = parseVec(methodName, new int[]{3,3}, expectedReturns, args);
        return Pair.of(
            Pair.of((FiguraVec3)parsed[0], (FiguraVec3)parsed[1]), 
            Arrays.copyOfRange(parsed, 2, parsed.length)
        );
    }

    public static FiguraVec4 parseVec4(String methodName, Object x, Number y, Number z, Number w, double defaultX, double defaultY, double defaultZ, double defaultW) {
        if (x instanceof FiguraVec3 vec)
            return FiguraVec4.of(vec.x, vec.y, vec.z, defaultW);
        if (x instanceof FiguraVec4 vec)
            return vec.copy();
        if (x == null || x instanceof Number) {
            if (x == null) x = defaultX;
            if (y == null) y = defaultY;
            if (z == null) z = defaultZ;
            if (w == null) w = defaultW;
            return FiguraVec4.of(((Number) x).doubleValue(), y.doubleValue(), z.doubleValue(), w.doubleValue());
        }
        throw new LuaError("Illegal argument to " + methodName + "(): " + x.getClass().getSimpleName());
    }

    public static ItemStack parseItemStack(String methodName, Object item) {
        if (item == null)
            return ItemStack.EMPTY;
        else if (item instanceof ItemStackAPI wrapper)
            return wrapper.itemStack;
        else if (item instanceof String string) {
            try {
                Level level = WorldAPI.getCurrentWorld();
                return ItemArgument.item(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).createItemStack(1, false);
            } catch (Exception e) {
                throw new LuaError("Could not parse item stack from string: " + string);
            }
        }

        throw new LuaError("Illegal argument to " + methodName + "(): " + item);
    }

    public static BlockState parseBlockState(String methodName, Object block) {
        if (block == null)
            return Blocks.AIR.defaultBlockState();
        else if (block instanceof BlockStateAPI wrapper)
            return wrapper.blockState;
        else if (block instanceof String string) {
            try {
                Level level = WorldAPI.getCurrentWorld();
                return BlockStateArgument.block(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).getState();
            } catch (Exception e) {
                throw new LuaError("Could not parse block state from string: " + string);
            }
        }

        throw new LuaError("Illegal argument to " + methodName + "(): " + block);
    }

    public static ResourceLocation parsePath(String path) {
        try {
            return new ResourceLocation(path);
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
    }

    public static Object[] parseBlockHitResult(HitResult hitResult) {
        if (hitResult instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            return new Object[]{new BlockStateAPI(WorldAPI.getCurrentWorld().getBlockState(pos), pos), FiguraVec3.fromVec3(blockHit.getLocation()), blockHit.getDirection().getName()};
        }
        return null;
    }

    public static int parseSlot(Object slot, Inventory inventory) {
        if (slot instanceof String s) {
            try {
                return SlotArgument.slot().parse(new StringReader(s));
            } catch (Exception e) {
                throw new LuaError("Unable to get slot \"" + slot + "\"");
            }
        } else if (slot instanceof Integer i) {
            if (i == -1 && inventory != null) {
                return inventory.getFreeSlot();
            } else {
                return i;
            }
        } else {
            throw new LuaError("Invalid type for getSlot: " + slot.getClass().getSimpleName());
        }
    }

    public static JsonElement asJsonValue(LuaValue value) {
        if (value.isnil()) return JsonNull.INSTANCE;
        if (value.isboolean()) return new JsonPrimitive(value.checkboolean());
        if (value instanceof LuaString s) return new JsonPrimitive(s.checkjstring());
        if (value.isint()) return new JsonPrimitive(value.checkint());
        if (value.isnumber()) return new JsonPrimitive(value.checkdouble());
        if (value.istable()) {
            LuaTable table = value.checktable();

            // If it's an "array" (uses numbers as keys)
            if (checkTableArray(table)) {
                JsonArray arr = new JsonArray();
                LuaValue[] keys = table.keys();
                int arrayLength = keys[keys.length-1].checkint();
                for(int i = 1; i <= arrayLength; i++) {
                    arr.add(asJsonValue(table.get(i)));
                }
                return arr;
            }
            // Otherwise, if it's a proper key-value table
            else {
                JsonObject object = new JsonObject();
                for (LuaValue key : table.keys()) {
                    object.add(key.tojstring(), asJsonValue(table.get(key)));
                }
                return object;
            }
        }
        if (value.isuserdata() && value.checkuserdata() instanceof FiguraJsonSerializer.JsonValue val) {
            return val.getElement();
        }
        // Fallback for things that shouldn't be converted (like functions)
        return null;
    }

    public static boolean checkTableArray(LuaTable table) {
        for (LuaValue key : table.keys()) {
            if (!key.isnumber()) return false;
        }

        return true;
    }
}
