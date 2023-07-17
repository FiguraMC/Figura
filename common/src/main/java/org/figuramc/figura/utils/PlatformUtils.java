package org.figuramc.figura.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;
import java.util.Map;

public class PlatformUtils {

    @ExpectPlatform
    public static Path getGameDir() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static String getModVersionString(){
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
    public static Map<String, Object> getModMetadata(String modId){
        throw new AssertionError();
    }
}
