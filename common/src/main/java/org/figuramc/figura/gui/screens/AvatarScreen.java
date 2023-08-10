package org.figuramc.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura.gui.widgets.EntityPreview;
import org.figuramc.figura.utils.FiguraText;

public class AvatarScreen extends AbstractPanelScreen {

    private final float scale;
    private final float pitch;
    private final float yaw;
    private final LivingEntity entity;

    public AvatarScreen(float scale, float pitch, float yaw, LivingEntity entity, Screen parentScreen) {
        super(parentScreen, FiguraText.of("gui.panels.title.avatar"));
        this.scale = scale;
        this.pitch = pitch;
        this.yaw = yaw;
        this.entity = entity;
    }

    @Override
    public Class<? extends Screen> getSelectedPanel() {
        return parentScreen.getClass();
    }

    @Override
    protected void init() {
        super.init();
        removeWidget(panels); // no panels :p

        // entity
        EntityPreview widget = new EntityPreview(0, 0, width, height, scale, pitch, yaw, entity, parentScreen);
        widget.setToggled(true);
        addRenderableWidget(widget);
    }
}
