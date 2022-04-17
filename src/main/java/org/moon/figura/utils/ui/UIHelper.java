package org.moon.figura.utils.ui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.moon.figura.utils.TextUtils;

public class UIHelper extends DrawableHelper {

    // -- Variables -- //

    public static boolean forceNameplate = false;
    public static boolean forceNoFire = false;

    //Used for GUI rendering
    private static final CustomFramebuffer FIGURA_FRAMEBUFFER = new CustomFramebuffer();
    private static int previousFBO = -1;

    // -- Functions -- //

    public static void useFiguraGuiFramebuffer() {
        previousFBO = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);

        int windowWidth = MinecraftClient.getInstance().getWindow().getWidth();
        int windowHeight = MinecraftClient.getInstance().getWindow().getHeight();
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
        MinecraftClient.getInstance().getFramebuffer().draw(windowWidth, windowHeight, false);
        RenderSystem.setProjectionMatrix(mf);
    }

    public static void useVanillaFramebuffer(MatrixStack stack) {
        //Reset state before we go back to normal rendering
        GlStateManager._enableDepthTest();
        //Set a sensible default for stencil buffer operations
        GlStateManager._stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
        GL30.glDisable(GL30.GL_STENCIL_TEST);

        //Bind vanilla framebuffer again
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousFBO);

        RenderSystem.disableBlend();
        //Draw GUI framebuffer -> vanilla framebuffer
        int windowWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int windowHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

        Matrix4f mf = RenderSystem.getProjectionMatrix();
        FIGURA_FRAMEBUFFER.drawToScreen(stack, windowWidth, windowHeight);
        RenderSystem.setProjectionMatrix(mf);
        RenderSystem.enableBlend();
    }

    public static void drawEntity(int x, int y, float scale, float pitch, float yaw, LivingEntity entity, MatrixStack matrices) {
        //apply matrix transformers
        matrices.push();
        matrices.translate(x, y, 0d);
        matrices.scale(scale, scale, scale);
        matrices.peek().getPositionMatrix().multiply(Matrix4f.scale(1f, 1f, -1f)); //Scale ONLY THE POSITIONS! Inverted normals don't work for whatever reason

        Quaternion quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180f);
        Quaternion quaternion2 = Vec3f.POSITIVE_X.getDegreesQuaternion(pitch);
        quaternion.hamiltonProduct(quaternion2);
        matrices.multiply(quaternion);
        quaternion2.conjugate();

        //backup entity variables
        float bodyYaw = entity.bodyYaw;
        float entityYaw = entity.getYaw();
        float entityPitch = entity.getPitch();
        float prevHeadYaw = entity.prevHeadYaw;
        float headYaw = entity.headYaw;
        boolean invisible = entity.isInvisible();

        //apply entity rotation
        entity.bodyYaw = 180f - yaw;
        entity.setYaw(180f - yaw);
        entity.setPitch(0f);
        entity.headYaw = entity.getYaw();
        entity.prevHeadYaw = entity.getYaw();
        entity.setInvisible(false);
        UIHelper.forceNameplate = true;
        UIHelper.forceNoFire = true;

        //set up lighting
        DiffuseLighting.disableGuiDepthLighting();
        RenderSystem.setShaderLights(Util.make(new Vec3f(-0.2f, -1f, -1f), Vec3f::normalize), Util.make(new Vec3f(-0.2f, 0.4f, -0.3f), Vec3f::normalize));

        //setup entity renderer
        EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        boolean renderHitboxes = dispatcher.shouldRenderHitboxes();
        dispatcher.setRenderHitboxes(false);
        dispatcher.setRenderShadows(false);
        dispatcher.setRotation(quaternion2);
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        //render
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0d, -1d, 0d, 0f, 1f, matrices, immediate, LightmapTextureManager.MAX_LIGHT_COORDINATE));
        immediate.draw();

        //restore entity rendering data
        dispatcher.setRenderHitboxes(renderHitboxes);
        dispatcher.setRenderShadows(true);

        //restore entity data
        entity.bodyYaw = bodyYaw;
        entity.setYaw(entityYaw);
        entity.setPitch(entityPitch);
        entity.prevHeadYaw = prevHeadYaw;
        entity.headYaw = headYaw;
        entity.setInvisible(invisible);
        UIHelper.forceNameplate = false;
        UIHelper.forceNoFire = false;

        //pop matrix
        matrices.pop();
        DiffuseLighting.enableGuiDepthLighting();
    }

    public static void renderBackgroundTexture(Identifier texture, int x, int y, int width, int height, float textureWidth, float textureHeight) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        bufferBuilder.vertex(x, y + height, 0f).texture(0f, height / textureHeight).next();
        bufferBuilder.vertex(x + width, y + height, 0f).texture(width / textureWidth, height / textureHeight).next();
        bufferBuilder.vertex(x + width, y, 0f).texture(width / textureWidth, 0f).next();
        bufferBuilder.vertex(x, y, 0f).texture(0f, 0f).next();

        tessellator.draw();
    }

    public static void fillRounded(MatrixStack matrixStack, int x, int y, int width, int height, int color) {
        fill(matrixStack, x + 1, y, x + width - 1, y + 1, color);
        fill(matrixStack, x, y + 1, x + width, y + height - 1, color);
        fill(matrixStack, x + 1, y + height - 1, x + width - 1, y + height, color);
    }

    public static void fillOutline(MatrixStack matrixStack, int x, int y, int width, int height, int color) {
        fill(matrixStack, x + 1, y, x + width - 1, y + 1, color);
        fill(matrixStack, x, y + 1, x + 1, y + height - 1, color);
        fill(matrixStack, x + width - 1, y + 1, x + width, y + height - 1, color);
        fill(matrixStack, x + 1, y + height - 1, x + width - 1, y + height, color);
    }

    public static void renderSliced(MatrixStack matrices, int x, int y, int width, int height, Identifier texture) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        //top left
        renderSlice(matrices.peek().getPositionMatrix(), bufferBuilder, x, y, 3, 3, 0f, 0f, 9, 9);
        //top middle
        renderSlice(matrices.peek().getPositionMatrix(), bufferBuilder, x + 3, y, width - 6, 3, 3f, 0f, 9, 9);
        //top right
        renderSlice(matrices.peek().getPositionMatrix(), bufferBuilder, x + width - 3, y, 3, 3, 6f, 0f, 9, 9);

        //middle left
        renderSlice(matrices.peek().getPositionMatrix(), bufferBuilder, x, y + 3, 3, height - 6, 0f, 3f, 9, 9);
        //middle middle
        renderSlice(matrices.peek().getPositionMatrix(), bufferBuilder, x + 3, y + 3, width - 6, height - 6, 3f, 3f, 9, 9);
        //middle right
        renderSlice(matrices.peek().getPositionMatrix(), bufferBuilder, x + width - 3, y + 3, 3, height - 6, 6f, 3f, 9, 9);

        //bottom left
        renderSlice(matrices.peek().getPositionMatrix(), bufferBuilder, x, y + height - 3, 3, 3, 0f, 6f, 9, 9);
        //bottom middle
        renderSlice(matrices.peek().getPositionMatrix(), bufferBuilder, x + 3, y + height - 3, width - 6, 3, 3f, 6f, 9, 9);
        //bottom right
        renderSlice(matrices.peek().getPositionMatrix(), bufferBuilder, x + width - 3, y + height - 3, 3, 3, 6f, 6f, 9, 9);

        tessellator.draw();
    }

    public static void renderSlice(Matrix4f matrix, BufferBuilder bufferBuilder, int x, int y, int width, int height, float u, float v, int texHeight, int texWidth) {
        bufferBuilder.vertex(matrix, x, y, 0f)
                .texture(u / texWidth, v / texHeight)
                .next();
        bufferBuilder.vertex(matrix, x, y + height, 0f)
                .texture(u / texWidth, (v + 3) / texHeight)
                .next();
        bufferBuilder.vertex(matrix, x + width, y + height, 0f)
                .texture((u + 3) / texWidth, (v + 3) / texHeight)
                .next();
        bufferBuilder.vertex(matrix, x + width, y, 0f)
                .texture((u + 3) / texWidth, v / texHeight)
                .next();
    }

    public static void setupScissor(int x, int y, int width, int height) {
        double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        int screenY = MinecraftClient.getInstance().getWindow().getHeight();

        int scaledWidth = (int) Math.max(width * scale, 0);
        int scaledHeight = (int) Math.max(height * scale, 0);
        RenderSystem.enableScissor((int) (x * scale), (int) (screenY - y * scale - scaledHeight), scaledWidth, scaledHeight);
    }

    //widget.isMouseOver() returns false if the widget is disabled or invisible
    public static boolean isMouseOver(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static void renderOutlineText(MatrixStack matrices, TextRenderer textRenderer, Text text, float x, float y, int color, int outline) {
        Text outlineText = new LiteralText(text.getString().replaceAll("§.", "")).setStyle(text.getStyle().withColor(outline));
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                textRenderer.draw(matrices, outlineText, x + i, y + j, outline);
            }
        }

        matrices.push();
        matrices.translate(0f, 0f, 0.1f);
        textRenderer.draw(matrices, text, x, y, color);
        matrices.pop();
    }

    public static void renderTooltip(MatrixStack matrices, Text tooltip, int mouseX, int mouseY) {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen != null) screen.renderTooltip(matrices, TextUtils.splitText(tooltip, "\n"), mouseX, Math.max(mouseY, 16));
    }

    /* TODO - WIP
    public static void setContext(ContextMenu context) {
        if (MinecraftClient.getInstance().currentScreen instanceof AbstractPanelScreen panelScreen)
            panelScreen.contextMenu = context;
    }

    public static ContextMenu getContext() {
        if (MinecraftClient.getInstance().currentScreen instanceof AbstractPanelScreen panelScreen)
            return panelScreen.contextMenu;

        return null;
    }

    public static void setTooltip(Text text) {
        if (MinecraftClient.getInstance().currentScreen instanceof AbstractPanelScreen panelScreen)
            panelScreen.tooltip = text;
    }
     */
}
