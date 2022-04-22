package org.moon.figura.model.rendering.texture;

import net.minecraft.client.renderer.RenderType;

public class FiguraTextureSet {

    public final String name;
    public final FiguraTexture mainTex, emissiveTex;
    public final RenderType mainType, emissiveType;

    public FiguraTextureSet(String name, byte[] mainData, byte[] emissiveData) {
        this.name = name;

        if (mainData != null) {
            mainTex = new FiguraTexture(mainData);
            mainType = RenderType.entityCutoutNoCull(mainTex.textureID);
        } else {
            mainTex = null;
            mainType = null;
        }

        if (emissiveData != null) {
            emissiveTex = new FiguraTexture(emissiveData);
            emissiveType = RenderType.eyes(emissiveTex.textureID);
        } else {
            emissiveTex = null;
            emissiveType = null;
        }
    }

    public void uploadIfNeeded() {
        if (mainTex != null)
            mainTex.registerAndUpload();
        if (emissiveTex != null)
            emissiveTex.registerAndUpload();
    }

}
