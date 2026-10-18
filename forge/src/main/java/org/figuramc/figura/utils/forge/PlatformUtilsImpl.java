package org.figuramc.figura.utils.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.utils.PlatformUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

public class PlatformUtilsImpl {
    public static Path getGameDir() {
        return FMLPaths.GAMEDIR.relative();
    }

    public static String getFiguraModVersionString() {
        return getModVersion(FiguraMod.MOD_ID);
    }

    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.relative();
    }

    public static boolean isModLoaded(String modId) {
        return ModList.getMods().stream().anyMatch(info -> info.getModId().equals(modId));
    }

    public static String getModVersion(String modId) {
        return FiguraModMetadataImpl.getMetadataForMod(modId).getModVersion().toString();
    }

    public static PlatformUtils.ModLoader getModLoader() {
        return PlatformUtils.ModLoader.FORGE;
    }

    public static InputStream loadFileFromRoot(String path) throws FileNotFoundException {
        File file = ModList.getModFileById(FiguraMod.MOD_ID).getFile().findResource(path).toFile();
        return new FileInputStream(file);
    }
}
