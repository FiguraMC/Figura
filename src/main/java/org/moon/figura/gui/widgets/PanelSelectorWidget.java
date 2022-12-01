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
            TrustScreen::new,
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

        //TODO - remove this when we actually implement those panels {
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
}
