package org.moon.figura.model.rendering.texture;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalDouble;
import java.util.function.Function;

public enum RenderTypes {
    NONE(null),

    CUTOUT(RenderType::entityCutoutNoCull),
    CUTOUT_CULL(RenderType::entityCutout),

    TRANSLUCENT(RenderType::entityTranslucent),
    TRANSLUCENT_CULL(RenderType::entityTranslucentCull),

    EMISSIVE(RenderType::eyes, false),
    EMISSIVE_SOLID(resourceLocation -> RenderType.beaconBeam(resourceLocation, false), false),
    EYES(RenderType::eyes, false),

    END_PORTAL(t -> RenderType.endPortal(), true),
    END_GATEWAY(t -> RenderType.endGateway(), true),
    TEXTURED_PORTAL(FiguraRenderType.TEXTURED_PORTAL, false),

    GLINT(t -> RenderType.entityGlintDirect(), true),
    GLINT2(t -> RenderType.glintDirect(), true),

    LINES(t -> RenderType.lines(), true),
    LINES_STRIP(t -> RenderType.lineStrip(), true),
    SOLID(t -> FiguraRenderType.SOLID, true),

    BLURRY(FiguraRenderType.BLURRY);

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
            return func.apply(id);

        return id == null || func == null ? null : func.apply(id);
    }

    private static class FiguraRenderType extends RenderType {

        public FiguraRenderType(String name, VertexFormat vertexFormat, VertexFormat.Mode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
            super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
        }

        public static final RenderType SOLID = create(
                "figura_solid",
                DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.QUADS,
                256,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_LINES_SHADER)
                        .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setOutputState(ITEM_ENTITY_TARGET)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false)
        );

        public static final Function<ResourceLocation, RenderType> TEXTURED_PORTAL = Util.memoize(
                texture -> create(
                        "figura_textured_portal",
                        DefaultVertexFormat.POSITION,
                        VertexFormat.Mode.QUADS,
                        256,
                        false,
                        false,
                        CompositeState.builder()
                                .setShaderState(RENDERTYPE_END_GATEWAY_SHADER)
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
                        DefaultVertexFormat.NEW_ENTITY,
                        VertexFormat.Mode.QUADS,
                        256,
                        true,
                        true,
                        CompositeState.builder()
                                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                                .setTextureState(new RenderStateShard.TextureStateShard(texture, true, false))
                                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                .setCullState(NO_CULL)
                                .setLightmapState(LIGHTMAP)
                                .setOverlayState(OVERLAY)
                                .createCompositeState(true)
                )
        );
    }
}