package org.moon.figura.lua.api.tasks;

import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.PartCustomization;
import org.moon.figura.lua.LuaWhitelist;

@LuaWhitelist
public abstract class RenderTask<T> {

    protected T target;
    protected FiguraModelPart modelPart;

    public abstract void render(PartCustomization.Stack customizationStack);

}
