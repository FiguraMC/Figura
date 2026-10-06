package org.figuramc.figura.model.rendering.texture;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;

import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import org.figuramc.figura.utils.FiguraIdentifier;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum FiguraRenderTypes {
    NONE(null),

    CUTOUT(RenderTypes::entityCutoutNoCull),
    CUTOUT_CULL(RenderTypes::entityCutout),
    CUTOUT_EMISSIVE_SOLID(resourceLocation -> FiguraRenderType.CUTOUT_EMISSIVE_SOLID.apply(resourceLocation, true)),

    TRANSLUCENT(RenderTypes::entityTranslucent),
    TRANSLUCENT_CULL(RenderTypes::itemEntityTranslucentCull),

    EMISSIVE(RenderTypes::eyes),
    EMISSIVE_SOLID(resourceLocation -> RenderTypes.beaconBeam(resourceLocation, false)),
    EYES(RenderTypes::eyes),

    END_PORTAL(t -> RenderTypes.endPortal(), false),
    END_GATEWAY(t -> RenderTypes.endGateway(), false),
    TEXTURED_PORTAL(FiguraRenderType.TEXTURED_PORTAL),

    GLINT(t -> RenderTypes.entityGlint(), false, false),
    GLINT2(t -> RenderTypes.glint(), false, false),
    TEXTURED_GLINT(FiguraRenderType.TEXTURED_GLINT, true, false),

    LINES(t -> RenderTypes.lines(), false),
    LINES_STRIP(t -> RenderTypes.lines(), false),
    SOLID(t -> FiguraRenderType.SOLID, false),

    BLURRY(FiguraRenderType.BLURRY);

    private final Function<Identifier, RenderType> func;
    private final boolean texture, offset;

    FiguraRenderTypes(Function<Identifier, RenderType> func) {
        this(func, true);
    }

    FiguraRenderTypes(Function<Identifier, RenderType> func, boolean texture) {
        this(func, texture, true);
    }

    FiguraRenderTypes(Function<Identifier, RenderType> func, boolean texture, boolean offset) {
        this.func = func;
        this.texture = texture;
        this.offset = offset;
    }

    public boolean isOffset() {
        return offset;
    }

    public RenderType get(Identifier id) {
        if (!texture)
            return func.apply(id);

        return id == null || func == null ? null : func.apply(id);
    }

    public abstract static class FiguraRenderType extends RenderType {
        public FiguraRenderType(String name, RenderSetup setup) {
            super(name, setup);
        }

        public static final RenderType SOLID = create(
                "figura_solid",
                RenderSetup.builder(FiguraRenderPipelines.FIGURA_SOLID).bufferSize(256)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                        .setOutline(RenderSetup.OutlineProperty.NONE)
                        .createRenderSetup()
        );

        private static final BiFunction<Identifier, Boolean, RenderType> CUTOUT_EMISSIVE_SOLID = Util.memoize(
                (texture, affectsOutline) ->
                        create("figura_cutout_emissive_solid",
                                RenderSetup.builder(RenderPipelines.BEACON_BEAM_TRANSLUCENT)
                                        .bufferSize(256)
                                        .withTexture("Sampler0", texture)
                                        .affectsCrumbling()
                                        .sortOnUpload()
                                        .useOverlay()
                                        .setOutline(affectsOutline ? RenderSetup.OutlineProperty.AFFECTS_OUTLINE : RenderSetup.OutlineProperty.NONE)
                                        .createRenderSetup()
                        )
        );


        public static final Function<Identifier, RenderType> TEXTURED_PORTAL = Util.memoize(
                texture -> create(
                        "figura_textured_portal",
                        RenderSetup.builder(RenderPipelines.END_GATEWAY)
                                .bufferSize(256)
                                .withTexture("Sampler0", texture)
                                .withTexture("Sampler1", texture)
                                .setOutline(RenderSetup.OutlineProperty.NONE)
                                .createRenderSetup()
                )
        );

        public static final Function<Identifier, RenderType> BLURRY = Util.memoize(
                texture -> create(
                        "figura_blurry",
                        RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
                                .bufferSize(256)
                                .affectsCrumbling()
                                .sortOnUpload()
                                .useLightmap()
                                .useOverlay()
                                .withTexture("Sampler0", texture, () -> {
                                    GpuDevice device = RenderSystem.getDevice();
                                    AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
                                    // basically copy it the sampler the texture set to linear to blur it
                                    return device.createSampler(abstractTexture.getSampler().getAddressModeU(), abstractTexture.getSampler().getAddressModeV(),
                                            FilterMode.LINEAR, FilterMode.LINEAR, abstractTexture.getSampler().getMaxAnisotropy(), abstractTexture.getSampler().getMaxLod());
                                })
                                .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                                .createRenderSetup()
                )
        );

        public static final Function<Identifier, RenderType> TEXTURED_GLINT = Util.memoize(
                texture -> create(
                        "figura_textured_glint_direct",
                        RenderSetup.builder(RenderPipelines.GLINT)
                                .bufferSize(256)
                                .withTexture("Sampler0", texture)
                                .setTextureTransform(TextureTransform.ENTITY_GLINT_TEXTURING)
                                .createRenderSetup()
                )
        );
    }

    public static class FiguraRenderPipelines extends RenderPipelines {
        protected static RenderPipeline.Snippet FIGURA_SOLID_SNIPPET = RenderPipeline.builder(MATRICES_FOG_SNIPPET, GLOBALS_SNIPPET).withVertexShader("core/rendertype_lines").withFragmentShader("core/rendertype_lines").withColorWrite(true).withDepthWrite(true).withBlend(BlendFunction.TRANSLUCENT).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS).buildSnippet();

        public static RenderPipeline FIGURA_SOLID = register(RenderPipeline.builder(FIGURA_SOLID_SNIPPET).withLocation(new FiguraIdentifier("pipeline/solid")).build());
    }
}