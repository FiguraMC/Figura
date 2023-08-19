package org.figuramc.figura.model;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.api.TextureAPI;
import org.figuramc.figura.mixin.render.MissingTextureAtlasSpriteAccessor;
import org.figuramc.figura.mixin.render.TextureAtlasAccessor;
import org.figuramc.figura.model.rendering.ImmediateAvatarRenderer;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.model.rendering.texture.FiguraTextureSet;
import org.figuramc.figura.model.rendering.texture.RenderTypes;
import org.figuramc.figura.utils.LuaUtils;
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
            atlas.bind();
            TextureAtlasAccessor atlasAccessor = (TextureAtlasAccessor) atlas;
            NativeImage nativeImage = new NativeImage(atlasAccessor.getWidth(), atlasAccessor.getHeight(), false);
            nativeImage.downloadTexture(0, false);
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
