package org.moon.figura.avatars.model.rendering.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.avatars.Avatar;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.function.Function;

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
        return emissiveTex.getWidth();
    }

    public int getHeight() {
        if (mainTex != null)
            return mainTex.getHeight();
        return emissiveTex.getHeight();
    }

    public enum RenderTypes {
        CUTOUT_NO_CULL(RenderType::entityCutoutNoCull),
        CUTOUT(RenderType::entityCutout),
        TRANSLUCENT(RenderType::entityTranslucent),
        TRANSLUCENT_CULL(RenderType::entityTranslucentCull),

        EMISSIVE(RenderType::eyes),
        EMISSIVE_SOLID(resourceLocation -> RenderType.beaconBeam(resourceLocation, false)),

        END_PORTAL(t -> RenderType.endPortal(), true),
        GLINT(t ->  RenderType.entityGlintDirect(), true),
        GLINT2(t ->  RenderType.glintDirect(), true);

        private final Function<ResourceLocation, RenderType> func;
        private final boolean force;

        RenderTypes(Function<ResourceLocation, RenderType> func) {
            this(func, false);
        }

        RenderTypes(Function<ResourceLocation, RenderType> func, boolean force) {
            this.func = func;
            this.force = force;
        }

        public RenderType get(ResourceLocation id) {
            if (force)
                return func.apply(null);

            return id == null ? null : func.apply(id);
        }
    }

    public static ResourceLocation getOverrideTexture(Avatar owner, String type, String path) {
        if (type == null)
            return null;

        type = type.toLowerCase();
        if (type.equals("skin") || type.equals("cape") || type.equals("elytra")) {
            if (Minecraft.getInstance().player == null)
                return null;

            PlayerInfo info = Minecraft.getInstance().player.connection.getPlayerInfo(owner.owner);
            if (info == null)
                return null;

            if (type.equals("skin"))
                return info.getSkinLocation();
            else if (type.equals("cape"))
                return info.getCapeLocation();
            else
                return info.getElytraLocation();
        } else if (type.equals("resource")) {
            try {
                if (path == null)
                    return null;

                ResourceLocation resource = new ResourceLocation(path);
                return Minecraft.getInstance().getResourceManager().hasResource(resource) ? resource : MissingTextureAtlasSprite.getLocation();
            } catch (Exception ignored) {}
        }
        throw new LuaRuntimeException("Invalid texture override type: " + type);
    }
}
