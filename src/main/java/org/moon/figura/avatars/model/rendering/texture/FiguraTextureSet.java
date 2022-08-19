package org.moon.figura.avatars.model.rendering.texture;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.luaj.vm2.LuaError;
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

    public static ResourceLocation getOverrideTexture(UUID owner, Pair<String, String> pair) {
        String type = pair.getFirst();

        if (type == null)
            return null;

        type = type.toLowerCase();
        return switch (type) {
            case "skin", "cape", "elytra" -> {
                if (Minecraft.getInstance().player == null)
                    yield null;

                PlayerInfo info = Minecraft.getInstance().player.connection.getPlayerInfo(owner);
                if (info == null)
                    yield null;

                yield switch (type) {
                    case "cape" -> info.getCapeLocation();
                    case "elytra" -> info.getElytraLocation() == null ? ElytraLayerAccessor.getWingsLocation() : info.getElytraLocation();
                    default -> info.getSkinLocation();
                };
            }
            case "resource" -> {
                try {
                    ResourceLocation resource = new ResourceLocation(pair.getSecond());
                    yield Minecraft.getInstance().getResourceManager().hasResource(resource) ? resource : MissingTextureAtlasSprite.getLocation();
                } catch (Exception ignored) {
                    yield MissingTextureAtlasSprite.getLocation();
                }
            }
            case "texture" -> null;
            default -> throw new LuaError("Invalid texture override type: " + type);
        };
    }
}
