package org.figuramc.figura.gui.widgets;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import org.figuramc.figura.utils.ui.UIHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Function;

public class BackendMotdWidget extends AbstractWidget implements Widget, GuiEventListener {
    private final Font font;
    private final FiguraMuliLineTextWidget multilineWidget;
    private double scrollAmount;
    private boolean scrolling;

    public BackendMotdWidget(int i, int j, int k, int l, Component component, Font textRenderer) {
        super(i, j, k, l, component);
        this.font = textRenderer;
        Pair<MultiLineLabel, Integer> multiLineLabelWithWidth = createAndReturnWidth(textRenderer, component);
        this.multilineWidget = new FiguraMuliLineTextWidget(multiLineLabelWithWidth.getFirst(), multiLineLabelWithWidth.getSecond(), textRenderer, component, true).setMaxWidth(this.getWidth() - this.totalInnerPadding());
    }

    static Pair<MultiLineLabel, Integer> createAndReturnWidth(Font renderer, Component... texts) {
        List<MultiLineLabel.TextWithWidth> textWithWidthList = Arrays.stream(texts).map(Component::getVisualOrderText).map((text) -> new MultiLineLabel.TextWithWidth(text, renderer.width(text))).collect(ImmutableList.toImmutableList());
        int width = textWithWidthList.stream().mapToInt((textWithWidth) -> textWithWidth.width).max().orElse(0);
        return Pair.of(MultiLineLabel.createFixed(renderer, textWithWidthList), width);
    }
    public BackendMotdWidget setColor(int i) {
        this.multilineWidget.setColor(i);
        return this;
    }

    public void setWidth(int value) {
        super.setWidth(value);
        this.multilineWidget.setMaxWidth(this.getWidth() - this.totalInnerPadding());
    }

    protected int totalInnerPadding() {
        return innerPadding() * 2;
    }

    protected int getInnerHeight() {
        return this.multilineWidget.getHeight();
    }

    protected boolean scrollbarVisible() {
        return this.getInnerHeight() > this.getHeight();
    }

    protected double scrollRate() {
        Objects.requireNonNull(this.font);
        return 9.0;
    }

    protected int innerPadding() {
        return 4;
    }

    protected void renderBorder(PoseStack stack, int x, int y, int width, int height) {
        UIHelper.renderSliced(stack, this.x - this.innerPadding(), this.y - this.innerPadding(), this.getWidth() + this.totalInnerPadding(), this.getHeight() + this.totalInnerPadding(), UIHelper.OUTLINE_FILL);
    }

