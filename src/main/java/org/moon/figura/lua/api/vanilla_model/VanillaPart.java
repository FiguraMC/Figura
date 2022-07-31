package org.moon.figura.lua.api.vanilla_model;

import net.minecraft.client.model.EntityModel;

public abstract class VanillaPart {
    protected String name;
    public VanillaPart(String name) {
        this.name = name;
    }
    public abstract void alter(EntityModel<?> model);
    public abstract void store(EntityModel<?> model);
    public abstract void restore(EntityModel<?> model);
    public abstract Boolean getVisible();
    public abstract void setVisible(Boolean visible);
}
