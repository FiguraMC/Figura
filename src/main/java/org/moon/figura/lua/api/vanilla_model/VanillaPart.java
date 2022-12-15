package org.moon.figura.lua.api.vanilla_model;

import net.minecraft.client.model.EntityModel;
import org.moon.figura.avatar.Avatar;

public abstract class VanillaPart {

    protected final String name;
    protected final Avatar owner;

    public VanillaPart(Avatar owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public abstract void alter(EntityModel<?> model);
    public abstract void store(EntityModel<?> model);
    public abstract void restore(EntityModel<?> model);
    public abstract boolean getVisible();
    public abstract void setVisible(boolean visible);
}
