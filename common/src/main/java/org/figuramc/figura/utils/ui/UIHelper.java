package org.figuramc.figura.utils.ui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.screens.AbstractPanelScreen;
import org.figuramc.figura.gui.screens.FiguraConfirmScreen;
import org.figuramc.figura.gui.widgets.ContextMenu;
import org.figuramc.figura.gui.widgets.FiguraWidget;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.RenderUtils;
import org.figuramc.figura.utils.TextUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.List;
import java.util.function.Consumer;

public final class UIHelper {

    private UIHelper() {}

    // -- Variables -- // 

    public static final ResourceLocation OUTLINE_FILL = new FiguraIdentifier("textures/gui/outline_fill.png");
    public static final ResourceLocation OUTLINE = new FiguraIdentifier("textures/gui/outline.png");
    public static final ResourceLocation TOOLTIP = new FiguraIdentifier("textures/gui/tooltip.png");
    public static final ResourceLocation UI_FONT = new FiguraIdentifier("ui");
    public static final ResourceLocation SPECIAL_FONT = new FiguraIdentifier("special");

    public static final Component UP_ARROW = Component.literal("^").withStyle(Style.EMPTY.withFont(UI_FONT));
    public static final Component DOWN_ARROW = Component.literal("V").withStyle(Style.EMPTY.withFont(UI_FONT));

    // Used for GUI rendering
    private static final CustomFramebuffer FIGURA_FRAMEBUFFER = new CustomFramebuffer();
    private static int previousFBO = -1;
    public static boolean paperdoll = false;
    public static float fireRot = 0f;
    public static float dollScale = 1f;

    // -- Functions -- // 

    public static void useFiguraGuiFramebuffer() {
        previousFBO = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);

        int width = Minecraft.getInstance().getWindow().getWidth();
        int height = Minecraft.getInstance().getWindow().getHeight();
        FIGURA_FRAMEBUFFER.setSize(width, height);

