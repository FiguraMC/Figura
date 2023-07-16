package org.figuramc.figura.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.function.Consumer;

public class SearchBar extends TextField {

    public static final ResourceLocation CLEAR_TEXTURE = new FiguraIdentifier("textures/gui/search_clear.png");
    public static final Component SEARCH_ICON = Component.literal("\uD83D\uDD0E").withStyle(Style.EMPTY.withFont(UIHelper.UI_FONT).applyFormats(ChatFormatting.DARK_GRAY));

    private final Button clearButton;

    public SearchBar(int x, int y, int width, int height, Consumer<String> changedListener) {
        super(x, y, width, height, TextField.HintType.SEARCH, changedListener);
        clearButton = new Button(getX() + getWidth() - 18, getY() + ((getHeight() - 16) / 2), 16, 16, 0, 0, 16, CLEAR_TEXTURE, 48, 16, FiguraText.of("gui.clear"), button -> {
            getField().setValue("");
            setFocused(false);
        });
        children.add(clearButton);
        getField().setWidth(getField().getWidth() - 16);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        clearButton.setVisible(!getField().getValue().isEmpty());
        super.render(gui, mouseX, mouseY, delta);
    }

    @Override
    protected void renderHint(GuiGraphics gui) {
        super.renderHint(gui);
        Font font = Minecraft.getInstance().font;
        gui.drawString(font, SEARCH_ICON, getX() + getWidth() - font.width(SEARCH_ICON) - 4, getY() + (int) ((getHeight() - font.lineHeight + 1) / 2f), 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return (!clearButton.isVisible() || !clearButton.mouseClicked(mouseX, mouseY, button)) && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return (!clearButton.isVisible() || !clearButton.isMouseOver(mouseX, mouseY)) && super.isMouseOver(mouseX, mouseY);
    }
}
