package org.moon.figura.utils;

import com.mojang.brigadier.StringReader;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import org.moon.figura.FiguraMod;
import org.moon.figura.lua.api.world.ItemStackWrapper;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaType;
import org.terasology.jnlua.NativeSupport;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class LuaUtils {

    public static void printStack(LuaState state) {
        System.out.println("--Top of Stack--");
        for (int i = state.getTop(); i > 0; i--) {
            System.out.println(getString(state, i));
        }
        System.out.println("--Bottom of Stack--");
    }

    private static String getString(LuaState state, int index) {
        if (state.type(index) == LuaType.TABLE)
            return "(table)";
        Object o = state.toJavaObject(index, Object.class);
        return o == null ? "null" : o.toString();
    }

    public static Object[] getStack(LuaState state) {
        int size = state.getTop();
        Object[] result = new Object[size];
        for (int i = 0; i < size; i++)
            result[i] = state.toJavaObject(i+1, Object.class);
        return result;
    }

    /**
     * This code gets repeated SO MUCH that I decided to put it in the utils class.
     * @param x Either the x coordinate of a vector, or a vector itself.
     * @param y The y coordinate of a vector, used if the first parameter was a number.
     * @param z The z coordinate of a vector, used if the first parameter was a number.
     * @return A FiguraVec3 representing the data passed in.
     */
    public static FiguraVec3 parseVec3(String methodName, Object x, Double y, Double z) {
        return parseVec3(methodName, x, y, z, 0, 0, 0);
    }

    public static FiguraVec3 parseVec3(String methodName, Object x, Double y, Double z, double defaultX, double defaultY, double defaultZ) {
        if (x instanceof FiguraVec3 vec)
            return vec.copy();
        if (x == null || x instanceof Double) {
            if (x == null) x = defaultX;
            if (y == null) y = defaultY;
            if (z == null) z = defaultZ;
            return FiguraVec3.of((double) x, y, z);
        }
        throw new LuaRuntimeException("Illegal argument to " + methodName + "(): " + x);
    }

    public static FiguraVec2 parseVec2(String methodName, Object x, Double y) {
        return parseVec2(methodName, x, y, 0, 0);
    }

    public static FiguraVec2 parseVec2(String methodName, Object x, Double y, double defaultX, double defaultY) {
        if (x instanceof FiguraVec2 vec)
            return vec.copy();
        if (x == null || x instanceof Double) {
            if (x == null) x = defaultX;
            if (y == null) y = defaultY;
            return FiguraVec2.of((double) x, y);
        }
        throw new LuaRuntimeException("Illegal argument to " + methodName + "(): " + x);
    }

    public static ItemStack parseItemStack(String methodName, Object item) {
        if (item == null)
            return ItemStack.EMPTY;
        else if (item instanceof ItemStackWrapper wrapper)
            return ItemStackWrapper.getStack(wrapper);
        else if (item instanceof String string) {
            try {
                return ItemArgument.item(new CommandBuildContext(RegistryAccess.BUILTIN.get())).parse(new StringReader(string)).createItemStack(1, false);
            } catch (Exception e) {
                throw new LuaRuntimeException("Could not parse item stack from string: " + string);
            }
        }

        throw new LuaRuntimeException("Illegal argument to " + methodName + "(): " + item);
    }

    /**
     * Figures out the OS and copies the appropriate lua native binaries into a path, then loads them up
     * so that JNLua has access to them.
     */
    public static void setupNativesForLua() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        boolean isMacOS = System.getProperty("os.name").toLowerCase().contains("mac");
        FiguraMod.DO_OUR_NATIVES_WORK = true; //Here's hoping!
        StringBuilder builder = new StringBuilder("libjnlua-5.3-");
        if (isWindows) {
            builder.append("windows-");
        } else if (isMacOS) {
            builder.append("mac-");
        } else {
            builder.append("linux-");
        }

        String arch = System.getProperty("os.arch");
        if (arch.endsWith("64")) {
            if (arch.equals("aarch64"))
                builder.append("arm64");
            else
                builder.append("amd64");
        } else {
            builder.append("i686");
        }

        if (isWindows) {
            builder.append(".dll");
        } else {
            builder.append(".so");
        }

        String targetLib = "/assets/" + FiguraMod.MOD_ID + "/lua/natives/" + builder;
        String os = isWindows ? "windows" : (isMacOS ? "mac" : "linux");
        FiguraMod.LOGGER.info(String.format("Detecting: OS=\"%s\", ARCH=\"%s\", Attempting search for native file at %s", os, arch, targetLib));

        InputStream libStream = FiguraMod.class.getResourceAsStream(targetLib);
        Path dest = FiguraMod.getCacheDirectory().resolve(builder.toString()).toAbsolutePath();

        try {
            if (libStream == null) throw new Exception("Cannot read natives from resources");
            Files.copy(libStream, dest, StandardCopyOption.REPLACE_EXISTING);
            FiguraMod.LOGGER.debug("Successfully copied lua natives!");
        } catch (Exception e) {
            Minecraft.crash(new CrashReport("Failed to copy Lua natives with from: \"" + targetLib + "\" to \"" + dest + "\"", e));
        }

        NativeSupport.loadLocation = dest.toString();
    }
}
