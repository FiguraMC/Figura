package org.moon.figura.lua.api;

import com.mojang.blaze3d.platform.NativeImage;
import org.luaj.vm2.LuaError;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.rendering.texture.FiguraTexture;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.utils.ColorUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "TextureAPI",
        value = "textures"
)
public class TextureAPI {

    private final Avatar owner;

    public TextureAPI(Avatar owner) {
        this.owner = owner;
    }

    private void check() {
        if (owner.renderer == null)
            throw new LuaError("Avatar have no active renderer!");
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {String.class, Integer.class, Integer.class},
                    argumentNames = {"name", "width", "height"}
            ),
            value = "textures.register")
    public FiguraTexture register(@LuaNotNil String name, int width, int height) {
        NativeImage image;
        try {
            image = new NativeImage(width, height, true);
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }

        FiguraTexture oldText = __index(name);
        if (oldText != null)
            oldText.close();

        image.fillRect(0, 0, width, height, ColorUtils.rgbaToIntABGR(ColorUtils.Colors.FRAN_PINK.vec.augmented()));
        FiguraTexture texture = new FiguraTexture(owner.owner, image, name);
        texture.registerAndUpload();

        owner.renderer.customTextures.put(name, texture);
        return texture;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "name"
            ),
            value = "textures.get")
    public FiguraTexture get(@LuaNotNil String name) {
        check();
        return owner.renderer.customTextures.get(name);
    }

    @LuaWhitelist
    public FiguraTexture __index(@LuaNotNil String name) {
        return get(name);
    }

    @Override
    public String toString() {
        return "TextureAPI";
    }
}
