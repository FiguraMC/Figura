package org.moon.figura.lua.api;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.mixin.render.TextureAtlasAccessor;
import org.moon.figura.utils.LuaUtils;

import java.util.ArrayList;
import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "TextureAtlas",
        value = "texture_atlas"
)
public class TextureAtlasAPI {

    private final TextureAtlas atlas;

    public TextureAtlasAPI(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    @LuaWhitelist
    @LuaMethodDoc("texture_atlas.list_sprites")
    public List<String> listSprites() {
        List<String> list = new ArrayList<>();
        for (ResourceLocation res : ((TextureAtlasAccessor) atlas).getTexturesByName().keySet())
            list.add(res.toString());
        return list;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "path"
            ),
            value = "texture_atlas.get_sprite_uv"
    )
    public FiguraVec4 getSpriteUV(@LuaNotNil String sprite) {
        ResourceLocation spriteLocation = LuaUtils.parsePath(sprite);
        TextureAtlasSprite s = atlas.getSprite(spriteLocation);
        return FiguraVec4.of(s.getU0(), s.getV0(), s.getU1(), s.getV1());
    }

    @LuaWhitelist
    @LuaMethodDoc("texture_atlas.get_width")
    public int getWidth() {
        return ((TextureAtlasAccessor) atlas).getWidth();
    }

    @LuaWhitelist
    @LuaMethodDoc("texture_atlas.get_height")
    public int getHeight() {
        return ((TextureAtlasAccessor) atlas).getHeight();
    }

    @Override
    public String toString() {
        return "TextureAtlas (" + atlas.location() + ")";
    }
}
