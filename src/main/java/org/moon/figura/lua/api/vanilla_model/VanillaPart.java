package org.moon.figura.lua.api.vanilla_model;

import net.minecraft.client.model.EntityModel;
import org.moon.figura.avatar.Avatar;

public abstract class VanillaPart {

    protected final String name;
    protected final Avatar owner;

    protected Boolean visible;

    public VanillaPart(Avatar owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public boolean checkVisible() {
        return visible == null || visible;
    }

    public abstract void change(EntityModel<?> model);
    public abstract void save(EntityModel<?> model);
    public abstract void restore(EntityModel<?> model);
    public abstract void transform(EntityModel<?> model);
    public abstract Boolean getVisible();
    public abstract VanillaPart setVisible(Boolean visible);
}
