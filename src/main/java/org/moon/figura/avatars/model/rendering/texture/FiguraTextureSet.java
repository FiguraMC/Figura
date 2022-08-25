package org.moon.figura.avatars.model.rendering.texture;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.mixin.render.layers.elytra.ElytraLayerAccessor;

import java.util.UUID;

public class FiguraTextureSet {

    public final String name;
    public final FiguraTexture mainTex, emissiveTex;

    public FiguraTextureSet(String name, byte[] mainData, byte[] emissiveData) {
        this.name = name;
        mainTex = mainData == null ? null : new FiguraTexture(mainData);
        emissiveTex = emissiveData == null ? null : new FiguraTexture(emissiveData);
    }

    public void clean() {
        if (mainTex != null)
            mainTex.close();
        if (emissiveTex != null)
            emissiveTex.close();
    }

    public void uploadIfNeeded() {
        if (mainTex != null)
            mainTex.registerAndUpload();
        if (emissiveTex != null)
            emissiveTex.registerAndUpload();
    }

    public int getWidth() {
        if (mainTex != null)
            return mainTex.getWidth();
        else if (emissiveTex != null)
            return emissiveTex.getWidth();
        else
            return -1;
    }

    public int getHeight() {
        if (mainTex != null)
            return mainTex.getHeight();
        else if (emissiveTex != null)
            return emissiveTex.getHeight();
        else
            return -1;
    }

    public ResourceLocation getOverrideTexture(UUID owner, Pair<OverrideType, String> pair) {
        OverrideType type = pair.getFirst();

        if (type == null)
            return null;

        return switch (type) {
            case SKIN, CAPE, ELYTRA -> {
                if (Minecraft.getInstance().player == null)
                    yield null;

                PlayerInfo info = Minecraft.getInstance().player.connection.getPlayerInfo(owner);
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
                    ResourceLocation resource = new ResourceLocation(pair.getSecond());
                    yield Minecraft.getInstance().getResourceManager().getResource(resource).isPresent() ? resource : MissingTextureAtlasSprite.getLocation();
                } catch (Exception ignored) {
                    yield MissingTextureAtlasSprite.getLocation();
                }
            }
            case PRIMARY -> mainTex == null ? null : mainTex.textureID;
            case SECONDARY -> emissiveTex == null ? null : emissiveTex.textureID;
        };
    }

    public enum OverrideType {
        SKIN,
        CAPE,
        ELYTRA,
        RESOURCE,
        PRIMARY,
        SECONDARY
    }
}
