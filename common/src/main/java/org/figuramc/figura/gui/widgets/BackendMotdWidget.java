package org.figuramc.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.figuramc.figura.utils.ui.UIHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Function;

public class BackendMotdWidget extends AbstractScrollWidget {
    private final Font font;
    private final FiguraMuliLineTextWidget multilineWidget;

    public BackendMotdWidget(int i, int j, int k, int l, Component component, Font textRenderer) {
        super(i, j, k, l, component);
        this.font = textRenderer;
        this.multilineWidget = new FiguraMuliLineTextWidget(MultiLineLabel.create(textRenderer, component), textRenderer, component, true).setMaxWidth(this.getWidth() - this.totalInnerPadding());
    }

    public BackendMotdWidget setColor(int i) {
        this.multilineWidget.setColor(i);
        return this;
    }

    public void setWidth(int value) {
        super.setWidth(value);
        this.multilineWidget.setMaxWidth(this.getWidth() - this.totalInnerPadding());
    }

    protected int getInnerHeight() {
        return this.multilineWidget.getHeight();
    }

    @Override
    protected boolean scrollbarVisible() {
        return this.getInnerHeight() > this.getHeight();
    }

    protected double scrollRate() {
        Objects.requireNonNull(this.font);
        return 9.0;
    }

    protected void renderBorder(PoseStack stack, int x, int y, int width, int height) {
        UIHelper.renderSliced(stack, this.getX() - this.innerPadding(), this.getY() - this.innerPadding(), this.getWidth() + this.totalInnerPadding(), this.getHeight() + this.totalInnerPadding(), UIHelper.OUTLINE_FILL);
    }

    protected void renderBackground(PoseStack pose) {
        UIHelper.renderSliced(pose, this.getX() - this.innerPadding(), this.getY() - this.innerPadding(), this.getWidth() + this.totalInnerPadding(), this.getHeight() + this.totalInnerPadding(), UIHelper.OUTLINE_FILL);
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            if (!this.scrollbarVisible()) {
                this.renderBackground(pose);
                pose.pushPose();
                pose.translate((float)this.getX(), (float)this.getY(), 0.0F);
                this.multilineWidget.render(pose, mouseX, mouseY, delta);
                pose.popPose();
            } else {
                super.renderButton(pose, mouseX, mouseY, delta);
            }

        }
    }

    protected void renderContents(PoseStack pose, int mouseX, int mouseY, float delta) {
        pose.pushPose();
        pose.translate((float)(this.getX() + this.innerPadding()), (float)(this.getY() + this.innerPadding()), 0.0F);
        this.multilineWidget.render(pose, mouseX, mouseY, delta);
        pose.popPose();
    }

    protected void updateWidgetNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, this.getMessage());
    }

    public void setHeight(int height) {
        this.height = height;
    }

    protected static class FiguraMuliLineTextWidget extends MultiLineTextWidget {
        private int color = 0xFFFFFF;
        private OptionalInt maxWidth = OptionalInt.empty();
        private OptionalInt maxRows = OptionalInt.empty();
        private Font font;
        private final SingleKeyCache<CacheKey, MultiLineLabel> cache = new SingleKeyCache<>(key -> {
            if (key.maxRows.isPresent()) {
                return MultiLineLabel.create(font, key.message, key.maxWidth, key.maxRows.getAsInt());
            }
            return MultiLineLabel.create(font, key.message, key.maxWidth);
        });
        private boolean centered = false;
        public FiguraMuliLineTextWidget(MultiLineLabel multiLineLabel, Font textRenderer, Component component, boolean bl) {
            super(multiLineLabel, textRenderer, component, bl);
            this.font = textRenderer;
        }

        @Override
        public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
            MultiLineLabel multiLineLabel = this.cache.getValue(this.getFreshCacheKey());
            int i = this.getX();
            int j = this.getY();
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
            return this.cache.getValue(this.getFreshCacheKey()).getWidth();
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
    }
}
