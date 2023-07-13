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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.Badges;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.screens.AbstractPanelScreen;
import org.moon.figura.gui.screens.FiguraConfirmScreen;
import org.moon.figura.gui.widgets.ContextMenu;
import org.moon.figura.gui.widgets.FiguraWidget;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.model.rendering.EntityRenderMode;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.TextUtils;

import java.util.List;
import java.util.Stack;

public class UIHelper extends GuiComponent {

    // -- Variables -- //

    public static final ResourceLocation OUTLINE_FILL = new FiguraIdentifier("textures/gui/outline_fill.png");
    public static final ResourceLocation OUTLINE = new FiguraIdentifier("textures/gui/outline.png");
    public static final ResourceLocation TOOLTIP = new FiguraIdentifier("textures/gui/tooltip.png");
    public static final ResourceLocation UI_FONT = new FiguraIdentifier("ui");
    public static final ResourceLocation SPECIAL_FONT = new FiguraIdentifier("special");

    public static final Component UP_ARROW = Component.literal("^").withStyle(Style.EMPTY.withFont(UI_FONT));
    public static final Component DOWN_ARROW = Component.literal("V").withStyle(Style.EMPTY.withFont(UI_FONT));

    //Used for GUI rendering
    private static final CustomFramebuffer FIGURA_FRAMEBUFFER = new CustomFramebuffer();
    private static int previousFBO = -1;
    public static boolean paperdoll = false;
    public static float fireRot = 0f;
    public static float dollScale = 1f;
    private static final Stack<FiguraVec4> SCISSORS_STACK = new Stack<>();

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

    @SuppressWarnings("deprecation")
    public static void drawEntity(float x, float y, float scale, float pitch, float yaw, LivingEntity entity, PoseStack stack, EntityRenderMode renderMode) {
        //backup entity variables
        float headX = entity.getXRot();
        float headY = entity.yHeadRot;
        boolean invisible = entity.isInvisible();

        float bodyY = entity.yBodyRot; //not truly a backup
        if (entity.getVehicle() instanceof LivingEntity l) {
            //drawEntity(x, y, scale, pitch, yaw, l, stack, renderMode);
            bodyY = l.yBodyRot;
        }

        //setup rendering properties
        float xRot, yRot;
        double xPos = 0d;
        double yPos = 0d;

        switch (renderMode) {
            case PAPERDOLL -> {
                //rotations
                xRot = pitch;
                yRot = yaw + bodyY + 180;

                //positions
                yPos--;

                if (entity.isFallFlying())
                    xPos += Mth.triangleWave((float) Math.toRadians(270), Mth.TWO_PI);

                if (entity.isAutoSpinAttack() || entity.isVisuallySwimming() || entity.isFallFlying()) {
                    yPos++;
                    entity.setXRot(0f);
                }

                //lightning
                Lighting.setupForEntityInInventory();

                //invisibility
                if (Configs.PAPERDOLL_INVISIBLE.value)
                    entity.setInvisible(false);
            }
            case FIGURA_GUI -> {
                //rotations
                xRot = pitch;
                yRot = yaw + bodyY + 180;

                if (!Configs.PREVIEW_HEAD_ROTATION.value) {
                    entity.setXRot(0f);
                    entity.yHeadRot = bodyY;
                }

                //positions
                yPos--;

                //set up lighting
                Lighting.setupForFlatItems();
                RenderSystem.setShaderLights(Util.make(new Vector3f(-0.2f, -1f, -1f), Vector3f::normalize), Util.make(new Vector3f(-0.2f, 0.4f, -0.3f), Vector3f::normalize));

                //invisibility
                entity.setInvisible(false);
            }
            default -> {
                //rotations
                float rot = (float) Math.atan(pitch / 40f) * 20f;

                xRot = (float) Math.atan(yaw / 40f) * 20f;
                yRot = -rot + bodyY + 180;

                entity.setXRot(-xRot);
                entity.yHeadRot = rot + bodyY;

                //lightning
                Lighting.setupForEntityInInventory();
            }
        }

        //apply matrix transformers
        stack.pushPose();
        stack.translate(x, y, renderMode == EntityRenderMode.MINECRAFT_GUI ? 250d : -250d);
        stack.scale(scale, scale, scale);
        stack.last().pose().multiply(Matrix4f.createScaleMatrix(1f, 1f, -1f)); //Scale ONLY THE POSITIONS! Inverted normals don't work for whatever reason

        //apply rotations
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180f);
        Quaternion quaternion2 = Vector3f.YP.rotationDegrees(yRot);
        Quaternion quaternion3 = Vector3f.XP.rotationDegrees(xRot);
        quaternion3.mul(quaternion2);
        quaternion.mul(quaternion3);
        stack.mulPose(quaternion);
        quaternion3.conj();

        //setup entity renderer
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        boolean renderHitboxes = dispatcher.shouldRenderHitBoxes();
        dispatcher.setRenderHitBoxes(false);
        dispatcher.setRenderShadow(false);
        dispatcher.overrideCameraOrientation(quaternion3);
        MultiBufferSource.BufferSource immediate = minecraft.renderBuffers().bufferSource();

