package org.moon.figura.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.moon.figura.FiguraMod;

import java.io.InputStream;

public class ResourceUtils {

    public static byte[] getResource(ResourceManager manager, ResourceLocation path) {
        try (InputStream is = manager.getResource(path).getInputStream()) {
            return is.readAllBytes();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
        return null;
    }
}
