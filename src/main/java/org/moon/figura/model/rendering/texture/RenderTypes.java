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

    EMISSIVE(RenderType::eyes, false, true),
    EMISSIVE_SOLID(resourceLocation -> RenderType.beaconBeam(resourceLocation, false), false, true),
    EYES(RenderType::eyes, false, true),

    END_PORTAL(t -> RenderType.endPortal(), true, true),
    END_GATEWAY(t -> RenderType.endGateway(), true, true),
    GLINT(t -> RenderType.entityGlintDirect(), true),
    GLINT2(t -> RenderType.glintDirect(), true),
    LINES(t -> RenderType.lines(), true),
    LINES_STRIP(t -> RenderType.lineStrip(), true);

    private final Function<ResourceLocation, RenderType> func;
    private final boolean force;
    private final boolean offset;

    RenderTypes(Function<ResourceLocation, RenderType> func) {
        this(func, false, false);
    }

    RenderTypes(Function<ResourceLocation, RenderType> func, boolean force) {
        this(func, force, false);
    }

    RenderTypes(Function<ResourceLocation, RenderType> func, boolean force, boolean offset) {
        this.func = func;
        this.force = force;
        this.offset = offset;
    }

    public RenderType get(ResourceLocation id) {
        if (force)
            return func.apply(id);

        return id == null || func == null ? null : func.apply(id);
    }

    public boolean isOffset() {
        return offset;
    }
}