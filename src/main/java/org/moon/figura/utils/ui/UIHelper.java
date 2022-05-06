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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.gui.screens.AbstractPanelScreen;
import org.moon.figura.gui.widgets.ContextMenu;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.TextUtils;

public class UIHelper extends GuiComponent {

    // -- Variables -- //

    public static final ResourceLocation OUTLINE = new FiguraIdentifier("textures/gui/outline.png");

    public static boolean forceNameplate = false;
    public static boolean forceNoFire = false;

    //Used for GUI rendering
    private static final CustomFramebuffer FIGURA_FRAMEBUFFER = new CustomFramebuffer();
    private static int previousFBO = -1;

    // -- Functions -- //

    public static void useFiguraGuiFramebuffer() {
        previousFBO = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);

        int windowWidth = Minecraft.getInstance().getWindow().getScreenWidth();
        int windowHeight = Minecraft.getInstance().getWindow().getScreenHeight();
        FIGURA_FRAMEBUFFER.setSize(windowWidth, windowHeight);

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
        Minecraft.getInstance().getMainRenderTarget().blitToScreen(windowWidth, windowHeight, false);
        RenderSystem.setProjectionMatrix(mf);
    }

    public static void useVanillaFramebuffer(PoseStack stack) {
        //Reset state before we go back to normal rendering
        GlStateManager._enableDepthTest();
        //Set a sensible default for stencil buffer operations
        GlStateManager._stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
        GL30.glDisable(GL30.GL_STENCIL_TEST);

        //Bind vanilla framebuffer again
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousFBO);

        RenderSystem.disableBlend();
        //Draw GUI framebuffer -> vanilla framebuffer
        int windowWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int windowHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        Matrix4f mf = RenderSystem.getProjectionMatrix();
        FIGURA_FRAMEBUFFER.drawToScreen(stack, windowWidth, windowHeight);
        RenderSystem.setProjectionMatrix(mf);
        RenderSystem.enableBlend();
    }

    public static void drawEntity(int x, int y, float scale, float pitch, float yaw, LivingEntity entity, PoseStack stack) {
        Avatar avatar = AvatarManager.getAvatar(entity);

        //apply matrix transformers
        stack.pushPose();
        stack.translate(x, y, 0f);
        stack.scale(scale, scale, scale);
        stack.last().pose().multiply(Matrix4f.createScaleMatrix(1f, 1f, -1f)); //Scale ONLY THE POSITIONS! Inverted normals don't work for whatever reason

        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180f);
        Quaternion quaternion2 = Vector3f.XP.rotationDegrees(pitch);
        quaternion.mul(quaternion2);
        stack.mulPose(quaternion);
        quaternion2.conj();

        //backup entity variables
        float bodyYaw = entity.yBodyRot;
        float entityYaw = entity.getYRot();
        float entityPitch = entity.getXRot();
        float prevHeadYaw = entity.yHeadRotO;
        float headYaw = entity.yHeadRot;
        boolean invisible = entity.isInvisible();

        //apply entity rotation
        entity.yBodyRot = 180f - yaw;
        entity.setYRot(180f - yaw);
        entity.setXRot(0f);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        entity.setInvisible(false);
        UIHelper.forceNameplate = true;
        UIHelper.forceNoFire = true;

        //set up lighting
        Lighting.setupForFlatItems();
        RenderSystem.setShaderLights(Util.make(new Vector3f(-0.2f, -1f, -1f), Vector3f::normalize), Util.make(new Vector3f(-0.2f, 0.4f, -0.3f), Vector3f::normalize));

        //setup entity renderer
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        boolean renderHitboxes = dispatcher.shouldRenderHitBoxes();
        dispatcher.setRenderHitBoxes(false);
        dispatcher.setRenderShadow(false);
        dispatcher.overrideCameraOrientation(quaternion2);
        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();

        //render
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0d, -1d, 0d, 0f, 1f, stack, immediate, LightTexture.FULL_BRIGHT));
        immediate.endBatch();

        //restore entity rendering data
        dispatcher.setRenderHitBoxes(renderHitboxes);
        dispatcher.setRenderShadow(true);

        //restore entity data
        entity.yBodyRot = bodyYaw;
        entity.setYRot(entityYaw);
        entity.setXRot(entityPitch);
        entity.yHeadRotO = prevHeadYaw;
        entity.yHeadRot = headYaw;
        entity.setInvisible(invisible);
        UIHelper.forceNameplate = false;
        UIHelper.forceNoFire = false;

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

    public static void renderBackgroundTexture(ResourceLocation texture, int x, int y, int width, int height, float textureWidth, float textureHeight) {
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
        setupTexture(texture);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        //top left
        sliceVertex(stack.last().pose(), bufferBuilder, x, y, 3, 3, 0f, 0f, 9, 9);
        //top middle
        sliceVertex(stack.last().pose(), bufferBuilder, x + 3, y, width - 6, 3, 3f, 0f, 9, 9);
        //top right
        sliceVertex(stack.last().pose(), bufferBuilder, x + width - 3, y, 3, 3, 6f, 0f, 9, 9);

        //middle left
        sliceVertex(stack.last().pose(), bufferBuilder, x, y + 3, 3, height - 6, 0f, 3f, 9, 9);
        //middle middle
        sliceVertex(stack.last().pose(), bufferBuilder, x + 3, y + 3, width - 6, height - 6, 3f, 3f, 9, 9);
        //middle right
        sliceVertex(stack.last().pose(), bufferBuilder, x + width - 3, y + 3, 3, height - 6, 6f, 3f, 9, 9);

        //bottom left
        sliceVertex(stack.last().pose(), bufferBuilder, x, y + height - 3, 3, 3, 0f, 6f, 9, 9);
        //bottom middle
        sliceVertex(stack.last().pose(), bufferBuilder, x + 3, y + height - 3, width - 6, 3, 3f, 6f, 9, 9);
        //bottom right
        sliceVertex(stack.last().pose(), bufferBuilder, x + width - 3, y + height - 3, 3, 3, 6f, 6f, 9, 9);

        tessellator.end();
    }

    public static void sliceVertex(Matrix4f matrix, BufferBuilder bufferBuilder, int x, int y, int width, int height, float u, float v, int texWidth, int texHeight) {
        bufferBuilder.vertex(matrix, x, y, 0f).uv(u / texWidth, v / texHeight).endVertex();
        bufferBuilder.vertex(matrix, x, y + height, 0f).uv(u / texWidth, (v + 3) / texHeight).endVertex();
        bufferBuilder.vertex(matrix, x + width, y + height, 0f).uv((u + 3) / texWidth, (v + 3) / texHeight).endVertex();
        bufferBuilder.vertex(matrix, x + width, y, 0f).uv((u + 3) / texWidth, v / texHeight).endVertex();
    }

    public static void setupScissor(int x, int y, int width, int height) {
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        int screenY = Minecraft.getInstance().getWindow().getScreenHeight();

        int scaledWidth = (int) Math.max(width * scale, 0);
        int scaledHeight = (int) Math.max(height * scale, 0);
        RenderSystem.enableScissor((int) (x * scale), (int) (screenY - y * scale - scaledHeight), scaledWidth, scaledHeight);
    }

    //widget.isMouseOver() returns false if the widget is disabled or invisible
    public static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static void renderOutlineText(PoseStack stack, Font textRenderer, Component text, float x, float y, int color, int outline) {
        Component outlineText = new TextComponent(text.getString().replaceAll("§.", "")).setStyle(text.getStyle().withColor(outline));
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                textRenderer.draw(stack, outlineText, x + i, y + j, outline);
            }
        }

        stack.pushPose();
        stack.translate(0f, 0f, 0.1f);
        textRenderer.draw(stack, text, x, y, color);
        stack.popPose();
    }

    public static void renderTooltip(PoseStack stack, Component tooltip, int mouseX, int mouseY) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen != null) screen.renderComponentTooltip(stack, TextUtils.splitText(tooltip, "\n"), mouseX, Math.max(mouseY, 16));
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
}
