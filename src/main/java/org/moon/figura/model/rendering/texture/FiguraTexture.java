package org.moon.figura.model.rendering.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.lwjgl.BufferUtils;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.LuaUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(
        name = "Texture",
        value = "texture"
)
public class FiguraTexture extends AbstractTexture implements Closeable {

    /**
     * The ID of the texture, used to register to Minecraft.
     */
    public final ResourceLocation textureID;
    private boolean registered = false;
    private boolean dirty = true;
    private final String name;
    private final Avatar owner;

    /**
     * Native image holding the texture data for this texture.
     */
    private final NativeImage texture;
    private NativeImage backup;
    private boolean isClosed = false;

    public FiguraTexture(Avatar owner, String name, byte[] data) {
        //Read image from wrapper
        NativeImage image;
        try {
            ByteBuffer wrapper = BufferUtils.createByteBuffer(data.length);
            wrapper.put(data);
            wrapper.rewind();
            image = NativeImage.read(wrapper);
        } catch (IOException e) {
            FiguraMod.LOGGER.error("", e);
            image = new NativeImage(1, 1, true);
        }

        this.texture = image;
        this.textureID = new FiguraIdentifier("avatar_tex/" + owner.owner + "/" + UUID.randomUUID());
        this.name = name;
        this.owner = owner;
    }

    public FiguraTexture(Avatar owner, String name, NativeImage image) {
        this.texture = image;
        this.textureID = new FiguraIdentifier("avatar_tex/" + owner.owner + "/custom/" + name);
        this.name = name;
        this.owner = owner;
    }

    @Override
    public void load(ResourceManager manager) throws IOException {}

    //Called when a texture is first created and when it reloads
    //Registers the texture to minecraft, and uploads it to GPU.
    public void registerAndUpload() {
        if (!registered) {
            //Register texture under the ID, so Minecraft's rendering can use it.
            Minecraft.getInstance().getTextureManager().register(textureID, this);
            registered = true;
        }

        if (dirty) {
            //Upload texture to GPU.
            TextureUtil.prepareImage(this.getId(), texture.getWidth(), texture.getHeight());
            texture.upload(0, 0, 0, false);
            dirty = false;
        }
    }

    public int getWidth() {
        return texture.getWidth();
    }

    public int getHeight() {
        return texture.getHeight();
    }

    @Override
    public void close() {
        //Make sure it doesn't close twice (minecraft tries to close the texture when reloading textures
        if (isClosed) return;

        isClosed = true;

        //Close native image
        texture.close();

        //Cache GLID and then release it on GPU
        RenderSystem.recordRenderCall(() -> TextureUtil.releaseTextureId(this.id));
    }

    public void saveCache() throws IOException {
        Path path = FiguraMod.getCacheDirectory().resolve("saved_texture.png");
        texture.writeToFile(path);
    }

    private void backupImage() {
        if (this.backup == null) {
            backup = new NativeImage(texture.format(), texture.getWidth(), texture.getHeight(), true);
            backup.copyFrom(texture);
        }
    }

    // -- lua stuff -- //


    private FiguraVec4 parseColor(String method, Object r, Double g, Double b, Double a) {
        return LuaUtils.parseVec4(method, r, g, b, a, 0, 0, 0, 1);
    }

    @LuaWhitelist
    @LuaMethodDoc("texture.get_name")
    public String getName() {
        return name;
    }

    @LuaWhitelist
    @LuaMethodDoc("texture.get_dimensions")
    public FiguraVec2 getDimensions() {
        return FiguraVec2.of(getWidth(), getHeight());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Integer.class, Integer.class},
                    argumentNames = {"x", "y"}
            ),
            value = "texture.get_pixel")
    public FiguraVec4 getPixel(int x, int y) {
        try {
            return ColorUtils.abgrToRGBA(texture.getPixelRGBA(x, y));
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class, FiguraVec3.class},
                            argumentNames = {"x", "y", "rgb"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class, FiguraVec4.class},
                            argumentNames = {"x", "y", "rgba"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "r", "g", "b", "a"}
                    )
            },
            value = "texture.set_pixel")
    public void setPixel(int x, int y, Object r, Double g, Double b, Double a) {
        try {
            backupImage();
            texture.setPixelRGBA(x, y, ColorUtils.rgbaToIntABGR(parseColor("setPixel", r, g, b, a)));
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class, Integer.class, Integer.class, FiguraVec3.class},
                            argumentNames = {"x", "y", "width", "height", "rgb"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class, Integer.class, Integer.class, FiguraVec4.class},
                            argumentNames = {"x", "y", "width", "height", "rgba"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class, Integer.class, Integer.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "width", "height", "r", "g", "b", "a"}
                    )
            },
            value = "texture.fill")
    public void fill(int x, int y, int width, int height, Object r, Double g, Double b, Double a) {
        try {
            backupImage();
            texture.fillRect(x, y, width, height, ColorUtils.rgbaToIntABGR(parseColor("fill", r, g, b, a)));
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("texture.update")
    public void update() {
        this.dirty = true;
    }

    @LuaWhitelist
    @LuaMethodDoc("texture.restore")
    public void restore() {
        if (backup == null)
            return;

        this.texture.copyFrom(backup);
        backup = null;
    }

    @LuaWhitelist
    @LuaMethodDoc("texture.save")
    public String save() {
        try {
            return Base64.getEncoder().encodeToString(texture.asByteArray());
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Integer.class, Integer.class, Integer.class, Integer.class, LuaFunction.class},
                    argumentNames = {"x", "y", "width", "height", "func"}
            ),
            value = "texture.apply_func"
    )
    public void applyFunc(int x, int y, int width, int height, @LuaNotNil LuaFunction function) {
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                FiguraVec4 color = getPixel(j, i);
                LuaValue result = function.call(owner.luaRuntime.typeManager.javaToLua(color).arg1());
                if (!result.isnil() && result.isuserdata(FiguraVec4.class))
                    setPixel(j, i, result.checkuserdata(FiguraVec4.class), null, null, null);
            }
        }
    }

    @Override
    public String toString() {
        return name + " (" + getWidth() + "x" + getHeight() + ") (Texture)";
    }
}