package org.figuramc.figura.utils.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;
import org.figuramc.figura.FiguraMod;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PlatformUtilsImpl {
    public static Path getGameDir() {
        return FMLPaths.GAMEDIR.relative();
    }

    public static String getModVersionString() {
        return ModList.get().getModContainerById(FiguraMod.MOD_ID).get().getModInfo().getVersion().getQualifier();
    }

    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.relative();
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
    
    public static Map<String, Object> getModMetadata(String modId) {
        if (!isModLoaded(modId))
            return null;
        IModInfo info = ModList.get().getModContainerById(modId).get().getModInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("id", info.getModId());
        map.put("name", info.getDisplayName());
        map.put("description", info.getDescription());
        map.put("version", info.getVersion().toString());
        map.put("namespace", info.getNamespace());
        map.put("mod_url", info.getModURL().map(URL::toString).orElse(""));
        map.put("update_url", info.getUpdateURL().map(URL::toString).orElse(""));
        map.put("logo", info.getLogoFile().orElse(""));
        map.put("logo_blurred", info.getLogoBlur());
        // TODO: I do not quite understand forge mod info, please fill it in with other values
        return map;
    }
}
