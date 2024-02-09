package org.figuramc.figura.model.rendering.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.mixin.render.layers.elytra.ElytraLayerAccessor;
import org.figuramc.figura.model.TextureCustomization;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.UUID;

public class FiguraTextureSet {

    public final String name;
    public final FiguraTexture[] textures = new FiguraTexture[4];

    public FiguraTextureSet(String name, FiguraTexture mainData, FiguraTexture emissiveData, FiguraTexture specularData, FiguraTexture normalData) {
        this.name = name;
        textures[0] = mainData;
        textures[1] = emissiveData;
        textures[2] = specularData;
        textures[3] = normalData;
    }

    public void clean() {
        for (FiguraTexture texture : textures) {
            if (texture != null)
                texture.close();
        }
    }

    public void uploadIfNeeded() {
        for (FiguraTexture texture : textures) {
            if (texture != null)
                texture.uploadIfDirty();
        }
    }

    public int getWidth() {
        for (FiguraTexture texture : textures) {
            if (texture != null)
                return texture.getWidth();
        }
        return -1;
    }

    public int getHeight() {
        for (FiguraTexture texture : textures) {
            if (texture != null)
                return texture.getHeight();
        }
        return -1;
    }

    public ResourceLocation getOverrideTexture(UUID owner, TextureCustomization pair) {
        OverrideType type;

        if (pair == null || (type = pair.getOverrideType()) == null)
            return null;

        switch (type) {
            case SKIN:
            case CAPE:
            case ELYTRA: {
                ClientPacketListener connection = Minecraft.getInstance().getConnection();
                if (connection == null) {
                    return null;
                } else {
                    PlayerInfo info = connection.getPlayerInfo(owner);
                    if (info == null) {
                        return null;
                    } else {
                        switch (type) {
                            case CAPE:
                                return info.getCapeLocation();
                            case ELYTRA:
                                return info.getElytraLocation() == null ? ElytraLayerAccessor.getWingsLocation() : info.getElytraLocation();
                            default:
                                return info.getSkinLocation();
                        }
                    }
                }
            }
            case RESOURCE: {
                try {
                    return new ResourceLocation(String.valueOf(pair.getValue()));
                } catch (Exception ignored) {
                    return MissingTextureAtlasSprite.getLocation();
                }
            }
            case PRIMARY:
                return (textures[0] == null) ? null : textures[0].getLocation();
            case SECONDARY:
                return (textures[1] == null) ? null : textures[1].getLocation();
            case SPECULAR:
                return (textures[2] == null) ? null : textures[2].getLocation();
            case NORMAL:
                return (textures[3] == null) ? null : textures[3].getLocation();
            case CUSTOM: {
                try {
                    return ((FiguraTexture) pair.getValue()).getLocation();
                } catch (Exception ignored) {
                    return MissingTextureAtlasSprite.getLocation();
                }
            }
            default:
                return null;
        }
    }

    public enum OverrideType {
        SKIN,
        CAPE,
        ELYTRA,
        RESOURCE(String.class, "String"),
        PRIMARY,
        SECONDARY,
        SPECULAR,
        NORMAL,
        CUSTOM(FiguraTexture.class, "Texture");

        public final @Nullable Type argumentType;
        public final @Nullable String typeName;

        OverrideType() {
            argumentType = null;
            typeName = null;
        }

        OverrideType(@Nullable Type argumentType, String typeName) {
            this.argumentType = argumentType;
            this.typeName = typeName;
        }
    }
}
