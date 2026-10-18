package org.figuramc.figura.model;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
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
        if (avatar.renderer == null) return null;

        Identifier resourceLocation = textureSet.getOverrideTexture(avatar.owner, this);
        if (resourceLocation == null) return null;
        String name = resourceLocation.toString();
        if (avatar.renderer.customTextures.containsKey(name)) {
            return avatar.renderer.customTextures.get(name);
        }

        AbstractTexture atlas = Minecraft.getInstance().getTextureManager().getTexture(resourceLocation);
        if (atlas instanceof TextureAtlas) {
            GpuTexture atlasGpuTexture = atlas.getTexture();
            TextureAtlasAccessor atlasAccessor = (TextureAtlasAccessor) atlas;
            int width = atlasAccessor.figura$getWidth();
            int height = atlasAccessor.figura$getHeight();
            var textureMap = avatar.renderer.customTextures;
            NativeImage nativeImage = new NativeImage(width, height, false);
            GpuBuffer gpuBuffer = null;
            FiguraTexture result = null;
            boolean scheduled = false;
            try {
                gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Atlas Read Buffer", 9, (long) width * height * atlasGpuTexture.getFormat().blockSize());
                result = avatar.registerTexture(name, nativeImage, false);
                GpuBuffer readBuffer = gpuBuffer;
                FiguraTexture texture = result;
                CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
                encoder.copyTextureToBuffer(atlasGpuTexture, readBuffer, 0, () -> {
                    try {
                        if (nativeImage.isClosed())
                            return;
                        try (com.mojang.renderpearl.api.buffers.GpuBufferSlice.MappedView readView = readBuffer.map(true, false)) {
                            for (int k = 0; k < height; k++) {
                                for (int l = 0; l < width; l++) {
                                    int m = readView.data().getInt((l + k * width) * atlasGpuTexture.getFormat().blockSize());
                                    nativeImage.setPixelABGR(l, height - k - 1, m | 0xFF000000);
                                }
                            }
                        }
                        texture.update();
                    } finally {
                        readBuffer.close();
                    }
                }, 0);
                scheduled = true;
                return texture;
            } finally {
                if (!scheduled) {
                    if (gpuBuffer != null)
                        gpuBuffer.close();
                    if (result != null) {
                        textureMap.remove(name, result);
                        result.close();
                    } else {
                        nativeImage.close();
                    }
                }
            }
        }
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
