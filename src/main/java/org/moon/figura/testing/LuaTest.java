package org.moon.figura.testing;

import net.fabricmc.loader.api.FabricLoader;
import org.moon.figura.FiguraMod;
import org.moon.figura.math.FiguraVec6;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaState53;
import org.terasology.jnlua.NativeSupport;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class LuaTest {

    public static void test() {
        setupNativesForLua();

        LuaState luaState = new LuaState53(999999);
        luaState.openLib(LuaState.Library.BASE);
        luaState.openLib(LuaState.Library.TABLE);
        luaState.openLib(LuaState.Library.STRING);
        luaState.openLib(LuaState.Library.MATH);

        (new TestObject()).pushToStack(luaState);
        luaState.setGlobal("globalTestVar");

        luaState.pushJavaFunction(state -> {
            if (state.isString(1)) {
                String v = state.toString(1);
                System.out.println(v);
            } else if (state.isNil(1)) {
                System.out.println("nil");
            } else if (state.isBoolean(1)) {
                System.out.println(state.toBoolean(1));
            } else if (state.isJavaObjectRaw(1)) {
                System.out.println("userdata");
            } else if (state.isTable(1)) {
                System.out.println(state.toJavaObject(1, Map.class));
            }
            return 0;
        });
        luaState.setGlobal("println");

        String testCode = "" +
                "local x = 651 " +
                "local y = 15 " +
                "println(x + y) " +
                "println(\"greetings\") " +
                "println(globalTestVar:getFive()) " +
                "globalTestVar:printHi() ";

        luaState.load(testCode, "main");
        luaState.call(0, 0);
    }

    public static void vectorTest() {
        setupNativesForLua();

        LuaState luaState = new LuaState53(999999);
        luaState.openLib(LuaState.Library.BASE);
        luaState.openLib(LuaState.Library.TABLE);
        luaState.openLib(LuaState.Library.STRING);
        luaState.openLib(LuaState.Library.MATH);
        luaState.pop(4); //Pop the four libraries we just put on there

        (new FiguraVec6(1, 2, 3, 4, 5, 6)).pushToStack(luaState);
        luaState.setGlobal("vec1");
        (new FiguraVec6(7, 5, 4, 2, 4, 1)).pushToStack(luaState);
        luaState.setGlobal("vec2");

        luaState.pushJavaFunction(state -> {
            if (state.isString(1)) {
                String v = state.toString(1);
                System.out.println(v);
            } else if (state.isNil(1)) {
                System.out.println("nil");
            } else if (state.isBoolean(1)) {
                System.out.println(state.toBoolean(1));
            } else if (state.isJavaObjectRaw(1)) {
                System.out.println("userdata");
            } else if (state.isTable(1)) {
                System.out.println(state.toJavaObject(1, Map.class));
            }
            return 0;
        });
        luaState.setGlobal("println");

        String testCode = "println(getmetatable(vec1)); local vec3 = vec1 + vec2; println(vec3); println(#vec3)";

        luaState.load(testCode, "main");
        luaState.call(0, 0);
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
