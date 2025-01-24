package org.figuramc.figura.utils;

import com.sun.jna.Platform;
import org.figuramc.figura.FiguraMod;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class OpusUtils {
    private static final Map<String, String> platforms = new HashMap<>() {{
        put("darwin-aarch64", "dylib");
        put("darwin-x86-64", "dylib");
        put("linux-arm", "so");
        put("linux-aarch64", "so");
        put("linux-x86", "so");
        put("linux-x86-64", "so");
        put("win32-x86", "dll");
        put("win32-x86-64", "dll");
    }};

    private static boolean loaded = false;

    public static boolean isLoaded() {
        return loaded;
    }
    
    public static synchronized boolean loadBundled() throws IOException {
        if (loaded) return false;
        String nativesRoot = "";
        try {
            String platform = Platform.RESOURCE_PREFIX;
            String ext = platforms.get(platform);
            if (ext == null) throw new UnsupportedOperationException("Failed to load libopus natives for " + platform);

            String dir = String.format("/natives/%s/libopus.%s", platform, ext);
            NativeUtil.loadLibraryFromJar(dir);
            FiguraMod.debug("Loading " + platform + " natives");
            nativesRoot = dir;
            loaded = true;
        } finally {
            System.setProperty("opus.lib", nativesRoot);
        }
        return true;
    }
}