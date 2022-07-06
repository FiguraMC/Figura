package org.moon.figura.lua.api.model;

import net.minecraft.client.model.EntityModel;

public interface VanillaPart {
    void alter(EntityModel<?> model);
    void store(EntityModel<?> model);
    void restore(EntityModel<?> model);
    boolean isVisible();
    void setVisible(boolean visible);
}
