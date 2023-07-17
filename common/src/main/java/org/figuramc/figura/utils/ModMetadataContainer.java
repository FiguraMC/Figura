package org.figuramc.figura.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.Map;

public abstract class ModMetadataContainer {
    private final String modId;
    protected ModMetadataContainer(String modID) {
        this.modId = modID;
    }

    public abstract String getCustomValueAsString(String key);
    public abstract Number getCustomValueAsNumber(String key);
    public abstract Boolean getCustomValueAsBoolean(String key);
    public abstract Object getCustomValueAsObject(String key);

    public abstract Version getModVersion();
    public abstract Map<String, Object> getKeyToObjectMap();

    public String getModId() {
        return this.modId;
    }
    @ExpectPlatform
    public static ModMetadataContainer getMetadataForMod(String modID) {
        throw new AssertionError();
    }
}
