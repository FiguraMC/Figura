package org.moon.figura.utils.ui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.screens.AbstractPanelScreen;
import org.moon.figura.gui.widgets.AbstractContainerElement;
import org.moon.figura.gui.widgets.ContextMenu;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.model.rendering.texture.EntityRenderMode;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.TextUtils;

import java.util.List;

public class UIHelper extends GuiComponent {

    // -- Variables -- //

    public static final ResourceLocation OUTLINE = new FiguraIdentifier("textures/gui/outline.png");
    public static final ResourceLocation TOOLTIP = new FiguraIdentifier("textures/gui/tooltip.png");
    public static final ResourceLocation UI_FONT = new FiguraIdentifier("ui");

    //Used for GUI rendering
    private static final CustomFramebuffer FIGURA_FRAMEBUFFER = new CustomFramebuffer();
    private static int previousFBO = -1;
    public static boolean paperdoll = false;
    public static float dollScale = 1f;
    public static FiguraVec4 scissors = FiguraVec4.of();

    // -- Functions -- //

    public static void useFiguraGuiFramebuffer() {
        previousFBO = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);

        int width = Minecraft.getInstance().getWindow().getWidth();
        int height = Minecraft.getInstance().getWindow().getHeight();
        FIGURA_FRAMEBUFFER.setSize(width, height);

