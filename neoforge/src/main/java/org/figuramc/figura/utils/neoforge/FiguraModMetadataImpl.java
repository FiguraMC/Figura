package org.figuramc.figura.utils.neoforge;


import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import org.figuramc.figura.utils.FiguraModMetadata;
import org.figuramc.figura.utils.Version;

public class FiguraModMetadataImpl extends FiguraModMetadata {
    private final IModInfo modInfo;
    protected FiguraModMetadataImpl(String modID) {
        super(modID);
        this.modInfo = ModList.get().getModContainerById(modID).get().getModInfo();
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
