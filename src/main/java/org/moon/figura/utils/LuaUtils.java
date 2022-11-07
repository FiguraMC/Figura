package org.moon.figura.utils;

import com.mojang.brigadier.StringReader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.luaj.vm2.LuaError;
import org.moon.figura.lua.api.world.BlockStateAPI;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;

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
                return ItemArgument.item(new CommandBuildContext(RegistryAccess.BUILTIN.get())).parse(new StringReader(string)).createItemStack(1, false);
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
                return BlockStateArgument.block(new CommandBuildContext(RegistryAccess.BUILTIN.get())).parse(new StringReader(string)).getState();
            } catch (Exception e) {
                throw new LuaError("Could not parse block state from string: " + string);
            }
        }

        throw new LuaError("Illegal argument to " + methodName + "(): " + block);
    }
}
