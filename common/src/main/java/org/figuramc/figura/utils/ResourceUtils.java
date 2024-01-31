package org.figuramc.figura.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;
import org.figuramc.figura.FiguraMod;

import java.io.InputStream;
import java.util.Optional;

public class ResourceUtils {

    public static byte[] getResource(ResourceManager manager, ResourceLocation path) {
        try (InputStream is = manager.getResource(path).getInputStream()) {
            return IOUtils.toByteArray(is);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
        return null;
    }
}
