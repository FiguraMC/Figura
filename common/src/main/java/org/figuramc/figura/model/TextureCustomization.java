package org.figuramc.figura.model;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.mixin.render.MissingTextureAtlasSpriteAccessor;
import org.figuramc.figura.mixin.render.TextureAtlasAccessor;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.model.rendering.texture.FiguraTextureSet;
import org.luaj.vm2.LuaError;

import java.util.Optional;

public class TextureCustomization {

    private final FiguraTextureSet.OverrideType first;
    private final Object second;

    public TextureCustomization(FiguraTextureSet.OverrideType first, Object second) {
        this.first = first;
        this.second = second;
    }

    public FiguraTextureSet.OverrideType getOverrideType() {
        return first;
    }

    public Object getValue() {
        return second;
    }

    public FiguraTexture getTexture(Avatar avatar, FiguraTextureSet textureSet) {
        if (avatar.render == null) return null;

        ResourceLocation resourceLocation = textureSet.getOverrideTexture(avatar.owner, this);
        String name = resourceLocation.toString();
        if (avatar.renderer.customTextures.containsKey(name)) {
            return avatar.renderer.customTextures.get(name);
        }

        // is there a way to check if an atlas exists without getAtlas? cause that is the only thing that will cause an error, and try catch blocks can be pricy
        try {
            TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(resourceLocation);
            GpuTexture atlasGpuTexture = atlas.getTexture();
            TextureAtlasAccessor atlasAccessor = (TextureAtlasAccessor) atlas;
            NativeImage nativeImage = new NativeImage(atlasAccessor.figura$getWidth(), atlasAccessor.figura$getHeight(), false);
            int width = atlasAccessor.figura$getWidth();
            int height = atlasAccessor.figura$getHeight();

            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Atlas Read Buffer", 9, width * height * atlasGpuTexture.getFormat().pixelSize());
            encoder.copyTextureToBuffer(atlasGpuTexture, gpuBuffer, 0, () -> {
                try (GpuBuffer.MappedView readView = encoder.mapBuffer(gpuBuffer, true, false)) {
                    for (int k = 0; k < height; k++) {
                        for (int l = 0; l < width; l++) {
                            int m = readView.data().getInt((l + k * width) * atlasGpuTexture.getFormat().pixelSize());
                            nativeImage.setPixelABGR(l, height - k - 1, m | 0xFF000000);
                        }
                    }
                }
                gpuBuffer.close();
            }, 0);
            return avatar.registerTexture(name, nativeImage, false);
        } catch (Exception ignored) {}
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
            // if the string is a valid resourceLocation but does not point to a valid resource, missingno
            NativeImage image = resource.isPresent() ? NativeImage.read(resource.get().open()) : MissingTextureAtlasSpriteAccessor.generateImage(16, 16);
            return avatar.registerTexture(name, image, false);
        } catch (Exception e) {
            // spit an error if the player inputs a resource location that does point to a thing, but not to an image
            throw new LuaError(e.getMessage());
        }
    }
}
