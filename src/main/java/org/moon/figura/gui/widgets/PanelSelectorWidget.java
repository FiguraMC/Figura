package org.moon.figura.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.screens.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PanelSelectorWidget extends AbstractContainerElement {

    private static final List<Function<Screen, AbstractPanelScreen>> PANELS = List.of(
            ProfileScreen::new,
            BrowserScreen::new,
            WardrobeScreen::new,
            PermissionsScreen::new,
            ConfigScreen::new
    );

    private final ArrayList<SwitchButton> buttons = new ArrayList<>();

    public PanelSelectorWidget(Screen parentScreen, int x, int y, int width, Class<? extends AbstractPanelScreen> selected) {
        super(x, y, width, 28);

        //buttons
        for (Function<Screen, AbstractPanelScreen> func : PANELS) {
            AbstractPanelScreen panel = func.apply(parentScreen);
            createPanelButton(panel, panel.getClass() == selected);
        }

        //TODO - remove this when we actually implement those panels
        if (FiguraMod.DEBUG_MODE)
            return;

        for (int i = 0; i < 2; i++) {
            SwitchButton button = buttons.get(i);
            button.setTooltip(new TextComponent("Not yet ❤"));
            button.active = false;
        }
        //TODO
    }

    private void createPanelButton(AbstractPanelScreen panel, boolean toggled) {
        //create button
        SwitchButton button = new SwitchButton(width / 2 - 72 * PANELS.size() / 2 + 6 + 72 * buttons.size(), y + 4, 60, 20, panel.getTitle(), null, bx -> Minecraft.getInstance().setScreen(panel));
        button.shouldHaveBackground(false);
        button.setToggled(toggled);

        //add button
        buttons.add(button);
        children.add(button);
    }

    public boolean cycleTab(int keyCode) {
        if (Screen.hasControlDown()) {
            int index = this.getNextPanel(keyCode);
            if (index != -1) {
                //TODO (same as above)
                int i = Math.max(FiguraMod.DEBUG_MODE ? 0 : 2, Math.min(index, buttons.size() - 1));
                SwitchButton button = buttons.get(i);
                button.playDownSound(Minecraft.getInstance().getSoundManager());
                button.onPress();
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
            int index = -1;
            for (int i = 0; i < buttons.size(); i++) {
                if (buttons.get(i).isToggled()) {
                    index = i;
                    break;
                }
            }

            //cycle
            if (index != -1) {
                int i = Screen.hasShiftDown() ? index - 1 : index + 1;
                return Math.floorMod(i, buttons.size());
            }
        }

        return -1;
    }
}
