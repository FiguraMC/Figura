package org.moon.figura.lua;

import net.fabricmc.loader.api.FabricLoader;
import org.moon.figura.FiguraMod;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.NativeSupport;

import java.io.File;
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
        Object o = state.toJavaObject(index, Object.class);
        return o == null ? "null" : o.toString();
    }

    /**
     * Figures out the OS and copies the appropriate lua native binaries into a path, then loads them up
     * so that JNLua has access to them.
     */
    public static void setupNativesForLua() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        boolean isMacOS = System.getProperty("os.name").toLowerCase().contains("mac");
        StringBuilder builder = new StringBuilder(isWindows ? "libjnlua-" : "jnlua-");
        builder.append("5.3-");
        if (isWindows) {
            builder.append("windows-");
        } else if (isMacOS) {
            builder.append("mac-");
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

        Path nativesFolder = FabricLoader.getInstance().getGameDir().normalize().resolve("libraries/lua-natives/");

        String targetLib = "/natives/" + builder;
        InputStream libStream = FiguraMod.class.getResourceAsStream(targetLib);
        File f = nativesFolder.resolve(builder.toString()).toFile();

        try {
            if (libStream == null) throw new Exception("Cannot read natives from resources");
            Files.createDirectories(nativesFolder);
            Files.copy(libStream, f.toPath().toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to copy Lua natives");
            FiguraMod.LOGGER.error(e);
        }

        NativeSupport.loadLocation = f.getAbsolutePath();
    }
}
