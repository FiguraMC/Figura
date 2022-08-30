package org.moon.figura.lua;

import org.moon.figura.avatars.Avatar;

public abstract class FiguraAPI {

    public abstract FiguraAPI build(Avatar avatar);

    public abstract String getName();
}
