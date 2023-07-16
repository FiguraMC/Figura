package org.figuramc.figura.utils;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public class FileTexture extends DynamicTexture {

    private final ResourceLocation id;

    private FileTexture(NativeImage image, ResourceLocation id) {
        super(image);
        this.id = id;

        Minecraft.getInstance().getTextureManager().register(id, this);
    }

    public static FileTexture of(Path path) throws IOException {
        String s = path.toString();
        ResourceLocation resourceLocation = new FiguraIdentifier("file/" + FiguraIdentifier.formatPath(s));
        return new FileTexture(readImage(path), resourceLocation);
    }

    public static NativeImage readImage(Path path) throws IOException {
        byte[] bytes = IOUtils.readFileBytes(path);
        ByteBuffer wrapper = BufferUtils.createByteBuffer(bytes.length);

        wrapper.put(bytes);
        wrapper.rewind();

        return NativeImage.read(wrapper);
    }

    public ResourceLocation getLocation() {
        return id;
    }
}