        //render
        paperdoll = true;
        fireRot = -yRot;
        dollScale = scale;

        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar != null) avatar.renderMode = renderMode;

        double finalXPos = xPos;
        double finalYPos = yPos;
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, finalXPos, finalYPos, 0d, 0f, 1f, stack, immediate, LightTexture.FULL_BRIGHT));
        immediate.endBatch();

        paperdoll = false;

        //restore entity rendering data
        dispatcher.setRenderHitBoxes(renderHitboxes);
        dispatcher.setRenderShadow(true);

        //pop matrix
        stack.popPose();
        Lighting.setupFor3DItems();

        //restore entity data
        entity.setXRot(headX);
        entity.yHeadRot = headY;
        entity.setInvisible(invisible);
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

    public static void renderAnimatedBackground(PoseStack stack, ResourceLocation texture, float x, float y, float width, float height, float textureWidth, float textureHeight, double speed, float delta) {
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

        renderBackgroundTexture(stack, texture, x, y, width, height, textureWidth, textureHeight);
    }

    public static void renderBackgroundTexture(PoseStack stack, ResourceLocation texture, float x, float y, float width, float height, float textureWidth, float textureHeight) {
        setupTexture(texture);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float u1 = width / textureWidth;
        float v1 = height / textureHeight;
        quad(bufferBuilder, stack.last().pose(), x, y, width, height, -999f, 0f, u1, 0f, v1);

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
        quad(buffer, pose, x, y, rWidthThird, rHeightThird, u, v, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //top middle
        quad(buffer, pose, x + rWidthThird, y, width - rWidthThird * 2, rHeightThird, u + rWidthThird, v, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //top right
        quad(buffer, pose, x + width - rWidthThird, y, rWidthThird, rHeightThird, u + rWidthThird * 2, v, rWidthThird, rHeightThird, textureWidth, textureHeight);

        //middle left
        quad(buffer, pose, x, y + rHeightThird, rWidthThird, height - rHeightThird * 2, u, v + rHeightThird, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //middle middle
        quad(buffer, pose, x + rWidthThird, y + rHeightThird, width - rWidthThird * 2, height - rHeightThird * 2, u + rWidthThird, v + rHeightThird, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //middle right
        quad(buffer, pose, x + width - rWidthThird, y + rHeightThird, rWidthThird, height - rHeightThird * 2, u + rWidthThird * 2, v + rHeightThird, rWidthThird, rHeightThird, textureWidth, textureHeight);

        //bottom left
        quad(buffer, pose, x, y + height - rHeightThird, rWidthThird, rHeightThird, u, v + rHeightThird * 2, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //bottom middle
        quad(buffer, pose, x + rWidthThird, y + height - rHeightThird, width - rWidthThird * 2, rHeightThird, u + rWidthThird, v + rHeightThird * 2, rWidthThird, rHeightThird, textureWidth, textureHeight);
        //bottom right
        quad(buffer, pose, x + width - rWidthThird, y + height - rHeightThird, rWidthThird, rHeightThird, u + rWidthThird * 2, v + rHeightThird * 2, rWidthThird, rHeightThird, textureWidth, textureHeight);

        tessellator.end();
    }

    public static void renderHalfTexture(PoseStack stack, int x, int y, int width, int height, int textureWidth, ResourceLocation texture) {
        renderHalfTexture(stack, x, y, width, height, 0f, 0f, textureWidth, 1, textureWidth, 1, texture);
    }

    public static void renderHalfTexture(PoseStack stack, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, ResourceLocation texture) {
        setupTexture(texture);

        //left
        int w = width / 2;
        blit(stack, x, y, w, height, u, v, w, regionHeight, textureWidth, textureHeight);

        //right
        x += w;
        if (width % 2 == 1) w++;
        blit(stack, x, y, w, height, u + regionWidth - w, v, w, regionHeight, textureWidth, textureHeight);
    }

    public static void renderSprite(PoseStack stack, int x, int y, int z, int width, int height, TextureAtlasSprite sprite) {
        setupTexture(sprite.atlas().location());
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        quad(bufferBuilder, stack.last().pose(), x, y, width, height, z, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1());
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void setupScissor(int x, int y, int width, int height) {
        FiguraVec4 vec = FiguraVec4.of(x, y, width, height);
        if (!SCISSORS_STACK.isEmpty()) {
            FiguraVec4 old = SCISSORS_STACK.peek();
            double newX = Math.max(x, old.x());
            double newY = Math.max(y, old.y());
            double newWidth = Math.min(x + width, old.x() + old.z()) - newX;
            double newHeight = Math.min(y + height, old.y() + old.w()) - newY;
            vec.set(newX, newY, newWidth, newHeight);
        }

        SCISSORS_STACK.push(vec);
        setupScissor(vec);
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

    private static void setupScissor(FiguraVec4 dimensions) {
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        int screenY = Minecraft.getInstance().getWindow().getHeight();

        int scaledWidth = (int) Math.max(dimensions.z * scale, 0);
        int scaledHeight = (int) Math.max(dimensions.w * scale, 0);
        RenderSystem.enableScissor((int) (dimensions.x * scale), (int) (screenY - dimensions.y * scale - scaledHeight), scaledWidth, scaledHeight);
    }

    public static void disableScissor() {
        SCISSORS_STACK.pop();
        if (!SCISSORS_STACK.isEmpty()) {
            setupScissor(SCISSORS_STACK.peek());
        } else {
            RenderSystem.disableScissor();
        }
    }

    public static void renderWithoutScissors(Runnable toRun) {
        RenderSystem.disableScissor();
        toRun.run();
        if (!SCISSORS_STACK.isEmpty()) {
            setupScissor(SCISSORS_STACK.peek());
        }
    }

    public static void highlight(PoseStack stack, FiguraWidget widget, Component text) {
        //screen
        int screenW, screenH;
        if (Minecraft.getInstance().screen instanceof AbstractPanelScreen panel) {
            screenW = panel.width;
            screenH = panel.height;
        } else {
            return;
        }

        //draw

        int x = widget.getX();
        int y = widget.getY();
        int width = widget.getWidth();
        int height = widget.getHeight();
        int color = 0xDD000000;

        //left
        fill(stack, 0, 0, x, y + height, color);
        //right
        fill(stack, x + width, y, screenW, screenH, color);
        //up
        fill(stack, x, 0, screenW, y, color);
        //down
        fill(stack, 0, y + height, x + width, screenH, color);

        //outline
        fillOutline(stack, Math.max(x - 1, 0), Math.max(y - 1, 0), Math.min(width + 2, screenW), Math.min(height + 2, screenH), 0xFFFFFFFF);

        //text

        if (text == null)
            return;

        //Woolfy generated code
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

        //fill(stack, (int) square.x, (int) square.y, (int) (square.x + square.z), (int) (square.y + square.w), 0xFFFF72AD);
        //renderTooltip(stack, text, 0, 0, false);
    }

    //widget.isMouseOver() returns false if the widget is disabled or invisible
    public static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY) {
        return isMouseOver(x, y, width, height, mouseX, mouseY, false);
    }

    public static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY, boolean force) {
        ContextMenu context = force ? null : getContext();
        return (context == null || !context.isVisible()) && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static void renderOutlineText(PoseStack stack, Font textRenderer, Component text, int x, int y, int color, int outline) {
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        textRenderer.drawInBatch8xOutline(text.getVisualOrderText(), x, y, color, outline, stack.last().pose(), bufferSource, LightTexture.FULL_BRIGHT);
        bufferSource.endBatch();
    }

    public static void renderTooltip(PoseStack stack, Component tooltip, int mouseX, int mouseY, boolean background) {
        Minecraft minecraft = Minecraft.getInstance();

        //window
        int screenX = minecraft.getWindow().getGuiScaledWidth();
        int screenY = minecraft.getWindow().getGuiScaledHeight();

        boolean reduced = Configs.REDUCED_MOTION.value;

        //calculate pos
        int x = reduced ? 0 : mouseX;
        int y = reduced ? screenY : mouseY - 12;

        //prepare text
        Font font = minecraft.font;
        List<FormattedCharSequence> text = TextUtils.wrapTooltip(tooltip, font, x, screenX, 12);
        int height = font.lineHeight * text.size();

        //clamp position to bounds
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

        //render
        stack.pushPose();
        stack.translate(0d, 0d, 999d);

        if (background)
            renderSliced(stack, x - 4, y - 4, width + 8, height + 8, TOOLTIP);

        for (int i = 0; i < text.size(); i++) {
            FormattedCharSequence charSequence = text.get(i);
            font.drawShadow(stack, charSequence, x, y + font.lineHeight * i, 0xFFFFFF);
        }

        stack.popPose();
    }

    public static void renderScrollingText(PoseStack stack, Component text, int x, int y, int width, int color) {
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int textX = x;

        //the text fit :D
        if (textWidth <= width) {
            font.draw(stack, text, textX, y, color);
            return;
        }

        //oh, no it doesn't fit
        textX += getTextScrollingOffset(textWidth, width, false);

        //draw text
        setupScissor(x, y, width, font.lineHeight);
        font.draw(stack, text, textX, y, color);
        disableScissor();
    }

    public static void renderCenteredScrollingText(PoseStack stack, Component text, int x, int y, int width, int height, int color) {
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        int textX = x + width / 2;
        int textY = y + height / 2 - font.lineHeight / 2;

        //the text fit :D
        if (textWidth <= width) {
            drawCenteredString(stack, font, text, textX, textY, color);
            return;
        }

        //oh, no it doesn't fit
        textX += getTextScrollingOffset(textWidth, width, true);

        //draw text
        setupScissor(x, y, width, height);
        drawCenteredString(stack, font, text, textX, textY, color);
        disableScissor();
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

    public static void renderLoading(PoseStack stack, int x, int y) {
        Component text = Component.literal(Integer.toHexString(Math.abs(FiguraMod.ticks) % 16)).withStyle(Style.EMPTY.withFont(Badges.FONT));
        Font font = Minecraft.getInstance().font;
        font.draw(stack, text, (int) (x - font.width(text) / 2f), (int) (y - font.lineHeight / 2f), -1);
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
