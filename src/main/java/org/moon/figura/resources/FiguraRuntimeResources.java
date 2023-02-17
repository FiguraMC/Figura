package org.moon.figura.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.packs.PathPackResources;
import org.moon.figura.FiguraMod;
import org.moon.figura.utils.IOUtils;

import java.nio.file.Path;

public class FiguraRuntimeResources {

    protected static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static final PathPackResources PACK = new PathPackResources(FiguraMod.MOD_NAME + " runtime resource pack", getRootDirectory(), true);

    public static Path getRootDirectory() {
        return IOUtils.getOrCreateDir(FiguraMod.getCacheDirectory(), "resources");
    }

    public static Path getAssetsDirectory() {
        return IOUtils.getOrCreateDir(getRootDirectory(), "assets/" + FiguraMod.MOD_ID);
    }

    public static void init() {
        FiguraLangManager.init();
    }
}
