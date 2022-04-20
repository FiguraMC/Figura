package org.moon.figura.model.rendering.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.BufferUtils;
import org.moon.figura.FiguraMod;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class FiguraTexture extends AbstractTexture implements Closeable {

    /**
     * The ID of the texture, used to register to Minecraft.
     */
    public final ResourceLocation textureID = new ResourceLocation(FiguraMod.MOD_ID, "avatar_tex/" + UUID.randomUUID());
    private boolean uploaded = false;

    /**
     * Native image holding the texture data for this texture.
     */
    private NativeImage nativeImage;

    private boolean isClosed = false;

    public FiguraTexture() {

    }

    public FiguraTexture(byte[] data) {
        //Read image from wrapper
        try {
            ByteBuffer wrapper = BufferUtils.createByteBuffer(data.length);
            wrapper.put(data);
            wrapper.rewind();
            nativeImage = NativeImage.read(wrapper);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load(ResourceManager manager) throws IOException {
    }

    //Called when a texture is first created and when it reloads
    //Registers the texture to minecraft, and uploads it to GPU.
    public void registerAndUpload() {
        if (uploaded) return;

        //Register texture under the ID, so Minecraft's rendering can use it.
        Minecraft.getInstance().getTextureManager().register(textureID, this);

        //Upload texture to GPU.
        TextureUtil.prepareImage(this.getId(), nativeImage.getWidth(), nativeImage.getHeight());
        nativeImage.upload(0, 0, 0, false);

        uploaded = true;
    }


    public int getWidth() {
        return nativeImage.getWidth();
    }

    public int getHeight() {
        return nativeImage.getHeight();
    }

    @Override
    public void close() {

        //Make sure it doesn't close twice (minecraft tries to close the texture when reloading textures
        if(isClosed) return;
        isClosed = true;

        //Close native image
        nativeImage.close();

        //Cache GLID and then release it on GPU
        RenderSystem.recordRenderCall(() -> {
            TextureUtil.releaseTextureId(this.id);
        });
    }
}