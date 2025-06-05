package org.figuramc.figura.gui;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.gui.widgets.permissions.PlayerPermPackElement;
import org.figuramc.figura.utils.ui.UIHelper;

public class FiguraPortraitRenderer extends PictureInPictureRenderer<FiguraPortraitRenderState> {

    private boolean renderSkin;
    public FiguraPortraitRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }


    @Override
    public Class<FiguraPortraitRenderState> getRenderStateClass() {
        return FiguraPortraitRenderState.class;
    }

    @Override
    protected void renderToTexture(FiguraPortraitRenderState portraitState, PoseStack poseStack) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);

        Avatar avatar = portraitState.avatar();
        if (avatar != null) {
            renderSkin = !avatar.renderHeadForPortrait(this.bufferSource, poseStack, LightTexture.FULL_BRIGHT, portraitState.modelScale(), portraitState.upsideDown());
        } else {
            renderSkin = true;
        }
    }


    @Override
    protected void blitTexture(FiguraPortraitRenderState pictureInPictureRenderState, GuiRenderState guiRenderState) {
        if (!renderSkin){
            super.blitTexture(pictureInPictureRenderState, guiRenderState);
            return;
        }

        if (pictureInPictureRenderState.fallbackSkin() != null) {
            ResourceLocation texture = pictureInPictureRenderState.fallbackSkin();
            // render skin
            UIHelper.enableBlend();
            blit(pictureInPictureRenderState, guiRenderState, RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, texture,  pictureInPictureRenderState.x0() + 4,  pictureInPictureRenderState.y0() + 4, 8f, 8f,32, 32, 8, 8, 64, 64);

            // hat
            GlStateManager._enableBlend();
            blit(pictureInPictureRenderState, guiRenderState, RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, texture, pictureInPictureRenderState.x0() + 4, pictureInPictureRenderState.y0() + 4, 40f, 8f,32, 32, 8, 8, 64, 64);
            GlStateManager._disableBlend();
        } else {
            blit(pictureInPictureRenderState, guiRenderState, pictureInPictureRenderState.x0() + 4, pictureInPictureRenderState.x1() + 4, 32, 32, PlayerPermPackElement.UNKNOWN);
        }
    }

    public void blit(
            FiguraPortraitRenderState renderState, GuiRenderState guiRenderState, RenderPipeline renderPipeline, ResourceLocation resourceLocation, int i, int j, int k, int l, float f, float g, float h, float m, int n
    ) {
        GpuTextureView gpuTextureView = Minecraft.getInstance().getTextureManager().getTexture(resourceLocation).getTextureView();
        guiRenderState
                .submitGuiElement(
                        new BlitRenderState(
                                renderPipeline, TextureSetup.singleTexture(gpuTextureView), renderState.pose(), i, j, k, l, f, g, h, m, n, renderState.scissorArea()
                        )
                );

    }

    public void blit(FiguraPortraitRenderState renderState, GuiRenderState guiRenderState, RenderPipeline renderPipeline, ResourceLocation resourceLocation, int i, int j, float f, float g, int k, int l, int m, int n, int o, int p) {
        this.blit(renderState, guiRenderState, renderPipeline, resourceLocation, i, j, f, g, k, l, m, n, o, p, -1);
    }

    public void blit(
            FiguraPortraitRenderState renderState, GuiRenderState guiRenderState, RenderPipeline renderPipeline, ResourceLocation resourceLocation, int i, int j, float f, float g, int k, int l, int m, int n, int o, int p, int q
    ) {
        this.blit(renderState, guiRenderState, renderPipeline, resourceLocation, i, i + k, j, j + l, (f + 0.0F) / o, (f + m) / o, (g + 0.0F) / p, (g + n) / p, q);
    }

    public void blit(FiguraPortraitRenderState renderState, GuiRenderState guiRenderState, int x, int y, int width, int height, ResourceLocation texture) {
        blit(renderState, guiRenderState, RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, texture, x, y, 0f, 0f, width, height, 1, 1, 1, 1);
    }

    @Override
    protected String getTextureLabel() {
        return "figura-portrait";
    }
}
