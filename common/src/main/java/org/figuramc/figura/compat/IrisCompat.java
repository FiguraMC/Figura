package org.figuramc.figura.compat;

import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import org.figuramc.figura.model.rendering.texture.RenderTypes;

public class IrisCompat {
    public static void assignPipelinesToIrisPrograms() {
        IrisApi.getInstance().assignPipeline(RenderTypes.FiguraRenderPipelines.FIGURA_SOLID, IrisProgram.ENTITIES);
    }
}
