package org.figuramc.figura.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.SharedConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

public class PlatformUtils {

    @ExpectPlatform
    public static Path getGameDir() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static String getFiguraModVersionString(){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path getConfigDir() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static String getModVersion(String modId) {
        throw new AssertionError();
    }

    public static int compareVersionTo(String v1, String v2) {
        if(v1 == null)
            return 1;
        String[] v1Parts = v1.split("[+,_]")[0].split("\\.");
        String[] v2Parts = v2.split("[+,_]")[0].split("\\.");
        int length = Math.max(v1Parts.length, v2Parts.length);
        for(int i = 0; i < length; i++) {
            int v1Part = i < v1Parts.length ?
                    Integer.parseInt(v1Parts[i]) : 0;
            int v2Part = i < v2Parts.length ?
                    Integer.parseInt(v2Parts[i]) : 0;
            if(v1Part < v2Part)
                return -1;
            if(v1Part > v2Part)
                return 1;
        }
        return 0;
    }

    public enum ModLoader {
        FORGE,
        FABRIC
    }

    @ExpectPlatform
    public static ModLoader getModLoader(){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static InputStream loadFileFromRoot(String file) throws FileNotFoundException {
        throw new AssertionError();
    }
}
