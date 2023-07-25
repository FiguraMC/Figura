package org.figuramc.figura.utils.ui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class CustomFramebuffer {

    private int fbo = -1;
    private int colorAttachment = -1;
    private int depthStencilAttachment = -1;

    private int width, height;

    /**
     * Attempts to adjust the framebuffer to match a given size
     *
     * @param nWidth  The new width
     * @param nHeight The new height
     */
    public void setSize(int nWidth, int nHeight) {

        // Minimized window, we don't even need the framebuffer, so...
        if (nWidth == 0 || nHeight == 0)
            return;

        if (nWidth != width || nHeight != height) {
            width = nWidth;
            height = nHeight;

            if (this.fbo != -1) {
                GlStateManager._glDeleteFramebuffers(this.fbo);
                fbo = -1;
            }
            if (this.colorAttachment != -1) {
                TextureUtil.releaseTextureId(this.colorAttachment);
                this.colorAttachment = -1;
            }
            if (this.depthStencilAttachment != -1) {
                TextureUtil.releaseTextureId(this.depthStencilAttachment);
                this.depthStencilAttachment = -1;
            }

            this.fbo = GlStateManager.glGenFramebuffers();
            this.colorAttachment = TextureUtil.generateTextureId();
            this.depthStencilAttachment = TextureUtil.generateTextureId();

            GlStateManager._bindTexture(this.depthStencilAttachment);
            GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH24_STENCIL8, width, height, 0, GL30.GL_DEPTH_STENCIL, GL30.GL_UNSIGNED_INT_24_8, null);

            GlStateManager._bindTexture(this.colorAttachment);
            GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);

            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.fbo);
            GlStateManager._bindTexture(this.colorAttachment);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, this.colorAttachment, 0);

            GlStateManager._bindTexture(this.depthStencilAttachment);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D, this.depthStencilAttachment, 0);
        }
    }

    public int getFbo() {
        return fbo;
    }

    public void drawToScreen(int viewWidth, int viewHeight) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        GlStateManager._viewport(0, 0, viewWidth, viewHeight);

        Minecraft minecraftClient = Minecraft.getInstance();
        ShaderInstance shader = minecraftClient.gameRenderer.blitShader;
        shader.setSampler("DiffuseSampler", colorAttachment);
        // shader.addSampler("DiffuseSampler", MinecraftClient.getInstance().getFramebuffer().getColorAttachment());
        // shader.addSampler("DiffuseSampler", MinecraftClient.getInstance().getTextureManager().getTexture(ClickableWidget.WIDGETS_TEXTURE).getGlId());
        Matrix4f matrix4f = new Matrix4f().setOrtho(0f, (float) viewWidth, (float) (viewHeight), 0f, 1000f, 3000f);
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
        if (shader.MODEL_VIEW_MATRIX != null) {
            shader.MODEL_VIEW_MATRIX.set(new Matrix4f().translation(0f, 0f, -2000f));
        }

        if (shader.PROJECTION_MATRIX != null) {
            shader.PROJECTION_MATRIX.set(matrix4f);
        }

        shader.apply();
        Tesselator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(0d, viewHeight, 0d).uv(0f, 0f).color(0xFF, 0xFF, 0xFF, 0xFF).endVertex();
        bufferBuilder.vertex(viewWidth, viewHeight, 0d).uv(1f, 0f).color(0xFF, 0xFF, 0xFF, 0xFF).endVertex();
        bufferBuilder.vertex(viewWidth, 0d, 0d).uv(1f, 1f).color(0xFF, 0xFF, 0xFF, 0xFF).endVertex();
        bufferBuilder.vertex(0d, 0d, 0d).uv(0f, 1f).color(0xFF, 0xFF, 0xFF, 0xFF).endVertex();
        BufferUploader.draw(bufferBuilder.end());
        shader.clear();
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
    }
}
