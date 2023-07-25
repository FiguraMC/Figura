package org.figuramc.figura.gui.widgets.lists;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.gui.widgets.*;
import org.figuramc.figura.lua.api.keybind.FiguraKeybind;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class KeybindList extends AbstractList {

    private final List<KeybindElement> keybinds = new ArrayList<>();
    private final Avatar owner;
    private final Button resetAllButton;

    private FiguraKeybind focusedKeybind;

    public KeybindList(int x, int y, int width, int height, Avatar owner, Button resetAllButton) {
        super(x, y, width, height);
        this.owner = owner;
        this.resetAllButton = resetAllButton;
        updateList();

        Label noOwner, noKeys;
        this.children.add(noOwner = new Label(FiguraText.of("gui.error.no_avatar").withStyle(ChatFormatting.YELLOW), x + width / 2, y + height / 2, TextUtils.Alignment.CENTER, 0));
        this.children.add(noKeys = new Label(FiguraText.of("gui.error.no_keybinds").withStyle(ChatFormatting.YELLOW), x + width / 2, y + height / 2, TextUtils.Alignment.CENTER, 0));
        noOwner.centerVertically = noKeys.centerVertically = true;

        noOwner.setVisible(owner == null);
        noKeys.setVisible(!noOwner.isVisible() && keybinds.isEmpty());
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        // background and scissors
        UIHelper.blitSliced(gui, getX(), getY(), getWidth(), getHeight(), UIHelper.OUTLINE_FILL);
        enableScissors(gui);

        if (!keybinds.isEmpty())
            updateEntries();

        // children
        super.render(gui, mouseX, mouseY, delta);

        // reset scissor
        gui.disableScissor();
    }

    private void updateEntries() {
        // scrollbar
        int totalHeight = -4;
        for (KeybindElement keybind : keybinds)
            totalHeight += keybind.getHeight() + 8;
        int entryHeight = keybinds.isEmpty() ? 0 : totalHeight / keybinds.size();

        scrollBar.setVisible(totalHeight > getHeight());
        scrollBar.setScrollRatio(entryHeight, totalHeight - getHeight());

        // render list
        int xOffset = scrollBar.isVisible() ? 4 : 11;
        int yOffset = scrollBar.isVisible() ? (int) -(Mth.lerp(scrollBar.getScrollProgress(), -4, totalHeight - getHeight())) : 4;
        for (KeybindElement keybind : keybinds) {
            keybind.setX(getX() + xOffset);
            keybind.setY(getY() + yOffset);
            yOffset += keybind.getHeight() + 8;
        }
    }

    private void updateList() {
        // clear old widgets
        keybinds.forEach(children::remove);

        // add new keybinds
        if (owner == null || owner.luaRuntime == null)
            return;

        for (FiguraKeybind keybind : owner.luaRuntime.keybinds.keyBindings) {
            KeybindElement element = new KeybindElement(getWidth() - 22, keybind, this);
            keybinds.add(element);
            children.add(element);
        }

        updateBindings();
    }

    public boolean updateKey(InputConstants.Key key) {
        if (focusedKeybind == null)
            return false;

        focusedKeybind.setKey(key);
        focusedKeybind = null;
        FiguraMod.processingKeybind = false;

        updateBindings();
        return true;
    }

    public void updateBindings() {
        boolean active = false;

        for (KeybindElement keybind : keybinds) {
            keybind.updateText();
            if (!active && !keybind.keybind.isDefault())
                active = true;
        }

        resetAllButton.setActive(active);
    }

    private static class KeybindElement extends AbstractContainerElement {

        private final KeybindWidgetHelper helper = new KeybindWidgetHelper();
        private final FiguraKeybind keybind;
        private final KeybindList parent;
        private final Button resetButton;
        private final Button keybindButton;

        public KeybindElement(int width, FiguraKeybind keybind, KeybindList parent) {
            super(0, 0, width, 20);
            this.keybind = keybind;
            this.parent = parent;

            // toggle button
            children.add(0, keybindButton = new ParentedButton(0, 0, 90, 20, keybind.getTranslatedKeyMessage(), this, button -> {
                parent.focusedKeybind = keybind;
                FiguraMod.processingKeybind = true;
                updateText();
            }));

            // reset button
            children.add(resetButton = new ParentedButton(0, 0, 60, 20, Component.translatable("controls.reset"), this, button -> {
                keybind.resetDefaultKey();
                parent.updateBindings();
            }));
        }

        @Override
        public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
            if (!this.isVisible()) return;

            helper.renderConflictBars(gui, keybindButton.getX() - 8, keybindButton.getY() + 2, 4, 16);

            // vars
            Font font = Minecraft.getInstance().font;
            int textY = getY() + getHeight() / 2 - font.lineHeight / 2;

            // hovered arrow
            setHovered(isMouseOver(mouseX, mouseY));
            if (isHovered()) {
                gui.drawString(font, HOVERED_ARROW, getX() + 4, textY, 0xFFFFFF);
                if (keybindButton.isHoveredOrFocused())
                    helper.renderTooltip();
            }

            // render name
            gui.drawString(font, this.keybind.getName(), getX() + 16, textY, 0xFFFFFF);

            // render children
            super.render(gui, mouseX, mouseY, delta);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            resetButton.setX(x + getWidth() - 60);
            keybindButton.setX(x + getWidth() - 154);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            resetButton.setY(y);
            keybindButton.setY(y);
        }

        public void updateText() {
            // tooltip
            List<FiguraKeybind> temp = new ArrayList<>();
            for (KeybindElement keybind : parent.keybinds)
                temp.add(keybind.keybind);
            helper.setTooltip(this.keybind, temp);

            // reset enabled
            boolean isDefault = this.keybind.isDefault();
            this.resetButton.setActive(!isDefault);

            // text
            boolean selected = parent.focusedKeybind == this.keybind;
            Component text = helper.getText(isDefault, selected, this.keybind.getTranslatedKeyMessage());
            keybindButton.setMessage(text);
        }
    }
}