        //Enable stencil buffer during this phase of rendering
        GL30.glEnable(GL30.GL_STENCIL_TEST);
        GlStateManager._stencilMask(0xFF);
        //Bind custom GUI framebuffer to be used for rendering
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, FIGURA_FRAMEBUFFER.getFbo());

        //Clear GUI framebuffer
        GlStateManager._clearStencil(0);
        GlStateManager._clearColor(0f, 0f, 0f, 1f);
        GlStateManager._clearDepth(1);
        GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL30.GL_STENCIL_BUFFER_BIT, false);

        Matrix4f mf = RenderSystem.getProjectionMatrix();
        Minecraft.getInstance().getMainRenderTarget().blitToScreen(width, height, false);
        RenderSystem.setProjectionMatrix(mf);
    }

    public static void useVanillaFramebuffer() {
        //Reset state before we go back to normal rendering
        GlStateManager._enableDepthTest();
        //Set a sensible default for stencil buffer operations
        GlStateManager._stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
        GL30.glDisable(GL30.GL_STENCIL_TEST);

        //Bind vanilla framebuffer again
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousFBO);

        RenderSystem.disableBlend();
        //Draw GUI framebuffer -> vanilla framebuffer
        int windowWidth = Minecraft.getInstance().getWindow().getWidth();
        int windowHeight = Minecraft.getInstance().getWindow().getHeight();

        Matrix4f mf = RenderSystem.getProjectionMatrix();
        FIGURA_FRAMEBUFFER.drawToScreen(windowWidth, windowHeight);
        RenderSystem.setProjectionMatrix(mf);
        RenderSystem.enableBlend();
    }

    public static void drawEntity(float x, float y, float scale, float pitch, float yaw, LivingEntity entity, PoseStack stack, EntityRenderMode renderMode) {
        //backup entity variables
        float entityPitch = entity.getXRot();
        float entityYaw = entity.getYRot();
        float bodyYaw = entity.yBodyRot;
        float headYaw = entity.yHeadRot;
        boolean invisible = entity.isInvisible();

        entity.setInvisible(false);

        //vehicle
        LivingEntity vehicle = null;
        float vBodyYaw = 0f;
        if (entity.getVehicle() instanceof LivingEntity l) {
            vehicle = l;
            vBodyYaw = l.yBodyRot;
        }

        //apply matrix transformers
        stack.pushPose();
        stack.translate(x, y, renderMode == EntityRenderMode.MINECRAFT_GUI ? 200d : 0d);
        stack.scale(scale, scale, scale);
        stack.last().pose().multiply(Matrix4f.createScaleMatrix(1f, 1f, -1f)); //Scale ONLY THE POSITIONS! Inverted normals don't work for whatever reason

        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180f);

        double finalY;
        Quaternion quaternion2;
        switch (renderMode) {
            case PAPERDOLL -> {
                //stack rotations
                quaternion2 = Vector3f.XP.rotationDegrees(pitch);
                Quaternion quaternion3 = Vector3f.YP.rotationDegrees(yaw + 180);
                quaternion3.mul(quaternion2);
                quaternion.mul(quaternion3);
                stack.mulPose(quaternion);
                quaternion3.conj();
                quaternion2 = quaternion3;

                //offset
                if (entity.isFallFlying())
                    stack.translate(Mth.triangleWave((float) Math.toRadians(270), Mth.TWO_PI), 0d, 0d);

                if (entity.isAutoSpinAttack() || entity.isVisuallySwimming() || entity.isFallFlying()) {
                    stack.translate(0d, 1d, 0d);
                    entity.setXRot(0f);
                }

                //rotations
                entity.yBodyRot = 0;
                entity.yHeadRot = headYaw - bodyYaw;

                if (vehicle != null)
                    vehicle.yBodyRot = vBodyYaw - bodyYaw;

                //lightning
                Lighting.setupForEntityInInventory();

                finalY = -1d;
            }
            case FIGURA_GUI -> {
                quaternion2 = Vector3f.XP.rotationDegrees(pitch);
                quaternion.mul(quaternion2);
                stack.mulPose(quaternion);
                quaternion2.conj();

                //rotations
                float rot = 180f - yaw;
                entity.setXRot(0f);
                entity.setYRot(rot);
                entity.yBodyRot = rot;
                entity.yHeadRot = rot;

                if (vehicle != null)
                    vehicle.yBodyRot = rot;

                //set up lighting
                Lighting.setupForFlatItems();
                RenderSystem.setShaderLights(Util.make(new Vector3f(-0.2f, -1f, -1f), Vector3f::normalize), Util.make(new Vector3f(-0.2f, 0.4f, -0.3f), Vector3f::normalize));

                yaw = 0f;
                finalY = -1d;
            }
            default -> {
                float angle = (float) Math.atan(pitch / 40f);
                float angle2 = (float) (Math.atan(yaw / 40f) * 20f);

                quaternion2 = Vector3f.XP.rotationDegrees(angle2);
                quaternion.mul(quaternion2);
                stack.mulPose(quaternion);
                quaternion2.conj();

                //rotations
                entity.setXRot(-angle2);
                entity.setYRot(180f + angle * 40f);
                entity.yBodyRot = 180f + angle * 20f;
                entity.yHeadRot = entity.getYRot();

                //lightning
                Lighting.setupForEntityInInventory();

                yaw = 0f;
                finalY = 0d;
            }
        }

        //setup entity renderer
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        boolean renderHitboxes = dispatcher.shouldRenderHitBoxes();
        dispatcher.setRenderHitBoxes(false);
        dispatcher.setRenderShadow(false);
        dispatcher.overrideCameraOrientation(quaternion2);
        MultiBufferSource.BufferSource immediate = minecraft.renderBuffers().bufferSource();

        //render
        UIHelper.paperdoll = true;
        UIHelper.dollScale = scale;

        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar != null) avatar.renderMode = renderMode;

        float finalYaw = yaw;
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0d, finalY, 0d, finalYaw, 1f, stack, immediate, LightTexture.FULL_BRIGHT));
        immediate.endBatch();

        UIHelper.paperdoll = false;

        //restore entity rendering data
        dispatcher.setRenderHitBoxes(renderHitboxes);
        dispatcher.setRenderShadow(true);

        //restore entity data
        entity.setXRot(entityPitch);
        entity.setYRot(entityYaw);
        entity.yBodyRot = bodyYaw;
        entity.yHeadRot = headYaw;
        entity.setInvisible(invisible);

        //vehicle
        if (vehicle != null)
            vehicle.yBodyRot = vBodyYaw;

        //pop matrix
        stack.popPose();
        Lighting.setupFor3DItems();
    }

    public static void setupTexture(ResourceLocation texture) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void renderTexture(PoseStack stack, int x, int y, int width, int height, ResourceLocation texture) {
        setupTexture(texture);
        blit(stack, x, y, width, height, 0f, 0f, 1, 1, 1, 1);
    }

    public static void renderAnimatedBackground(ResourceLocation texture, double x, double y, float width, float height, float textureWidth, float textureHeight, double speed, float delta) {
        if (speed != 0) {
            double d = (FiguraMod.ticks + delta) / speed;
            x -= d % textureWidth;
            y -= d % textureHeight;
        }

        width += textureWidth;
        height += textureHeight;

        if (speed < 0) {
            x -= textureWidth;
            y -= textureHeight;
        }

        renderBackgroundTexture(texture, x, y, width, height, textureWidth, textureHeight);
    }

    public static void renderBackgroundTexture(ResourceLocation texture, double x, double y, float width, float height, float textureWidth, float textureHeight) {
        setupTexture(texture);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferBuilder.vertex(x, y + height, 0f).uv(0f, height / textureHeight).endVertex();
        bufferBuilder.vertex(x + width, y + height, 0f).uv(width / textureWidth, height / textureHeight).endVertex();
        bufferBuilder.vertex(x + width, y, 0f).uv(width / textureWidth, 0f).endVertex();
        bufferBuilder.vertex(x, y, 0f).uv(0f, 0f).endVertex();

        tessellator.end();
    }

    public static void fillRounded(PoseStack stack, int x, int y, int width, int height, int color) {
        fill(stack, x + 1, y, x + width - 1, y + 1, color);
        fill(stack, x, y + 1, x + width, y + height - 1, color);
        fill(stack, x + 1, y + height - 1, x + width - 1, y + height, color);
    }

    public static void fillOutline(PoseStack stack, int x, int y, int width, int height, int color) {
        fill(stack, x + 1, y, x + width - 1, y + 1, color);
        fill(stack, x, y + 1, x + 1, y + height - 1, color);
        fill(stack, x + width - 1, y + 1, x + width, y + height - 1, color);
        fill(stack, x + 1, y + height - 1, x + width - 1, y + height, color);
    }

    public static void renderSliced(PoseStack stack, int x, int y, int width, int height, ResourceLocation texture) {
        renderSliced(stack, x, y, width, height, 0f, 0f, 15, 15, 15, 15, texture);
    }

    public static void renderSliced(PoseStack stack, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, ResourceLocation texture) {
        setupTexture(texture);

        Matrix4f pose = stack.last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float rWidthThird = regionWidth / 3f;
        float rHeightThird = regionHeight / 3f;

        //top left
        sliceVertex(pose, buffer, x, y, rWidthThird, rHeightThird, u, v, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //top middle
        sliceVertex(pose, buffer, x + rWidthThird, y, width - rWidthThird * 2, rHeightThird, u + rWidthThird, v, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //top right
        sliceVertex(pose, buffer, x + width - rWidthThird, y, rWidthThird, rHeightThird, u + rWidthThird * 2, v, rWidthThird, rHeightThird, textureWidth, textureHeight);

        //middle left
        sliceVertex(pose, buffer, x, y + rHeightThird, rWidthThird, height - rHeightThird * 2, u, v + rHeightThird, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //middle middle
        sliceVertex(pose, buffer, x + rWidthThird, y + rHeightThird, width - rWidthThird * 2, height - rHeightThird * 2, u + rWidthThird, v + rHeightThird, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //middle right
        sliceVertex(pose, buffer, x + width - rWidthThird, y + rHeightThird, rWidthThird, height - rHeightThird * 2, u + rWidthThird * 2, v + rHeightThird, rWidthThird, rHeightThird, textureWidth, textureHeight);

        //bottom left
        sliceVertex(pose, buffer, x, y + height - rHeightThird, rWidthThird, rHeightThird, u, v + rHeightThird * 2, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //bottom middle
        sliceVertex(pose, buffer, x + rWidthThird, y + height - rHeightThird, width - rWidthThird * 2, rHeightThird, u + rWidthThird, v + rHeightThird * 2, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //bottom right
        sliceVertex(pose, buffer, x + width - rWidthThird, y + height - rHeightThird, rWidthThird, rHeightThird, u + rWidthThird * 2, v + rHeightThird * 2, rWidthThird, rHeightThird, textureWidth, textureHeight);

        tessellator.end();
    }

    private static void sliceVertex(Matrix4f matrix, BufferBuilder bufferBuilder, float x, float y, float width, float height, float u, float v, float regionWidth, float regionHeight, int textureWidth, int textureHeight) {
        float x1 = x + width;
        float y1 = y + height;

        float u0 = u / textureWidth;
        float v0 = v / textureHeight;
        float u1 = (u + regionWidth) / textureWidth;
        float v1 = (v + regionHeight) / textureHeight;

        bufferBuilder.vertex(matrix, x, y1, 0f).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrix, x1, y1, 0f).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrix, x1, y, 0f).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrix, x, y, 0f).uv(u0, v0).endVertex();
    }

    public static void setupScissor(int x, int y, int width, int height) {
        scissors.set(x, y, width, height);

        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        int screenY = Minecraft.getInstance().getWindow().getHeight();

        int scaledWidth = (int) Math.max(width * scale, 0);
        int scaledHeight = (int) Math.max(height * scale, 0);
        RenderSystem.enableScissor((int) (x * scale), (int) (screenY - y * scale - scaledHeight), scaledWidth, scaledHeight);
    }

    public static void highlight(PoseStack stack, Object component, Component text) {
        //object
        int x, y, width, height;
        if (component instanceof AbstractWidget w) {
            x = w.x; y = w.y;
            width = w.getWidth();
            height = w.getHeight();
        } else if (component instanceof AbstractContainerElement c) {
            x = c.x; y = c.y;
            width = c.width;
            height = c.height;
        } else {
            return;
        }

        //screen
        int screenW, screenH;
        if (Minecraft.getInstance().screen instanceof AbstractPanelScreen panel) {
            screenW = panel.width;
            screenH = panel.height;

            if (text != null)
                panel.tooltip = text;
        } else {
            return;
        }

        //draw

        //left
        fill(stack, 0, 0, x, y + height, 0xBB000000);
        //right
        fill(stack, x + width, y, screenW, screenH, 0xBB000000);
        //up
        fill(stack, x, 0, screenW, y, 0xBB000000);
        //down
        fill(stack, 0, y + height, x + width, screenH, 0xBB000000);

        //outline
        fillOutline(stack, Math.max(x - 1, 0), Math.max(y - 1, 0), Math.min(width + 2, screenW), Math.min(height + 2, screenH), 0xFFFFFFFF);
    }

    //widget.isMouseOver() returns false if the widget is disabled or invisible
    public static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY) {
        return isMouseOver(x, y, width, height, mouseX, mouseY, false);
    }

    public static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY, boolean force) {
        ContextMenu context = force ? null : getContext();
        return (context == null || !context.isVisible()) && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static void renderOutlineText(PoseStack stack, Font textRenderer, Component text, int x, int y, int color, int outline) {
        Component outlineText = TextUtils.replaceStyle(TextUtils.replaceInText(text, "§.", ""), Style.EMPTY.withColor(outline));
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                textRenderer.draw(stack, outlineText, x + i, y + j, outline);
            }
        }

        stack.pushPose();
        stack.translate(0f, 0f, 0.001f);
        textRenderer.draw(stack, text, x, y, color);
        stack.popPose();
    }

    public static void renderTooltip(PoseStack stack, Component tooltip, int mouseX, int mouseY, boolean background) {
        Minecraft minecraft = Minecraft.getInstance();

        //window
        double screenX = minecraft.getWindow().getGuiScaledWidth();
        double screenY = minecraft.getWindow().getGuiScaledHeight();

        //prepare text
        Font font = minecraft.font;
        List<FormattedCharSequence> text = TextUtils.warpTooltip(tooltip, font, mouseX, (int) screenX);
        int height = font.lineHeight * text.size();

        //calculate pos
        int x = mouseX + 12;
        int y = (int) Math.min(Math.max(mouseY - 12, 0), screenY - height);

        int width = TextUtils.getWidth(text, font);
        if (x + width > screenX)
            x = Math.max(x - 28 - width, 0);

        //render
        stack.pushPose();
        stack.translate(0d, 0d, 400d);

        if (background)
            renderSliced(stack, x - 4, y - 4, width + 8, height + 8, TOOLTIP);

        for (int i = 0; i < text.size(); i++) {
            FormattedCharSequence charSequence = text.get(i);
            font.drawShadow(stack, charSequence, x, y + font.lineHeight * i, 0xFFFFFF);
        }

        stack.popPose();
    }

    public static void setContext(ContextMenu context) {
        if (Minecraft.getInstance().screen instanceof AbstractPanelScreen panelScreen)
            panelScreen.contextMenu = context;
    }

    public static ContextMenu getContext() {
        if (Minecraft.getInstance().screen instanceof AbstractPanelScreen panelScreen)
            return panelScreen.contextMenu;
        return null;
    }

    public static void setTooltip(Component text) {
        if (Minecraft.getInstance().screen instanceof AbstractPanelScreen panelScreen)
            panelScreen.tooltip = text;
    }

    public static void setTooltip(Style style) {
        if (style == null || style.getHoverEvent() == null)
            return;

        Component text = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);
        if (text != null)
            setTooltip(text);
    }
}
