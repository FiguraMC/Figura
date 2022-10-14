package org.moon.figura.model.rendering.texture;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public enum RenderTypes {
    NONE(null),

    CUTOUT(RenderType::entityCutoutNoCull),
    CUTOUT_CULL(RenderType::entityCutout),
    TRANSLUCENT(RenderType::entityTranslucent),
    TRANSLUCENT_CULL(RenderType::entityTranslucentCull),

    EMISSIVE(RenderType::eyes),
    EMISSIVE_SOLID(resourceLocation -> RenderType.beaconBeam(resourceLocation, false)),

    END_PORTAL(t -> RenderType.endPortal(), true),
    END_GATEWAY(t -> RenderType.endGateway(), true),
    GLINT(t ->  RenderType.entityGlintDirect(), true),
    GLINT2(t ->  RenderType.glintDirect(), true),
    LINES(t ->  RenderType.lines(), true),
    LINES_STRIP(t ->  RenderType.lineStrip(), true);

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
}