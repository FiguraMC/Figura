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
import org.figuramc.figura.lua.api.world.BlockStateAPI;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import javax.xml.validation.Validator;

public class LuaUtils {

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

    public static Pair<FiguraVec3, FiguraVec3> parse2Vec3(String methodName, Object x, Object y, Number z, Object w, Number t, Number h) {
        FiguraVec3 a, b;

        if (x instanceof FiguraVec3 vec1) {
            a = vec1.copy();
            if (y instanceof FiguraVec3 vec2) {
                b = vec2.copy();
            } else if (y == null || y instanceof Number) {
                if (w == null || w instanceof Number) {
                    b = parseVec3(methodName, y, z, (Number) w);
                } else {
                    throw new LuaError("Illegal argument to " + methodName + "(): " + w);
                }
            } else {
                throw new LuaError("Illegal argument to " + methodName + "(): " + y);
            }
        } else if (x == null || x instanceof Number && y == null || y instanceof Number) {
            a = parseVec3(methodName, x, (Number) y, z);
            if (w instanceof FiguraVec3 vec1) {
                b = vec1.copy();
            } else if (w == null || w instanceof Number) {
                b = parseVec3(methodName, w, t, h);
            } else {
                throw new LuaError("Illegal argument to " + methodName + "(): " + w);
            }
        } else {
            throw new LuaError("Illegal argument to " + methodName + "(): " + x);
        }

        return Pair.of(a, b);
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
        if (value.isint()) return new JsonPrimitive(value.checkint());
        if (value.isnumber()) return new JsonPrimitive(value.checkdouble());
        if (value.isstring()) return new JsonPrimitive(value.checkjstring());
        if (value.istable()) {
            LuaTable table = value.checktable();

            // If it's an "array" (uses numbers as keys)
            if (table.length() > 0) {
                JsonArray arr = new JsonArray();
                for (int i = 0; i < table.length(); i++) {
                    arr.add(asJsonValue(table.get(i+1)));
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

        // Fallback for things that shouldn't be converted (like functions)
        return null;
    }
}
