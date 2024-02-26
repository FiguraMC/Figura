package org.figuramc.figura.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.Locale;
import java.util.function.Consumer;

public class TextField extends AbstractContainerElement {

    public static final ResourceLocation BACKGROUND = new FiguraIdentifier("textures/gui/text_field.png");
    public static final int ENABLED_COLOR = ChatFormatting.WHITE.getColor();
    public static final int DISABLED_COLOR = ChatFormatting.DARK_GRAY.getColor();

    private final HintType hint;
    private final EditBox field;
    private int borderColour = 0xFFFFFFFF;
    private boolean enabled = true;

    public TextField(int x, int y, int width, int height, HintType hint, Consumer<String> changedListener) {
        super(x, y, width, height);
        this.hint = hint;

        field = new EditBox(Minecraft.getInstance().font, x + 4, y + (height - 8) / 2, width - 12, height - (height - 8) / 2, Component.empty());
        field.setMaxLength(32767);
        field.setBordered(false);
        field.setResponder(changedListener);
        children.add(field);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        if (!isVisible()) return;

        // render background
        UIHelper.blitSliced(gui, getX(), getY(), getWidth(), getHeight(), !isEnabled() ? 0f : this.isMouseOver(mouseX, mouseY) ? 32f : 16f, 0f, 16, 16, 48, 16, BACKGROUND);

        // render outline
        if (isFocused())
            UIHelper.fillOutline(gui, getX(), getY(), getWidth(), getHeight(), borderColour);

        // hint text
        if (hint != null && field.getValue().isEmpty() && !field.isFocused())
            renderHint(gui);

        // children
        super.render(gui, mouseX, mouseY, delta);
    }

    protected void renderHint(GuiGraphics gui) {
        Font font = Minecraft.getInstance().font;
        gui.drawString(
                font, hint.hint.copy().append(TextUtils.ELLIPSIS).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
                getX() + 4, getY() + (int) ((getHeight() - font.lineHeight + 1) / 2f), 0xFFFFFF
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // mouse over check
        if (!isEnabled() || !this.isMouseOver(mouseX, mouseY))
            return false;

        // hacky
        mouseX = Mth.clamp(mouseX, field.getX(), field.getX() + field.getWidth() - 1);
        mouseY = Mth.clamp(mouseY, field.getY(), field.getY() + field.getHeight() - 1);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return !field.isFocused();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.field.setX(x + 4);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.field.setY(y + (this.getHeight() - 8) / 2);
    }

    public void setBorderColour(int borderColour) {
        this.borderColour = borderColour;
    }

    public int getBorderColour() {
        return borderColour;
    }

    public EditBox getField() {
        return field;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
        field.updateNarration(output);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return field.narrationPriority();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible == isVisible())
            return;

        super.setVisible(visible);
        this.field.setFocused(false);
    }

    public void setColor(int color) {
        this.field.setTextColor(enabled ? color : DISABLED_COLOR);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setColor(ENABLED_COLOR);
    }

    @Override
    public void setFocused(boolean bl) {
        this.field.setFocused(bl);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isFocused() {
        return isEnabled() && field.isFocused();
    }

    public enum HintType {
        ANY,
        INT,
        POSITIVE_INT,
        FLOAT,
        POSITIVE_FLOAT,
        HEX_COLOR,
        FOLDER_PATH,
        IP,
        SEARCH,
        NAME;

        private final Component hint;

        HintType() {
            this.hint = FiguraText.of("gui.text_hint." + this.name().toLowerCase(Locale.US));
        }
    }
}
