package org.figuramc.figura.gui;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.gui.widgets.permissions.PlayerPermPackElement;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;

public class FiguraPortraitRenderer extends PictureInPictureRenderer<FiguraPortraitRenderState> {

    private final CachedOrthoProjectionMatrixBuffer avatarProjectionMatrixBuffer = new CachedOrthoProjectionMatrixBuffer(
            "Portrait-PIP - " + this.getClass().getSimpleName(), -1000.0F, 1000.0F, true
    );
    Map<Avatar, TextureEntry> avatarToTexture = new HashMap<>();
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
    public void prepare(FiguraPortraitRenderState pictureInPictureRenderState, GuiRenderState guiRenderState, int i) {
        int j = (pictureInPictureRenderState.x1() - pictureInPictureRenderState.x0()) * i;
        int k = (pictureInPictureRenderState.y1() - pictureInPictureRenderState.y0()) * i;
        if (pictureInPictureRenderState.avatar() != null) {
            prepareTexturesAndProjectionForAvatar(pictureInPictureRenderState.avatar(), j, k);
            TextureEntry textureEntry = avatarToTexture.get(pictureInPictureRenderState.avatar());
            RenderSystem.outputColorTextureOverride = textureEntry.textureView;
            RenderSystem.outputDepthTextureOverride = textureEntry.depthTextureView;
            PoseStack poseStack = new PoseStack();
            poseStack.translate(j / 2.0F, this.getTranslateY(k, i), 0.0F);
            float f = i * pictureInPictureRenderState.scale();
            poseStack.scale(f, f, -f);
            this.renderToTexture(pictureInPictureRenderState, poseStack);
            this.bufferSource.endBatch();
            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;
            this.blitTexture(pictureInPictureRenderState, guiRenderState);
        }
        else
            super.prepare(pictureInPictureRenderState, guiRenderState, i);
    }

    private void prepareTexturesAndProjectionForAvatar(Avatar avatar, int i, int j) {
        if (avatar == null)
            return;

        TextureEntry entry = avatarToTexture.computeIfAbsent(avatar, k -> new TextureEntry());

        boolean bl = entry.texture == null || entry.texture.getWidth(0) != i || entry.texture.getHeight(0) != j;
        if (entry.texture != null && bl) {
            entry.texture.close();
            entry.texture = null;
            entry.textureView.close();
            entry.textureView = null;
            entry.depthTexture.close();
            entry.depthTexture = null;
            entry.depthTextureView.close();
            entry.sampler.close();
            entry.sampler = null;
        }

        GpuDevice gpuDevice = RenderSystem.getDevice();
        if (entry.texture == null) {
            entry.texture = gpuDevice.createTexture(() -> "UI " + this.getTextureLabel() + " texture " + avatar.name, 12, TextureFormat.RGBA8, i, j, 1, 1);
            entry.textureView = gpuDevice.createTextureView(entry.texture);
            entry.depthTexture = gpuDevice.createTexture(() -> "UI " + this.getTextureLabel() + " depth texture " + avatar.name, 8, TextureFormat.DEPTH32, i, j, 1, 1);
            entry.depthTextureView = gpuDevice.createTextureView(entry.depthTexture);
            entry.sampler = gpuDevice.createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, 1, OptionalDouble.empty());
        }

