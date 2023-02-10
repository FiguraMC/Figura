package org.moon.figura.model.rendering.texture;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.mixin.render.layers.elytra.ElytraLayerAccessor;

import java.util.UUID;

public class FiguraTextureSet {
    public final FiguraTexture[] textures = new FiguraTexture[4];

    public FiguraTextureSet(FiguraTexture mainData, FiguraTexture emissiveData, FiguraTexture specularData, FiguraTexture normalData) {
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

    public ResourceLocation getOverrideTexture(UUID owner, Pair<OverrideType, Object> pair) {
        OverrideType type;

        if (pair == null || (type = pair.getFirst()) == null)
            return null;

        return switch (type) {
            case SKIN, CAPE, ELYTRA -> {
                ClientPacketListener connection = Minecraft.getInstance().getConnection();
                if (connection == null)
                    yield null;

                PlayerInfo info = connection.getPlayerInfo(owner);
                if (info == null)
                    yield null;

                yield switch (type) {
                    case CAPE -> info.getCapeLocation();
                    case ELYTRA -> info.getElytraLocation() == null ? ElytraLayerAccessor.getWingsLocation() : info.getElytraLocation();
                    default -> info.getSkinLocation();
                };
            }
            case RESOURCE -> {
                try {
                    ResourceLocation resource = new ResourceLocation(String.valueOf(pair.getSecond()));
                    yield Minecraft.getInstance().getTextureManager().getTexture(resource, null) != null ? resource : MissingTextureAtlasSprite.getLocation();
                } catch (Exception ignored) {
                    yield MissingTextureAtlasSprite.getLocation();
                }
            }
            case PRIMARY -> textures[0] == null ? null : textures[0].getLocation();
            case SECONDARY -> textures[1] == null ? null : textures[1].getLocation();
            case SPECULAR -> textures[2] == null ? null : textures[2].getLocation();
            case NORMAL -> textures[3] == null ? null : textures[3].getLocation();
            case CUSTOM -> {
                try {
                    yield ((FiguraTexture) pair.getSecond()).getLocation();
                } catch (Exception ignored) {
                    yield MissingTextureAtlasSprite.getLocation();
                }
            }
        };
    }

    public enum OverrideType {
        SKIN,
        CAPE,
        ELYTRA,
        RESOURCE,
        PRIMARY,
        SECONDARY,
        SPECULAR,
        NORMAL,
        CUSTOM
    }
}
