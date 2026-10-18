package org.figuramc.figura.utils.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.figuramc.figura.utils.FiguraModMetadata;
import org.figuramc.figura.utils.Version;

public class FiguraModMetadataImpl extends FiguraModMetadata {
    private final IModInfo modInfo;
    protected FiguraModMetadataImpl(String modID) {
        super(modID);
        // Debug screen initialization can access Figura before Forge creates mod containers.
        this.modInfo = ModList.getMods().stream().filter(info -> info.getModId().equals(modID))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown mod: " + modID));
    }

    public static FiguraModMetadata getMetadataForMod(String modID) {
        return new FiguraModMetadataImpl(modID);
    }

    @Override
    public String getCustomValueAsString(String key) {
        return modInfo.getModProperties().get(key).toString();
    }

    @Override
    public Number getCustomValueAsNumber(String key) {
        return (Number) modInfo.getModProperties().get(key);
    }

    @Override
    public Boolean getCustomValueAsBoolean(String key) {
        return (Boolean) modInfo.getModProperties().get(key);
    }

    @Override
    public Object getCustomValueAsObject(String key) {
        return modInfo.getModProperties().get(key);
    }


    @Override
    public Version getModVersion() {
        return new Version(modInfo.getVersion().toString());
    }


}
