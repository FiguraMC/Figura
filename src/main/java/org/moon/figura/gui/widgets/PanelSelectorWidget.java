package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.screens.*;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PanelSelectorWidget extends AbstractContainerElement {

    private static final List<Function<Screen, AbstractPanelScreen>> PANELS = List.of(
            ProfileScreen::new,
            BrowserScreen::new,
            WardrobeScreen::new,
            PermissionsScreen::new,
            ConfigScreen::new,
            DocsScreen::new
    );

    //TODO - remove this when we actually implement those panels
    private static final List<Integer> PANELS_BLACKLIST = List.of(0, 1, 5);

    private final List<SwitchButton> buttons = new ArrayList<>();

    private SwitchButton selected;

    public PanelSelectorWidget(Screen parentScreen, int x, int y, int width, Class<? extends AbstractPanelScreen> selected) {
        super(x, y, width, 28);

        //buttons
        for (Function<Screen, AbstractPanelScreen> func : PANELS) {
            AbstractPanelScreen panel = func.apply(parentScreen);
            createPanelButton(panel, panel.getClass() == selected);
        }

        for (int i : PANELS_BLACKLIST) {
            SwitchButton button = buttons.get(i);
            button.setMessage(button.getMessage().copy().withStyle(ChatFormatting.RED));
            button.setTooltip(new TextComponent("Not yet ❤"));
            button.active = FiguraMod.DEBUG_MODE;
        }
    }

    private void createPanelButton(AbstractPanelScreen panel, boolean toggled) {
        //create button
        SwitchButton button = new SwitchButton(width / 2 - 76 * PANELS.size() / 2 + 6 + 76 * buttons.size(), y + 4, 64, 20, panel.getTitle(), null, bx -> Minecraft.getInstance().setScreen(panel));
        button.shouldHaveBackground(false);
        button.setUnderline(false);
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
        int x = selected.x;
        int width = selected.getWidth();

        //background

        //left
        UIHelper.renderTexture(stack, 0, 0, x, 24, UIHelper.FILL);
        //center
        UIHelper.renderTexture(stack, x, 0, width, 1, UIHelper.FILL);
        //right
        UIHelper.renderTexture(stack, x + width, 0, this.width, 24, UIHelper.FILL);

        //buttons
        super.render(stack, mouseX, mouseY, delta);

        //lines

        //left
        UIHelper.fill(stack, 1, 23, x + 1, 24, 0xFF404040);
        //left up
        UIHelper.fill(stack, x, 2, x + 1, 23, 0xFFFFFFFF);

        //center up
        UIHelper.fill(stack, x + 1, 1, x + width - 1, 2, 0xFFFFFFFF);
        //center down
        UIHelper.fill(stack, x + width / 3, 22, x + (int) (width / 1.5), 23, 0xFFFFFFFF);

        //right up
        UIHelper.fill(stack, x + width - 1, 2, x + width, 23, 0xFFFFFFFF);
        //right
        UIHelper.fill(stack, x + width - 1, 23, x + width + this.width, 24, 0xFF404040);
    }

    public boolean cycleTab(int keyCode) {
        if (Screen.hasControlDown()) {
            int index = this.getNextPanel(keyCode);
            if (index != -1) {
                SwitchButton button = buttons.get(index);
                button.run();
                return true;
            }
        }

        return false;
    }

    private int getNextPanel(int keyCode) {
        //numbers
        if (keyCode >= 49 && keyCode <= 57) {
            int panel = keyCode - 49;
            return PANELS_BLACKLIST.contains(panel) ? -1 : panel;
        }

        //tab
        if (keyCode == 258) {
            //get current button
            int index = buttons.indexOf(selected);

            int i = Screen.hasShiftDown() ? index - 1 : index + 1;
            while (true) {
                i = Math.floorMod(i, buttons.size());
                if (!PANELS_BLACKLIST.contains(i))
                    return i;
                i = Screen.hasShiftDown() ? i - 1 : i + 1;
            }
        }

        return -1;
    }
}
