
package org.figuramc.figura.ducks;

import org.figuramc.figura.model.rendering.EntityRenderMode;

import java.util.UUID;

public interface GuiEntityRenderStateExtension {

    double getXPos();
    void setXPos(double xPos);

    double getYPos();
    void setYPos(double yPos);

    EntityRenderMode getRenderMode();
    void setRenderMode(EntityRenderMode renderMode);
}
