package org.moon.figura.gui.screens;

import net.minecraft.network.chat.Component;
import org.moon.figura.gui.widgets.InteractableEntity;
import org.moon.figura.utils.FiguraText;

public class AvatarScreen extends AbstractPanelScreen {

    public static final Component TITLE = FiguraText.of("gui.panels.title.avatar");
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
    }
}
