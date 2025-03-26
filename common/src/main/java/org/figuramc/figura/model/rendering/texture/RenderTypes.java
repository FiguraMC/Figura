package org.figuramc.figura.model.rendering.texture;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import org.figuramc.figura.utils.FiguraIdentifier;

import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum RenderTypes {
    NONE(null),

    CUTOUT(RenderType::entityCutoutNoCull),
    CUTOUT_CULL(RenderType::entityCutout),
    CUTOUT_EMISSIVE_SOLID(resourceLocation -> FiguraRenderType.CUTOUT_EMISSIVE_SOLID.apply(resourceLocation, true)),

    TRANSLUCENT(RenderType::entityTranslucent),
    TRANSLUCENT_CULL(RenderType::itemEntityTranslucentCull),

    EMISSIVE(RenderType::eyes),
    EMISSIVE_SOLID(resourceLocation -> RenderType.beaconBeam(resourceLocation, false)),
    EYES(RenderType::eyes),

    END_PORTAL(t -> RenderType.endPortal(), false),
    END_GATEWAY(t -> RenderType.endGateway(), false),
    TEXTURED_PORTAL(FiguraRenderType.TEXTURED_PORTAL),

    GLINT(t -> RenderType.entityGlint(), false, false),
    GLINT2(t -> RenderType.glint(), false, false),
    TEXTURED_GLINT(FiguraRenderType.TEXTURED_GLINT, true, false),

    LINES(t -> RenderType.lines(), false),
    LINES_STRIP(t -> RenderType.lineStrip(), false),
    SOLID(t -> FiguraRenderType.SOLID, false),

    BLURRY(FiguraRenderType.BLURRY);

    private final Function<ResourceLocation, RenderType> func;
    private final boolean texture, offset;

    RenderTypes(Function<ResourceLocation, RenderType> func) {
        this(func, true);
    }

    RenderTypes(Function<ResourceLocation, RenderType> func, boolean texture) {
        this(func, texture, true);
    }

    RenderTypes(Function<ResourceLocation, RenderType> func, boolean texture, boolean offset) {
        this.func = func;
        this.texture = texture;
        this.offset = offset;
    }

    public boolean isOffset() {
        return offset;
    }

    public RenderType get(ResourceLocation id) {
        if (!texture)
            return func.apply(id);

        return id == null || func == null ? null : func.apply(id);
    }

    private abstract static class FiguraRenderType extends RenderType {

        public FiguraRenderType(String name, int bufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
            super(name, bufferSize, hasCrumbling, translucent, startAction, endAction);
        }

        public static final RenderType SOLID = create(
                "figura_solid",
                256,
                FiguraRenderPipelines.FIGURA_SOLID,
                RenderType.CompositeState.builder()
                        .setLineState(new LineStateShard(OptionalDouble.empty()))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .createCompositeState(false)
        );

        private static final BiFunction<ResourceLocation, Boolean, RenderType> CUTOUT_EMISSIVE_SOLID = Util.memoize(
                (texture, affectsOutline) ->
                        create("figura_cutout_emissive_solid", 256, true, true, RenderPipelines.BEACON_BEAM_TRANSLUCENT,
                                CompositeState.builder()
                                        .setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
                                        .setOverlayState(OVERLAY)
                                        .createCompositeState(affectsOutline)));


        public static final Function<ResourceLocation, RenderType> TEXTURED_PORTAL = Util.memoize(
                texture -> create(
                        "figura_textured_portal",
                        256,
                        false,
                        false,
                        RenderPipelines.END_GATEWAY,
                        CompositeState.builder()
                                .setTextureState(
                                        MultiTextureStateShard.builder()
                                                .add(texture, false, false)
                                                .add(texture, false, false)
                                                .build()
                                )
                                .createCompositeState(false)
                )
        );

        public static final Function<ResourceLocation, RenderType> BLURRY = Util.memoize(
                texture -> create(
                        "figura_blurry",
                        256,
                        true,
                        true,
                        RenderPipelines.ENTITY_TRANSLUCENT,
                        CompositeState.builder()
                                .setTextureState(new TextureStateShard(texture, TriState.TRUE, false))
                                .setLightmapState(LIGHTMAP)
                                .setOverlayState(OVERLAY)
                                .createCompositeState(true)
                )
        );

        public static final Function<ResourceLocation, RenderType> TEXTURED_GLINT = Util.memoize(
                texture -> create(
                        "figura_textured_glint_direct",
                        256,
                        false,
                        false,
                        RenderPipelines.GLINT,
                        RenderType.CompositeState.builder()
                                .setTextureState(new TextureStateShard(texture, TriState.FALSE, false))
                                .setTexturingState(ENTITY_GLINT_TEXTURING)
                                .createCompositeState(false)
                )
        );
    }

    public static class FiguraRenderPipelines extends RenderPipelines {
        protected static RenderPipeline.Snippet FIGURA_SOLID_SNIPPET = RenderPipeline.builder(MATRICES_COLOR_FOG_SNIPPET).withVertexShader("core/rendertype_lines").withFragmentShader("core/rendertype_lines").withUniform("LineWidth",UniformType.FLOAT).withUniform("ScreenSize",UniformType.VEC2).withColorWrite(true).withDepthWrite(true).withBlend(BlendFunction.TRANSLUCENT).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS).buildSnippet();

        public static RenderPipeline FIGURA_SOLID = register(RenderPipeline.builder(FIGURA_SOLID_SNIPPET).withLocation(new FiguraIdentifier("pipeline/solid")).build());

    }
}