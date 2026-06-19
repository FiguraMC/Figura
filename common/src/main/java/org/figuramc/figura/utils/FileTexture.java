package org.figuramc.figura.utils;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public class FileTexture extends DynamicTexture {

    private final Identifier id;

    private FileTexture(NativeImage image, Identifier id) {
        super(null, image);
        this.id = id;

        Minecraft.getInstance().getTextureManager().register(id, this);
    }

    public static FileTexture of(Path path) throws IOException {
        String s = path.toString();
        Identifier resourceLocation = FiguraIdentifier.of("file/" + FiguraIdentifier.formatPath(s));
        return new FileTexture(readImage(path), resourceLocation);
    }

    public static NativeImage readImage(Path path) throws IOException {
        byte[] bytes = IOUtils.readFileBytes(path);
        ByteBuffer wrapper = BufferUtils.createByteBuffer(bytes.length);

        wrapper.put(bytes);
        wrapper.rewind();

        return NativeImage.read(wrapper);
    }

    public Identifier getLocation() {
        return id;
    }
}
