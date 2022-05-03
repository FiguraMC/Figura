package org.moon.figura.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.gui.screens.*;

import java.util.ArrayList;
import java.util.function.Supplier;

public class PanelSelectorWidget extends AbstractContainerElement {

    public PanelSelectorWidget(Screen parentScreen, int x, int y, int width, int selected) {
        super(x, y, width, 20);

        //buttons
        ArrayList<SwitchButton> buttons = new ArrayList<>();
        createPanelButton(buttons, () -> new ProfileScreen(parentScreen), ProfileScreen.TITLE, width, y);
        createPanelButton(buttons, () -> new BrowserScreen(parentScreen), BrowserScreen.TITLE, width, y);
        createPanelButton(buttons, () -> new WardrobeScreen(parentScreen), WardrobeScreen.TITLE, width, y);
        createPanelButton(buttons, () -> new TrustScreen(parentScreen), TrustScreen.TITLE, width, y);
        createPanelButton(buttons, () -> new ConfigScreen(parentScreen), ConfigScreen.TITLE, width, y);

        //selected button
        buttons.get(selected).setToggled(true);

        //TODO - remove this when we actually implement those panels
        for (int i = 0; i < 2; i++) {
            SwitchButton button = buttons.get(i);
            button.setTooltip(new TextComponent("Not yet ❤"));
            button.active = false;
        }
    }

    private void createPanelButton(ArrayList<SwitchButton> list, Supplier<AbstractPanelScreen> screenSupplier, Component title, int screenWidth, int y) {
        //create button
        SwitchButton button = new SwitchButton(screenWidth / 2 - 176 + 72 * list.size(), y + 4, 60, 20, title, null, bx -> Minecraft.getInstance().setScreen(screenSupplier.get()));
        button.shouldHaveBackground(false);

        //add button
        list.add(button);
        children.add(button);
    }
}
