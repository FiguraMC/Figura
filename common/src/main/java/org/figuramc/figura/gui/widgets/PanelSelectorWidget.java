package org.figuramc.figura.gui.widgets;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.entries.FiguraScreen;
import org.figuramc.figura.gui.screens.*;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class PanelSelectorWidget extends AbstractContainerElement {

    public static final ResourceLocation BACKGROUND = new FiguraIdentifier("textures/gui/panels_background.png");

    private static final List<Function<Screen, Pair<Screen, PanelIcon>>> PANELS = new ArrayList<>() {{
                add(s -> Pair.of(new ProfileScreen(s), PanelIcon.PROFILE));
                add(s -> Pair.of(new BrowserScreen(s), PanelIcon.BROWSER));
                add(s -> Pair.of(new WardrobeScreen(s), PanelIcon.WARDROBE));
                add(s -> Pair.of(new PermissionsScreen(s), PanelIcon.PERMISSIONS));
                add(s -> Pair.of(new ConfigScreen(s), PanelIcon.SETTINGS));
                add(s -> Pair.of(new HelpScreen(s), PanelIcon.HELP));
    }};

    // TODO - remove this when we actually implement those panels
    private static final List<Integer> PANELS_BLACKLIST = List.of(0, 1);

    private final List<PanelButton> buttons = new ArrayList<>();

    private PanelButton selected;

    public PanelSelectorWidget(Screen parentScreen, int x, int y, int width, Class<? extends Screen> selected) {
        super(x, y, width, 28);

        // buttons

        // size variables
        int buttonCount = PANELS.size() - (FiguraMod.debugModeEnabled() ? 0 : PANELS_BLACKLIST.size());
        int buttonWidth = Math.min(Math.max((width - 4) / buttonCount - 4, 24), 96) + 4;
        int spacing = (width - (4 + buttonWidth * buttonCount)) / 2;

        for (int i = 0; i < PANELS.size(); i++) {
            // skip blacklist
            if (!FiguraMod.debugModeEnabled() && PANELS_BLACKLIST.contains(i))
                continue;

            // get button data
            Pair<Screen, PanelIcon> panel = PANELS.get(i).apply(parentScreen);
            Screen s = panel.getFirst();
            PanelIcon icon = panel.getSecond();
            int buttonX = 4 + buttonWidth * buttons.size() + spacing;

            // create button
            createPanelButton(s, icon, s.getClass() == selected, buttonX, buttonWidth - 4);
        }

        // locked buttons
        if (FiguraMod.debugModeEnabled()) {
            for (int i : PANELS_BLACKLIST) {
                PanelButton button = buttons.get(i);
                button.setMessage(button.getMessage().copy().withStyle(ChatFormatting.RED));
            }
        }
    }

    public static void initEntryPoints(Set<FiguraScreen> set) {
        for (FiguraScreen figuraScreen : set) {
            PanelIcon icon = figuraScreen.getPanelIcon();
            PANELS.add(s -> Pair.of(figuraScreen.getScreen(s), icon == null ? PanelIcon.OTHER : icon));
        }
    }

    private void createPanelButton(Screen panel, PanelIcon icon, boolean toggled, int x, int width) {
        // create button
        PanelButton button = new PanelButton(x, getY(), width, getHeight() - 4, panel.getTitle(), icon, this, bx -> Minecraft.getInstance().setScreen(panel));
        button.shouldHaveBackground(false);
        if (toggled) this.selected = button;

        // add button
        buttons.add(button);
        children.add(button);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        UIHelper.blitSliced(gui, getX(), getY(), selected.getX() - getX(), getHeight() - 4, BACKGROUND);
        UIHelper.blitSliced(gui, selected.getX() + selected.getWidth(), getY(), getWidth() - selected.getX() - selected.getWidth(), getHeight() - 4, BACKGROUND);
        super.render(gui, mouseX, mouseY, delta);
    }

    public boolean cycleTab(int keyCode) {
        if (Screen.hasControlDown()) {
            int i = this.getNextPanel(keyCode);
            if (i >= 0 && i < buttons.size()) {
                PanelButton button = buttons.get(i);
                button.run();
                return true;
            }
        }

        return false;
    }

    private int getNextPanel(int keyCode) {
        // numbers
        if (keyCode >= 49 && keyCode <= 57)
            return keyCode - 49;

        // tab
        if (keyCode == 258) {
            // get current button
            int index = buttons.indexOf(selected);

            int i = Screen.hasShiftDown() ? index - 1 : index + 1;
            return Math.floorMod(i, buttons.size());
        }

        return -1;
    }

    public enum PanelIcon {
        PROFILE(0),
        BROWSER(1),
        WARDROBE(2),
        PERMISSIONS(3),
        SETTINGS(4),
        HELP(5),
        OTHER(6);

        public final int uv;

        PanelIcon(int uv) {
            this.uv = uv;
        }
    }

    private static class PanelButton extends IconButton {

        public static final ResourceLocation TEXTURE = new FiguraIdentifier("textures/gui/panels_button.png");
        public static final ResourceLocation ICONS = new FiguraIdentifier("textures/gui/panels.png");

        private final PanelSelectorWidget parent;

        public PanelButton(int x, int y, int width, int height, Component text, PanelIcon icon, PanelSelectorWidget parent, OnPress pressAction) {
            super(x, y, width, height, 20 * icon.uv, 0, 20, ICONS, 140, 20, text, null, pressAction);
            this.parent = parent;
        }

        @Override
        public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float delta) {
            super.renderWidget(gui, mouseX, mouseY, delta);
            boolean iconOnly = iconsOnly();

            if (iconOnly && this.isMouseOver(mouseX, mouseY))
                UIHelper.setTooltip(getMessage());
        }

        @Override
        protected void renderTexture(GuiGraphics gui, float delta) {
            UIHelper.blitSliced(gui, getX(), getY(), getWidth(), getHeight(), isSelected() ? 24f : 0f, this.isHoveredOrFocused() ? 24f : 0f, 24, 24, 48, 48, TEXTURE);

            UIHelper.enableBlend();
            int size = getTextureSize();
            gui.blit(texture, getX() + (iconsOnly() ? (getWidth() - size) / 2 : 2), getY() + (getHeight() - size) / 2 + (!isSelected() ? 2 : 0), size, size, u, v, regionSize, regionSize, textureWidth, textureHeight);
        }

        @Override
        protected void renderText(GuiGraphics gui, float delta) {
            if (iconsOnly())
                return;

            int size = getTextureSize();
            int offset = !isSelected() ? 3 : 0;
            Component message = isSelected() ? getMessage().copy().withStyle(ChatFormatting.UNDERLINE) : getMessage();
            UIHelper.renderCenteredScrollingText(gui, message, getX() + 4 + size, getY() + offset, getWidth() - 6 - size, getHeight(), getTextColor());
        }

        private boolean iconsOnly() {
            return getWidth() < 72;
        }

        private boolean isSelected() {
            return parent.selected == this;
        }
    }
}