    protected void renderBackground(PoseStack pose) {
        UIHelper.renderSliced(pose, this.x - this.innerPadding(), this.y - this.innerPadding(), this.getWidth() + this.totalInnerPadding(), this.getHeight() + this.totalInnerPadding(), UIHelper.OUTLINE_FILL);
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            if (!this.scrollbarVisible()) {
                this.renderBackground(pose);
                pose.pushPose();
                pose.translate((float)this.x, (float)this.y, 0.0F);
                this.multilineWidget.render(pose, mouseX, mouseY, delta);
                pose.popPose();
            } else {
                if (this.visible) {
                    this.renderBackground(pose);
                    RenderSystem.enableScissor(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1);
                    pose.pushPose();
                    pose.translate(0.0, -this.scrollAmount, 0.0);
                    this.renderContents(pose, mouseX, mouseY, delta);
                    pose.popPose();
                    RenderSystem.disableScissor();
                    this.renderDecorations(pose);
                }
            }

        }
    }

    protected void renderContents(PoseStack pose, int mouseX, int mouseY, float delta) {
        pose.pushPose();
        pose.translate((float)(this.x + this.innerPadding()), (float)(this.y + this.innerPadding()), 0.0F);
        this.multilineWidget.render(pose, mouseX, mouseY, delta);
        pose.popPose();
    }

    protected void updateWidgetNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, this.getMessage());
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder) {

    }
    public boolean shouldRender() {
        return getScrollBarHeight() > 0 && this.height >= 48;
    }

    protected double scrollAmount() {
        return this.scrollAmount;
    }

    protected void setScrollAmount(double scrollAmount) {
        this.scrollAmount = Mth.clamp(scrollAmount, 0.0, (double)this.getMaxScrollAmount());
    }

    protected int getMaxScrollAmount() {
        return Math.max(0, this.getContentHeight() - (this.height - 4));
    }

    private int getContentHeight() {
        return 4;
    }

    private void renderScrollBar() {
        int i = this.getScrollBarHeight();
        int j = this.x + this.width;
        int k = this.x + this.width + 8;
        int l = Math.max(this.y, (int)this.scrollAmount * (this.height - i) / this.getMaxScrollAmount() + this.y);
        int m = l + i;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(j, m, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(k, m, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(k, l, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(j, l, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(j, (m - 1), 0.0).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex((k - 1), (m - 1), 0.0).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex((k - 1), l, 0.0).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex(j, l, 0.0).color(192, 192, 192, 255).endVertex();
        tesselator.end();
    }
    private int getScrollBarHeight() {
        return Mth.clamp((int)((float)(this.height * this.height) / (float)this.getContentHeight()), 32, this.height);
    }

    protected void renderDecorations(PoseStack matrices) {
        if (this.scrollbarVisible()) {
            this.renderScrollBar();
        }
    }

    protected boolean withinContentAreaTopBottom(int top, int bottom) {
        return (double)bottom - this.scrollAmount >= (double)this.y && (double)top - this.scrollAmount <= (double)(this.y + this.height);
    }

    protected boolean withinContentAreaPoint(double x, double y) {
        return x >= (double)this.x && x < (double)(this.x + this.width) && y >= (double)this.y && y < (double)(this.y + this.height);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.visible) {
            return false;
        } else {
            boolean bl = this.withinContentAreaPoint(mouseX, mouseY);
            boolean bl2 = this.scrollbarVisible() && mouseX >= (double)(this.x + this.width) && mouseX <= (double)(this.x + this.width + 8) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
            this.setFocused(bl || bl2);
            if (bl2 && button == 0) {
                this.scrolling = true;
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.scrolling = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.visible && this.isFocused() && this.scrolling) {
            if (mouseY < (double)this.y) {
                this.setScrollAmount(0.0);
            } else if (mouseY > (double)(this.y + this.height)) {
                this.setScrollAmount((double)this.getMaxScrollAmount());
            } else {
                int i = this.getScrollBarHeight();
                double d = (double)Math.max(1, this.getMaxScrollAmount() / (this.height - i));
                this.setScrollAmount(this.scrollAmount + deltaY * d);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.visible && this.isFocused()) {
            this.setScrollAmount(this.scrollAmount - amount * this.scrollRate());
            return true;
        } else {
            return false;
        }
    }

    protected static class FiguraMuliLineTextWidget extends AbstractWidget {
        private int color = 0xFFFFFF;
        private OptionalInt maxWidth = OptionalInt.empty();
        private OptionalInt maxRows = OptionalInt.empty();
        private Font font;
        private int multiLineWidth;
        private final SingleKeyCache<CacheKey, MultiLineLabel> cache = new SingleKeyCache<>(key -> {
            if (key.maxRows.isPresent()) {
                return MultiLineLabel.create(font, key.message, key.maxWidth, key.maxRows.getAsInt());
            }
            return MultiLineLabel.create(font, key.message, key.maxWidth);
        });
        private boolean centered = false;
        public FiguraMuliLineTextWidget(MultiLineLabel multiLineLabel, Integer width, Font textRenderer, Component component, boolean bl) {
            super(0, 0, width, multiLineLabel.getLineCount() * textRenderer.lineHeight, component);
            this.font = textRenderer;
            this.multiLineWidth = width;
        }

        @Override
        public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
            MultiLineLabel multiLineLabel = this.cache.getValue(this.getFreshCacheKey());
            int i = this.x;
            int j = this.y;
            int k = this.font.lineHeight;
            int l = this.color;
            if (this.centered) {
                multiLineLabel.renderCentered(matrices, i + this.getWidth() / 2, j, k, l);
            } else {
                multiLineLabel.renderLeftAligned(matrices, i, j, k, l);
            }
        }

        public FiguraMuliLineTextWidget setColor(int i) {
            this.color = i;
            return this;
        }

        public FiguraMuliLineTextWidget setMaxWidth(int width) {
            this.maxWidth = OptionalInt.of(width);
            return this;
        }

        public FiguraMuliLineTextWidget setMaxRows(int rows) {
            this.maxRows = OptionalInt.of(rows);
            return this;
        }

        public FiguraMuliLineTextWidget setCentered(boolean centered) {
            this.centered = centered;
            return this;
        }

        @Override
        public int getWidth() {
            return multiLineWidth;
        }

        private CacheKey getFreshCacheKey() {
            return new CacheKey(this.getMessage(), this.maxWidth.orElse(Integer.MAX_VALUE), this.maxRows);
        }


        @Override
        public int getHeight() {
            return this.cache.getValue(this.getFreshCacheKey()).getLineCount() * this.font.lineHeight;
        }

        public class SingleKeyCache<K, V> {
            private final Function<K, V> computeValue;
            @Nullable
            private K cacheKey = null;
            @Nullable
            private V cachedValue;

            public SingleKeyCache(Function<K, V> mapper) {
                this.computeValue = mapper;
            }

            public V getValue(K key) {
                if (this.cachedValue == null || !Objects.equals(this.cacheKey, key)) {
                    this.cachedValue = this.computeValue.apply(key);
                    this.cacheKey = key;
                }
                return this.cachedValue;
            }
        }
        @Environment(value= EnvType.CLIENT)
        record CacheKey(Component message, int maxWidth, OptionalInt maxRows) {
        }

        @Override
        public void updateNarration(NarrationElementOutput builder) {

        }
    }
}
