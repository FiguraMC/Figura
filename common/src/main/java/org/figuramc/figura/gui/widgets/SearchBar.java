package org.figuramc.figura.gui.widgets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.function.Consumer;

public class SearchBar extends TextField {

    public static final Identifier CLEAR_TEXTURE = new FiguraIdentifier("textures/gui/search_clear.png");
    public static final Component SEARCH_ICON = Component.literal("\uD83D\uDD0E").withStyle(Style.EMPTY.withFont(new FontDescription.Resource(UIHelper.UI_FONT)).applyFormats(ChatFormatting.DARK_GRAY));

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
    public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
        clearButton.setVisible(!getField().getValue().isEmpty());
        super.extractRenderState(gui, mouseX, mouseY, delta);
    }

    @Override
    protected void renderHint(GuiGraphicsExtractor gui) {
        super.renderHint(gui);
        Font font = Minecraft.getInstance().font;
        gui.text(font, SEARCH_ICON, getX() + getWidth() - font.width(SEARCH_ICON) - 4, getY() + (int) ((getHeight() - font.lineHeight + 1) / 2f), UIHelper.adjustColor(0xFFFFFF));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        return (!clearButton.isVisible() || !clearButton.mouseClicked(mouseButtonEvent, bl)) && super.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return (!clearButton.isVisible() || !clearButton.isMouseOver(mouseX, mouseY)) && super.isMouseOver(mouseX, mouseY);
    }
}
