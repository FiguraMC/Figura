package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.FiguraMod;
import org.moon.figura.entries.FiguraScreen;
import org.moon.figura.gui.screens.*;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class PanelSelectorWidget extends AbstractContainerElement {

    private static final ResourceLocation OVERLAY = new FiguraIdentifier("textures/gui/panels_overlay.png");

    private static final List<Function<Screen, Pair<Screen, PanelIcon>>> PANELS = new ArrayList<>() {{
                add(s -> Pair.of(new ProfileScreen(s), PanelIcon.PROFILE));
                add(s -> Pair.of(new BrowserScreen(s), PanelIcon.BROWSER));
                add(s -> Pair.of(new WardrobeScreen(s), PanelIcon.WARDROBE));
                add(s -> Pair.of(new PermissionsScreen(s), PanelIcon.PERMISSIONS));
                add(s -> Pair.of(new DocsScreen(s), PanelIcon.DOCS));
                add(s -> Pair.of(new ConfigScreen(s), PanelIcon.SETTINGS));
    }};

    //TODO - remove this when we actually implement those panels
    private static final List<Integer> PANELS_BLACKLIST = List.of(0, 1, 4);

    private final List<PanelButton> buttons = new ArrayList<>();

    private PanelButton selected;

    public PanelSelectorWidget(Screen parentScreen, int x, int y, int width, Class<? extends Screen> selected) {
        super(x, y, width, 28);

        //buttons

        //size variables
        int buttonCount = PANELS.size() - (FiguraMod.DEBUG_MODE ? 0 : PANELS_BLACKLIST.size());
        int buttonWidth = Math.min(Math.max((width - 4) / buttonCount - 4, 24), 96) + 4;
        int spacing = (width - (4 + buttonWidth * buttonCount)) / 2;

        for (int i = 0; i < PANELS.size(); i++) {
            //skip blacklist
            if (!FiguraMod.DEBUG_MODE && PANELS_BLACKLIST.contains(i))
                continue;

            //get button data
            Pair<Screen, PanelIcon> panel = PANELS.get(i).apply(parentScreen);
            Screen s = panel.getFirst();
            PanelIcon icon = panel.getSecond();
            int buttonX = 4 + buttonWidth * buttons.size() + spacing;

            //create button
            createPanelButton(s, icon, s.getClass() == selected, buttonX, buttonWidth - 4);
        }

        //locked buttons
        if (FiguraMod.DEBUG_MODE) {
            for (int i : PANELS_BLACKLIST) {
                SwitchButton button = buttons.get(i);
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
        //create button
        PanelButton button = new PanelButton(x, y + 4, width, height - 8, panel.getTitle(), icon, bx -> Minecraft.getInstance().setScreen(panel));
        button.shouldHaveBackground(false);
        button.setToggled(toggled);
        if (toggled) {
            this.selected = button;
            button.y = 1;
            button.setHeight(23);
        }

        //add button
        buttons.add(button);
        children.add(button);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //selected overlay
        if (selected != null)
            renderSelectedOverlay(stack);

        //buttons
        UIHelper.setupScissor(0, 0, this.width, 23);
        super.render(stack, mouseX, mouseY, delta);
        UIHelper.disableScissor();
    }

    private void renderSelectedOverlay(PoseStack stack) {
        int x = selected.x;
        int width = selected.getWidth();
        int left = x + width;
        int right = this.width - left;

        //center
        UIHelper.renderSliced(stack, x, 0, width, 24, 24f, 0f, 24, 24, 48, 24, OVERLAY);
        //left
        UIHelper.blit(stack, 0, 0, x, 24, 0f, 0f, 24, 24, 48, 24);
        //right
        UIHelper.blit(stack, left, 0, right, 24, 0f, 0f, 24, 24, 48, 24);
    }

    public boolean cycleTab(int keyCode) {
        if (Screen.hasControlDown()) {
            int i = this.getNextPanel(keyCode);
            if (i >= 0 && i < buttons.size()) {
                SwitchButton button = buttons.get(i);
                button.run();
                return true;
            }
        }

        return false;
    }

    private int getNextPanel(int keyCode) {
        //numbers
        if (keyCode >= 49 && keyCode <= 57)
            return keyCode - 49;

        //tab
        if (keyCode == 258) {
            //get current button
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
        DOCS(4),
        SETTINGS(5),
        OTHER(6);

        public final int uv;

        PanelIcon(int uv) {
            this.uv = uv;
        }
    }

    private static class PanelButton extends SwitchButton {

        public static final ResourceLocation ICONS = new FiguraIdentifier("textures/gui/panels.png");
        private final PanelIcon icon;

        public PanelButton(int x, int y, int width, int height, Component text, PanelIcon icon, OnPress pressAction) {
            super(x, y, width, height, text, null, pressAction);
            this.icon = icon;
        }

        @Override
        public void renderButton(PoseStack stack, int mouseX, int mouseY, float delta) {
            super.renderButton(stack, mouseX, mouseY, delta);

            boolean iconOnly = iconsOnly();

            UIHelper.setupTexture(ICONS);
            blit(stack, x + (!iconOnly ? 2 : getWidth() / 2 - 10), y + getHeight() / 2 - 10, 20, 20, 20f * icon.uv, 0f, 20, 20, 140, 20);

            if (iconOnly && this.isMouseOver(mouseX, mouseY))
                UIHelper.setTooltip(getMessage());
        }

        @Override
        protected void renderText(PoseStack stack, float delta) {
            if (iconsOnly())
                return;

            int x = this.x;
            int width = getWidth();
            this.x = x + 22;
            setWidth(width - 22);
            super.renderText(stack, delta);
            this.x = x;
            setWidth(width);
        }

        private boolean iconsOnly() {
            return getWidth() < 72;
        }
    }
}