        // Enable stencil buffer during this phase of rendering
        GL30.glEnable(GL30.GL_STENCIL_TEST);
        GlStateManager._stencilMask(0xFF);
        // Bind custom GUI framebuffer to be used for rendering
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, FIGURA_FRAMEBUFFER.getFbo());

        // Clear GUI framebuffer
        GlStateManager._clearStencil(0);
        GlStateManager._clearColor(0f, 0f, 0f, 1f);
        GlStateManager._clearDepth(1);
        GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL30.GL_STENCIL_BUFFER_BIT, false);

        Matrix4f mf = RenderSystem.getProjectionMatrix();
        Minecraft.getInstance().getMainRenderTarget().blitToScreen(width, height, false);
        RenderSystem.setProjectionMatrix(mf, VertexSorting.ORTHOGRAPHIC_Z);
    }

    public static void useVanillaFramebuffer() {
        // Reset state before we go back to normal rendering
        GlStateManager._enableDepthTest();
        // Set a sensible default for stencil buffer operations
        GlStateManager._stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
        GL30.glDisable(GL30.GL_STENCIL_TEST);

        // Bind vanilla framebuffer again
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousFBO);

        RenderSystem.disableBlend();
        // Draw GUI framebuffer -> vanilla framebuffer
        int windowWidth = Minecraft.getInstance().getWindow().getWidth();
        int windowHeight = Minecraft.getInstance().getWindow().getHeight();

        Matrix4f mf = RenderSystem.getProjectionMatrix();
        FIGURA_FRAMEBUFFER.drawToScreen(windowWidth, windowHeight);
        RenderSystem.setProjectionMatrix(mf, VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.enableBlend();
    }

    @SuppressWarnings("deprecation")
    public static void drawEntity(float x, float y, float scale, float pitch, float yaw, LivingEntity entity, GuiGraphics gui, Vector3f offset, EntityRenderMode renderMode) {
        // backup entity variables
        float headX = entity.getXRot();
        float headY = entity.yHeadRot;
        boolean invisible = entity.isInvisible();

        float bodyY = entity.yBodyRot; // not truly a backup
        if (entity.getVehicle() instanceof LivingEntity l) {
            // drawEntity(x, y, scale, pitch, yaw, l, stack, renderMode);
            bodyY = l.yBodyRot;
        }

        // setup rendering properties
        float xRot, yRot;
        double xPos = 0d;
        double yPos = 0d;

        switch (renderMode) {
            case PAPERDOLL -> {
                // rotations
                xRot = pitch;
                yRot = yaw + bodyY + 180;

                // positions
                yPos--;

                if (entity.isFallFlying())
                    xPos += Mth.triangleWave((float) Math.toRadians(270), Mth.TWO_PI);

                if (entity.isAutoSpinAttack() || entity.isVisuallySwimming() || entity.isFallFlying()) {
                    yPos++;
                    entity.setXRot(0f);
                }

                // lightning
                Lighting.setupForEntityInInventory();

                // invisibility
                if (Configs.PAPERDOLL_INVISIBLE.value)
                    entity.setInvisible(false);
            }
            case FIGURA_GUI -> {
                // rotations
                xRot = pitch;
                yRot = yaw + bodyY + 180;

                if (!Configs.PREVIEW_HEAD_ROTATION.value) {
                    entity.setXRot(0f);
                    entity.yHeadRot = bodyY;
                }

                // positions
                yPos--;

                // set up lighting
                Lighting.setupForFlatItems();
                RenderSystem.setShaderLights(Util.make(new Vector3f(-0.2f, -1f, -1f), Vector3f::normalize), Util.make(new Vector3f(-0.2f, 0.4f, -0.3f), Vector3f::normalize));

                // invisibility
                entity.setInvisible(false);
            }
            default -> {
                // rotations
                xRot = pitch;
                yRot = yaw + bodyY + 180;

                entity.setXRot(-xRot);
                entity.yHeadRot = -yaw + bodyY;

                // lightning
                Lighting.setupForEntityInInventory();
            }
        }

        // apply matrix transformers
        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 250d);
        pose.scale(scale, scale, scale);
        pose.last().pose().scale(1f, 1f, -1f); // Scale ONLY THE POSITIONS! Inverted normals don't work for whatever reason

        Avatar avatar = AvatarManager.getAvatar(entity);
        if (RenderUtils.vanillaModelAndScript(avatar) && !avatar.luaRuntime.renderer.getRootRotationAllowed()) {
            yRot = yaw;
        }

        // apply rotations
        Quaternionf quaternion = Axis.ZP.rotationDegrees(180f);
        Quaternionf quaternion2 = Axis.YP.rotationDegrees(yRot);
        Quaternionf quaternion3 = Axis.XP.rotationDegrees(xRot);
        quaternion3.mul(quaternion2);
        quaternion.mul(quaternion3);
        pose.mulPose(quaternion);
        quaternion3.conjugate();
        pose.translate(offset.x, offset.y, offset.z);

        // setup entity renderer
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        boolean renderHitboxes = dispatcher.shouldRenderHitBoxes();
        dispatcher.setRenderHitBoxes(false);
        dispatcher.setRenderShadow(false);
        dispatcher.overrideCameraOrientation(quaternion3);
        MultiBufferSource.BufferSource immediate = minecraft.renderBuffers().bufferSource();

        // render
        paperdoll = true;
        fireRot = -yRot;
        dollScale = scale;

        if (avatar != null) avatar.renderMode = renderMode;

        double finalXPos = xPos;
        double finalYPos = yPos;
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, finalXPos, finalYPos, 0d, 0f, 1f, pose, immediate, LightTexture.FULL_BRIGHT));
        immediate.endBatch();

        paperdoll = false;

        // restore entity rendering data
        dispatcher.setRenderHitBoxes(renderHitboxes);
        dispatcher.setRenderShadow(true);

        // pop matrix
        pose.popPose();
        Lighting.setupFor3DItems();

        // restore entity data
        entity.setXRot(headX);
        entity.yHeadRot = headY;
        entity.setInvisible(invisible);
    }

    public static void enableBlend() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static void prepareTexture(ResourceLocation texture) {
        enableBlend();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    public static void blit(GuiGraphics gui, int x, int y, int width, int height, ResourceLocation texture) {
        gui.blit(texture, x, y, width, height, 0f, 0f, 1, 1, 1, 1);
    }

    public static void renderAnimatedBackground(GuiGraphics gui, ResourceLocation texture, float x, float y, float width, float height, float textureWidth, float textureHeight, double speed, float delta) {
        if (speed != 0) {
            double d = (FiguraMod.ticks + delta) * speed;
            x -= d % textureWidth;
            y -= d % textureHeight;
        }

        width += textureWidth;
        height += textureHeight;

        if (speed < 0) {
            x -= textureWidth;
            y -= textureHeight;
        }

        renderBackgroundTexture(gui, texture, x, y, width, height, textureWidth, textureHeight);
    }

    public static void renderBackgroundTexture(GuiGraphics gui, ResourceLocation texture, float x, float y, float width, float height, float textureWidth, float textureHeight) {
        prepareTexture(texture);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float u1 = width / textureWidth;
        float v1 = height / textureHeight;
        quad(bufferBuilder, gui.pose().last().pose(), x, y, width, height, -999f, 0f, u1, 0f, v1);

        tessellator.end();
    }

    public static void fillRounded(GuiGraphics gui, int x, int y, int width, int height, int color) {
        gui.fill(x + 1, y, x + width - 1, y + 1, color);
        gui.fill(x, y + 1, x + width, y + height - 1, color);
        gui.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
    }

    public static void fillOutline(GuiGraphics gui, int x, int y, int width, int height, int color) {
        gui.fill(x + 1, y, x + width - 1, y + 1, color);
        gui.fill(x, y + 1, x + 1, y + height - 1, color);
        gui.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
        gui.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
    }

    public static void blitSliced(GuiGraphics gui, int x, int y, int width, int height, ResourceLocation texture) {
        blitSliced(gui, x, y, width, height, 0f, 0f, 15, 15, 15, 15, texture);
    }

    public static void blitSliced(GuiGraphics gui, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, ResourceLocation texture) {
        prepareTexture(texture);

        Matrix4f pose = gui.pose().last().pose();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float rWidthThird = regionWidth / 3f;
        float rHeightThird = regionHeight / 3f;

        // top left
        quad(buffer, pose, x, y, rWidthThird, rHeightThird, u, v, rWidthThird, rHeightThird, textureWidth, textureHeight);
        // top middle
        quad(buffer, pose, x + rWidthThird, y, width - rWidthThird * 2, rHeightThird, u + rWidthThird, v, rWidthThird, rHeightThird, textureWidth, textureHeight);
        // top right
        quad(buffer, pose, x + width - rWidthThird, y, rWidthThird, rHeightThird, u + rWidthThird * 2, v, rWidthThird, rHeightThird, textureWidth, textureHeight);

        // middle left
        quad(buffer, pose, x, y + rHeightThird, rWidthThird, height - rHeightThird * 2, u, v + rHeightThird, rWidthThird, rHeightThird, textureWidth, textureHeight);
        // middle middle
        quad(buffer, pose, x + rWidthThird, y + rHeightThird, width - rWidthThird * 2, height - rHeightThird * 2, u + rWidthThird, v + rHeightThird, rWidthThird, rHeightThird, textureWidth, textureHeight);
        // middle right
        quad(buffer, pose, x + width - rWidthThird, y + rHeightThird, rWidthThird, height - rHeightThird * 2, u + rWidthThird * 2, v + rHeightThird, rWidthThird, rHeightThird, textureWidth, textureHeight);

        // bottom left
        quad(buffer, pose, x, y + height - rHeightThird, rWidthThird, rHeightThird, u, v + rHeightThird * 2, rWidthThird, rHeightThird, textureWidth, textureHeight);
        // bottom middle
        quad(buffer, pose, x + rWidthThird, y + height - rHeightThird, width - rWidthThird * 2, rHeightThird, u + rWidthThird, v + rHeightThird * 2, rWidthThird, rHeightThird, textureWidth, textureHeight);
        // bottom right
        quad(buffer, pose, x + width - rWidthThird, y + height - rHeightThird, rWidthThird, rHeightThird, u + rWidthThird * 2, v + rHeightThird * 2, rWidthThird, rHeightThird, textureWidth, textureHeight);

        tessellator.end();
    }

    public static void renderHalfTexture(GuiGraphics gui, int x, int y, int width, int height, int textureWidth, ResourceLocation texture) {
        renderHalfTexture(gui, x, y, width, height, 0f, 0f, textureWidth, 1, textureWidth, 1, texture);
    }

    public static void renderHalfTexture(GuiGraphics gui, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, ResourceLocation texture) {
        enableBlend();

        // left
        int w = width / 2;
        gui.blit(texture, x, y, w, height, u, v, w, regionHeight, textureWidth, textureHeight);

        // right
        x += w;
        if (width % 2 == 1) w++;
        gui.blit(texture, x, y, w, height, u + regionWidth - w, v, w, regionHeight, textureWidth, textureHeight);
    }

    public static void renderSprite(GuiGraphics gui, int x, int y, int z, int width, int height, TextureAtlasSprite sprite) {
        prepareTexture(sprite.atlasLocation());
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        quad(bufferBuilder, gui.pose().last().pose(), x, y, width, height, z, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private static void quad(BufferBuilder bufferBuilder, Matrix4f pose, float x, float y, float width, float height, float u, float v, float regionWidth, float regionHeight, int textureWidth, int textureHeight) {
        float u0 = u / textureWidth;
        float v0 = v / textureHeight;
        float u1 = (u + regionWidth) / textureWidth;
        float v1 = (v + regionHeight) / textureHeight;
        quad(bufferBuilder, pose, x, y, width, height, 0f, u0, u1, v0, v1);
    }

    private static void quad(BufferBuilder bufferBuilder, Matrix4f pose, float x, float y, float width, float height, float z, float u0, float u1, float v0, float v1) {
        float x1 = x + width;
        float y1 = y + height;
        bufferBuilder.vertex(pose, x, y1, z).uv(u0, v1).endVertex();
        bufferBuilder.vertex(pose, x1, y1, z).uv(u1, v1).endVertex();
        bufferBuilder.vertex(pose, x1, y, z).uv(u1, v0).endVertex();
        bufferBuilder.vertex(pose, x, y, z).uv(u0, v0).endVertex();
    }

    public static void renderWithoutScissors(GuiGraphics gui, Consumer<GuiGraphics> toRun) {
        // very jank
        gui.enableScissor(0, 0, 1, 1);
        RenderSystem.disableScissor();
        toRun.accept(gui);
        gui.disableScissor();
    }

    public static void highlight(GuiGraphics gui, FiguraWidget widget, Component text) {
        // screen
        int screenW, screenH;
        if (Minecraft.getInstance().screen instanceof AbstractPanelScreen panel) {
            screenW = panel.width;
            screenH = panel.height;
        } else {
            return;
        }

        // draw

        int x = widget.getX();
        int y = widget.getY();
        int width = widget.getWidth();
        int height = widget.getHeight();
        int color = 0xDD000000;

        // left
        gui.fill(0, 0, x, y + height, color);
        // right
        gui.fill(x + width, y, screenW, screenH, color);
        // up
        gui.fill(x, 0, screenW, y, color);
        // down
        gui.fill(0, y + height, x + width, screenH, color);

        // outline
        fillOutline(gui, Math.max(x - 1, 0), Math.max(y - 1, 0), Math.min(width + 2, screenW), Math.min(height + 2, screenH), 0xFFFFFFFF);

        // text

        if (text == null)
            return;

        int bottomDistance = screenH - (y + height);
        int rightDistance = screenW - (x + width);
        int verArea = y * screenW - bottomDistance * screenW;
        int horArea = x * screenH - rightDistance * screenH;
        FiguraVec4 square = new FiguraVec4();

        if (Math.abs(verArea) > Math.abs(horArea)) {
            if (verArea >= 0) {
                square.set(0, 0, screenW, y);
            } else {
                square.set(0, y + height, screenW, bottomDistance);
            }
        } else {
            if (horArea >= 0) {
                square.set(0, 0, x, screenH);
            } else {
                square.set(x + width, 0, rightDistance, screenH);
            }
        }

        // fill(stack, (int) square.x, (int) square.y, (int) (square.x + square.z), (int) (square.y + square.w), 0xFFFF72AD);
        // renderTooltip(stack, text, 0, 0, false);
    }

    // widget.isMouseOver() returns false if the widget is disabled or invisible
    public static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY) {
        return isMouseOver(x, y, width, height, mouseX, mouseY, false);
    }

    public static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY, boolean force) {
        ContextMenu context = force ? null : getContext();
        return (context == null || !context.isVisible()) && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static void renderOutlineText(GuiGraphics gui, Font textRenderer, Component text, int x, int y, int color, int outline) {
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        textRenderer.drawInBatch8xOutline(text.getVisualOrderText(), x, y, color, outline, gui.pose().last().pose(), bufferSource, LightTexture.FULL_BRIGHT);
        bufferSource.endBatch();
    }

    public static void renderTooltip(GuiGraphics gui, Component tooltip, int mouseX, int mouseY, boolean background) {
        Minecraft minecraft = Minecraft.getInstance();

        // window
        int screenX = minecraft.getWindow().getGuiScaledWidth();
        int screenY = minecraft.getWindow().getGuiScaledHeight();

        boolean reduced = Configs.REDUCED_MOTION.value;

        // calculate pos
        int x = reduced ? 0 : mouseX;
        int y = reduced ? screenY : mouseY - 12;

        // prepare text
        Font font = minecraft.font;
        List<FormattedCharSequence> text = TextUtils.wrapTooltip(tooltip, font, x, screenX, 12);
        int height = font.lineHeight * text.size();

        // clamp position to bounds
        x += 12;
        y = Math.min(Math.max(y, 0), screenY - height);
        int width = TextUtils.getWidth(text, font);
        if (x + width > screenX)
            x = Math.max(x - width - 24, 0);

        if (reduced) {
            x += (screenX - width) / 2;
            if (background)
                y -= 4;
        }

        // render
        gui.pose().pushPose();
        gui.pose().translate(0d, 0d, 999d);

        if (background)
            blitSliced(gui, x - 4, y - 4, width + 8, height + 8, TOOLTIP);

        for (int i = 0; i < text.size(); i++) {
            FormattedCharSequence charSequence = text.get(i);
            gui.drawString(font, charSequence, x, y + font.lineHeight * i, 0xFFFFFF);
        }

        gui.pose().popPose();
    }

    public static void renderScrollingText(GuiGraphics gui, Component text, int x, int y, int width, int color) {
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int textX = x;

        // the text fit :D
        if (textWidth <= width) {
            gui.drawString(font, text, textX, y, color);
            return;
        }

        // oh, no it doesn't fit
        textX += getTextScrollingOffset(textWidth, width, false);

        // draw text
        gui.enableScissor(x, y, x + width, y + font.lineHeight);
        gui.drawString(font, text, textX, y, color);
        gui.disableScissor();
    }

    public static void renderCenteredScrollingText(GuiGraphics gui, Component text, int x, int y, int width, int height, int color) {
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int textX = x + width / 2;
        int textY = y + height / 2 - font.lineHeight / 2;

        // the text fit :D
        if (textWidth <= width) {
            gui.drawCenteredString(font, text, textX, textY, color);
            return;
        }

        // oh, no it doesn't fit
        textX += getTextScrollingOffset(textWidth, width, true);

        // draw text
        gui.enableScissor(x, y, x + width, y + height);
        gui.drawCenteredString(font, text, textX, textY, color);
        gui.disableScissor();
    }

    private static int getTextScrollingOffset(int textWidth, int width, boolean centered) {
        float speed = Configs.TEXT_SCROLL_SPEED.tempValue;
        int scrollLen = textWidth - width;
        int startingOffset = (int) Math.ceil(scrollLen / 2d);
        int stopDelay = (int) (Configs.TEXT_SCROLL_DELAY.tempValue * speed);
        int time = scrollLen + stopDelay;
        int totalTime = time * 2;
        int ticks = (int) (FiguraMod.ticks * speed);
        int currentTime = ticks % time;
        int dir = (ticks % totalTime) > time - 1 ? 1 : -1;

        int clamp = Math.min(Math.max(currentTime - stopDelay, 0), scrollLen);
        return (startingOffset - clamp) * dir - (centered ? 0 : startingOffset);
    }

    public static Runnable openURL(String url) {
        Minecraft minecraft = Minecraft.getInstance();
        return () -> minecraft.setScreen(new FiguraConfirmScreen.FiguraConfirmLinkScreen((bl) -> {
            if (bl) Util.getPlatform().openUri(url);
        }, url, minecraft.screen));
    }

    public static void renderLoading(GuiGraphics gui, int x, int y) {
        Component text = Component.literal(Integer.toHexString(Math.abs(FiguraMod.ticks) % 16)).withStyle(Style.EMPTY.withFont(Badges.FONT));
        Font font = Minecraft.getInstance().font;
        gui.drawString(font, text, x - font.width(text) / 2, y - font.lineHeight / 2, -1, false);
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
