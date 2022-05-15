package org.moon.figura.avatars.model.rendering.texture;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

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

        public RenderType get(FiguraTexture texture) {
            if (force)
                return func.apply(null);

            return texture == null ? null : func.apply(texture.textureID);
        }
    }
}
