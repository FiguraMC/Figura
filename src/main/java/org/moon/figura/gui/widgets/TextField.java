package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

import java.util.function.Consumer;

public class TextField extends AbstractContainerElement {

    private static final ResourceLocation BACKGROUND = new FiguraIdentifier("textures/gui/text_field.png");

    private final Component hint;
    private final EditBox field;
    private int borderColour = 0xFFFFFFFF;

    public TextField(int x, int y, int width, int height, Component hint, Consumer<String> changedListener) {
        super(x, y, width, height);
        this.hint = hint;

        field = new EditBox(Minecraft.getInstance().font, x + 4, y + (height - 8) / 2, width - 12, height - (height - 8) / 2, TextComponent.EMPTY.copy());
        field.setMaxLength(32767);
        field.setBordered(false);
        field.setResponder(changedListener);
        children.add(field);
    }

    @Override
    public void tick() {
        field.tick();
        super.tick();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!isVisible()) return;

        //render background
        UIHelper.renderSliced(stack, x, y, width, height, BACKGROUND);

        //render hovered background
        if (this.isMouseOver(mouseX, mouseY))
            UIHelper.fillRounded(stack, x, y, width, height, 0x60FFFFFF);

        //render outline
        if (field.isFocused())
            UIHelper.fillOutline(stack, x, y, width, height, borderColour);

        //hint text
        if (hint != null && field.getValue().isEmpty() && !field.isFocused()) {
            Minecraft.getInstance().font.drawShadow(
                    stack, hint.copy().withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
                    this.x + 4, this.y + (height - 8f) / 2f, 0xFFFFFF
            );
        }
        //input text
        else {
            field.renderButton(stack, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //mouse over check
        if (!this.isMouseOver(mouseX, mouseY))
            return false;

        //hacky
        mouseX = Mth.clamp(mouseX, field.x, field.x + field.getWidth() - 1);
        mouseY = Mth.clamp(mouseY, field.y, field.y + field.getHeight() - 1);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
        this.field.x = x + 4;
        this.field.y = y + (this.height - 8) / 2;
    }

    public void setBorderColour(int borderColour) {
        this.borderColour = borderColour;
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
        super.setVisible(visible);
        this.field.setFocus(false);
    }
}
