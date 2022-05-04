package org.moon.figura.gui.screens;

import net.minecraft.network.chat.Component;
import org.moon.figura.gui.widgets.InteractableEntity;
import org.moon.figura.gui.widgets.PanelSelectorWidget;
import org.moon.figura.utils.FiguraText;

public class AvatarScreen extends AbstractPanelScreen {

    public static final Component TITLE = new FiguraText("gui.panels.title.avatar");
    private final InteractableEntity entityWidget;

    public AvatarScreen(AbstractPanelScreen parentScreen, InteractableEntity entityWidget) {
        super(parentScreen, TITLE, parentScreen.index);
        this.entityWidget = entityWidget;
    }

    @Override
    protected void init() {
        //entity
        entityWidget.x = 0;
        entityWidget.y = 0;
        entityWidget.width = width;
        entityWidget.height = height;
        this.addRenderableWidget(entityWidget);

        //do not call super here because we want the parent of the parent screen and we want it to be added last
        this.addRenderableWidget(panels = new PanelSelectorWidget(((AbstractPanelScreen) parentScreen).parentScreen, 0, 0, width, index));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //check panels first >~<
        return panels.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }
}
