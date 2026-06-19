package net.minecraft.client.renderer.rendertype;

public final class FiguraRenderTypeFactory {
    private FiguraRenderTypeFactory() {
    }

    public static RenderType create(String name, RenderSetup setup) {
        return RenderType.create(name, setup);
    }
}
