package org.moon.figura.gui.screens;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.gui.widgets.InteractableEntity;
import org.moon.figura.utils.FiguraText;

public class AvatarScreen extends AbstractPanelScreen {

    public static final Component TITLE = FiguraText.of("gui.panels.title.avatar");
    private final int scale;
    private final float pitch;
    private final float yaw;
    private final LivingEntity entity;

    public AvatarScreen(int scale, float pitch, float yaw, LivingEntity entity, AbstractPanelScreen parentScreen) {
        super(parentScreen, TITLE, parentScreen.index);
        this.scale = scale;
        this.pitch = pitch;
        this.yaw = yaw;
        this.entity = entity;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    protected void init() {
        super.init();
        //entity
        InteractableEntity widget = new InteractableEntity(0, 0, width, height, scale, pitch, yaw, entity, (AbstractPanelScreen) parentScreen);
        widget.setToggled(true);
        addRenderableWidget(widget);
        removeWidget(panels); //no panels :p
    }
}
