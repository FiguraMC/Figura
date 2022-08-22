package org.moon.figura.lua.api.config;

import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;

public class ConfigAPI {

    private final Avatar owner;
    private final boolean isHost;

    public ConfigAPI(Avatar owner) {
        this.owner = owner;
        this.isHost = FiguraMod.isLocal(this.owner.owner);
    }

    
}
