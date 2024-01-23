package org.figuramc.figura.utils;

import java.nio.file.Path;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

public class PathUtils {

    public static Path getPath (String path) {
        if (path.isEmpty()) return Path.of("/");
        if (!path.startsWith(".")) path = "/" + path;
        return Path.of(path
            .replaceAll("\\\\", "/")
            .replaceAll("[\\.]([^\\./])", "/$1")
            .replaceAll("\\/\\/", "/"));
    }

    public static Path getPath (LuaValue path) {
        String str = path.isnil() ? "/" : path.checkjstring();
        return getPath(str);
    }

    public static Path getWorkingDirectory(LuaFunction debugGetinfo) {
        // Navigate up the stack trace and grab the first non-Java source
        int i=1;
        String file = "";
        do {
            LuaValue stack = debugGetinfo.call(LuaValue.valueOf(i++));
            if(stack.isnil()) // shouldn't happen, but guards against potential infinite loops
                break;
            file = stack.get("source").checkjstring();
        } while (file.equals("=[Java]"));

        Path path = Path.of("/" + file);
        return path.getNameCount() > 1 ? path.resolve("../").normalize() : Path.of("/");
    }

    public static String computeSafeString(Path path) {
        String str = path == null ? "" : path.normalize().toString();
        return str
            .replaceAll("\\\\", "/")
            .replaceAll("^\\/+", "");
    }

    public static String computeSafeString(String path) {
        return computeSafeString(getPath(path));
    }

    public static boolean isAbsolute(Path path) {
        return path.getRoot() != null || !path.toString().startsWith(".");
    }

    public static boolean isAbsolute(String path) {
        return isAbsolute(getPath(path));
    }

}
