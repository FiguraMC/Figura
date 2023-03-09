package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.screens.*;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PanelSelectorWidget extends AbstractContainerElement {

    private static final ResourceLocation OVERLAY = new FiguraIdentifier("textures/gui/panels_overlay.png");

    private static final List<Function<Screen, AbstractPanelScreen>> PANELS = List.of(
            ProfileScreen::new,
            BrowserScreen::new,
            WardrobeScreen::new,
            PermissionsScreen::new,
            DocsScreen::new,
            ConfigScreen::new
    );

    //TODO - remove this when we actually implement those panels
    private static final List<Integer> PANELS_BLACKLIST = List.of(0, 1, 4);

    private final List<SwitchButton> buttons = new ArrayList<>();

    private SwitchButton selected;

    public PanelSelectorWidget(Screen parentScreen, int x, int y, int width, Class<? extends AbstractPanelScreen> selected) {
        super(x, y, width, 28);

        //buttons
        for (int i = 0; i < PANELS.size(); i++) {
            if (!FiguraMod.DEBUG_MODE && PANELS_BLACKLIST.contains(i))
                continue;

            AbstractPanelScreen panel = PANELS.get(i).apply(parentScreen);
            createPanelButton(panel, panel.getClass() == selected);
        }

        if (FiguraMod.DEBUG_MODE) {
            for (int i : PANELS_BLACKLIST) {
                SwitchButton button = buttons.get(i);
                button.setMessage(button.getMessage().copy().withStyle(ChatFormatting.RED));
            }
        }
    }

    private void createPanelButton(AbstractPanelScreen panel, boolean toggled) {
        //create button
        int size = PANELS.size() - (FiguraMod.DEBUG_MODE ? 0 : PANELS_BLACKLIST.size());
        SwitchButton button = new SwitchButton(width / 2 - 76 * size / 2 + 6 + 76 * buttons.size(), y + 4, 64, height - 8, panel.getTitle(), null, bx -> Minecraft.getInstance().setScreen(panel));
        button.shouldHaveBackground(false);
        button.setUnderline(false);
        button.setToggled(toggled);
        if (toggled) {
            this.selected = button;
            button.setY(1);
            button.setHeight(23);
        }

        //add button
        buttons.add(button);
        children.add(button);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //selected overlay

        int x = selected.getX();
        int width = selected.getWidth();
        int left = x + width;
        int right = this.width - left;

        //left
        UIHelper.renderSliced(stack, 0, 0, x, 24, 0f, 0f, 16, 16, 32, 16, OVERLAY);
        //center
        UIHelper.renderSliced(stack, x, 0, width, 24, 16f, 0f, 16, 16, 32, 16, OVERLAY);
        //right
        UIHelper.renderSliced(stack, left, 0, right, 24, 0f, 0f, 16, 16, 32, 16, OVERLAY);

        //buttons
        UIHelper.setupScissor(0, 0, this.width, 23);
        super.render(stack, mouseX, mouseY, delta);
        UIHelper.disableScissor();
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
}