        gpuDevice.createCommandEncoder().clearColorAndDepthTextures(entry.texture, 0, entry.depthTexture, 1.0);
        RenderSystem.setProjectionMatrix(this.avatarProjectionMatrixBuffer.getBuffer(i, j), ProjectionType.ORTHOGRAPHIC);
    }

    @Override
    protected void blitTexture(FiguraPortraitRenderState pictureInPictureRenderState, GuiRenderState guiRenderState) {
        if (!renderSkin){
            TextureEntry entry = avatarToTexture.get(pictureInPictureRenderState.avatar());

            guiRenderState.submitBlitToCurrentLayer(
                    new BlitRenderState(
                            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                            TextureSetup.singleTexture(entry.textureView, entry.sampler),
                            pictureInPictureRenderState.pose(),
                            pictureInPictureRenderState.x0(),
                            pictureInPictureRenderState.y0(),
                            pictureInPictureRenderState.x1(),
                            pictureInPictureRenderState.y1(),
                            0.0F,
                            1.0F,
                            1.0F,
                            0.0F,
                            -1,
                            pictureInPictureRenderState.scissorArea(),
                            null
                    )
            );
            return;
        }

        if (pictureInPictureRenderState.fallbackSkin() != null) {
            Identifier texture = pictureInPictureRenderState.fallbackSkin();
            // render skin
            UIHelper.enableBlend();
            GpuTextureView gpuTextureView = Minecraft.getInstance().getTextureManager().getTexture(texture).getTextureView();
            GpuSampler sampler = Minecraft.getInstance().getTextureManager().getTexture(texture).getSampler();

            guiRenderState.submitBlitToCurrentLayer(
                    new BlitRenderState(
                            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                            TextureSetup.singleTexture(gpuTextureView, sampler),
                            pictureInPictureRenderState.pose(),
                            pictureInPictureRenderState.x0(),
                            pictureInPictureRenderState.y0(),
                            pictureInPictureRenderState.x1(),
                            pictureInPictureRenderState.y1(),
                            8/64F,
                            16/64.0F,
                            8/64F,
                            16/64.0F,
                            -1,
                            pictureInPictureRenderState.scissorArea(),
                            null
                    )
            );

            // hat
            GlStateManager._enableBlend();
            guiRenderState.submitBlitToCurrentLayer(
                    new BlitRenderState(
                            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                            TextureSetup.singleTexture(gpuTextureView, sampler),
                            pictureInPictureRenderState.pose(),
                            pictureInPictureRenderState.x0(),
                            pictureInPictureRenderState.y0(),
                            pictureInPictureRenderState.x1(),
                            pictureInPictureRenderState.y1(),
                            40/64F,
                            48/64.0F,
                            8/64F,
                            16/64.0F,
                            -1,
                            pictureInPictureRenderState.scissorArea(),
                            null
                    )
            );
            GlStateManager._disableBlend();
        } else {
            GpuTextureView gpuTextureView = Minecraft.getInstance().getTextureManager().getTexture(PlayerPermPackElement.UNKNOWN).getTextureView();
            GpuSampler sampler = Minecraft.getInstance().getTextureManager().getTexture(PlayerPermPackElement.UNKNOWN).getSampler();
            guiRenderState.submitBlitToCurrentLayer(
                    new BlitRenderState(
                            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                            TextureSetup.singleTexture(gpuTextureView, sampler),
                            pictureInPictureRenderState.pose(),
                            pictureInPictureRenderState.x0(),
                            pictureInPictureRenderState.y0(),
                            pictureInPictureRenderState.x1(),
                            pictureInPictureRenderState.y1(),
                            0,
                            1,
                            0,
                            1,
                            -1,
                            pictureInPictureRenderState.scissorArea(),
                            null
                    )
            );
        }
    }

    @Override
    protected String getTextureLabel() {
        return "figura-portrait";
    }

    @Override
    public void close() {
        super.close();
        for (Map.Entry<Avatar, TextureEntry> entry : avatarToTexture.entrySet()) {
            entry.getValue().texture.close();
            entry.getValue().textureView.close();
            entry.getValue().depthTexture.close();
            entry.getValue().depthTextureView.close();
        }
        avatarProjectionMatrixBuffer.close();
    }

    private static final class TextureEntry {
        private GpuTexture texture;
        private GpuTextureView textureView;
        private GpuTexture depthTexture;
        private GpuTextureView depthTextureView;
        private GpuSampler sampler;

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (TextureEntry) obj;
            return Objects.equals(this.texture, that.texture) &&
                    Objects.equals(this.textureView, that.textureView) &&
                    Objects.equals(this.depthTexture, that.depthTexture) &&
                    Objects.equals(this.depthTextureView, that.depthTextureView);
        }

        @Override
        public int hashCode() {
            return Objects.hash(texture, textureView, depthTexture, depthTextureView);
        }

        @Override
        public String toString() {
            return "TextureEntry[" +
                    "texture=" + texture + ", " +
                    "textureView=" + textureView + ", " +
                    "depthTexture=" + depthTexture + ", " +
                    "depthTextureView=" + depthTextureView + ']';
        }


    }

}
