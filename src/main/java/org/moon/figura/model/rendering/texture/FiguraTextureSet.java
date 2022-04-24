package org.moon.figura.model.rendering.texture;

import net.minecraft.client.renderer.RenderType;

import java.util.HashMap;
import java.util.Map;

public class FiguraTextureSet {

    public final String name;
    public final FiguraTexture mainTex, emissiveTex;
    private final Map<String, RenderType> renderTypes = new HashMap<>();

    public FiguraTextureSet(String name, byte[] mainData, byte[] emissiveData) {
        this.name = name;

        if (mainData != null) {
            mainTex = new FiguraTexture(mainData);
            renderTypes.put("CUTOUT_NO_CULL", RenderType.entityCutoutNoCull(mainTex.textureID));
        } else {
            mainTex = null;
        }

        if (emissiveData != null) {
            emissiveTex = new FiguraTexture(emissiveData);
            renderTypes.put("EMISSIVE", RenderType.eyes(emissiveTex.textureID));
        } else {
            emissiveTex = null;
            renderTypes.put("EMISSIVE", null);
        }
    }

    public void uploadIfNeeded() {
        if (mainTex != null)
            mainTex.registerAndUpload();
        if (emissiveTex != null)
            emissiveTex.registerAndUpload();
    }

    public RenderType getRenderType(String name) {
        if (name == null)
            return null;
        if (!renderTypes.containsKey(name))
            if (mainTex != null)
                switch (name) {
                    case "CUTOUT" -> renderTypes.put("CUTOUT", RenderType.entityCutout(mainTex.textureID));
                    case "END_PORTAL" -> renderTypes.put("END_PORTAL", RenderType.endPortal());
                    case "GLINT" -> renderTypes.put("GLINT", RenderType.glintDirect());
                    case "GLINT2" -> renderTypes.put("GLINT2", RenderType.entityGlintDirect());
                    default -> throw new IllegalArgumentException("Invalid render type name: \"" + name + "\".");
                }
        return renderTypes.get(name);
    }

}
