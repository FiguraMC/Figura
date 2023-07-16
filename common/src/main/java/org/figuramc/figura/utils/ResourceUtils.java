package org.figuramc.figura.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.figuramc.figura.FiguraMod;

import java.io.InputStream;
import java.util.Optional;

public class ResourceUtils {

    public static byte[] getResource(ResourceManager manager, ResourceLocation path) {
        Optional<Resource> optional = manager.getResource(path);
        if (optional.isPresent()) {
            try (InputStream is = optional.get().open()) {
                return is.readAllBytes();
            } catch (Exception e) {
                FiguraMod.LOGGER.error("", e);
            }
        }
        return null;
    }
}
