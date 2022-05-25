package org.moon.figura.utils;

import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import org.moon.figura.FiguraMod;
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

    /**
     * Figures out the OS and copies the appropriate lua native binaries into a path, then loads them up
     * so that JNLua has access to them.
     */
    public static void setupNativesForLua() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        boolean isMacOS = System.getProperty("os.name").toLowerCase().contains("mac");
        FiguraMod.DO_OUR_NATIVES_WORK = isWindows;
        StringBuilder builder = new StringBuilder("libjnlua-");
        builder.append("5.3-");
        if (isWindows) {
            builder.append("windows-");
        } else if (isMacOS) {
            builder.append("OUTDATEDmac-"); //Mark mac as outdated, it doesn't have the getter and setter natives yet
        } else {
            builder.append("linux-");
        }

        if (System.getProperty("os.arch").endsWith("64")) {
            builder.append("amd64");
        } else {
            builder.append("i686");
        }

        if (isWindows) {
            builder.append(".dll");
        } else if (isMacOS) {
            builder.append(".dylib");
        } else {
            builder.append(".so");
        }

        String targetLib = "/assets/" + FiguraMod.MOD_ID + "/lua/natives/" + builder;
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
