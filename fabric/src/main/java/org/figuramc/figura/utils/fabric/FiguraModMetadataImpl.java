package org.figuramc.figura.utils.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.figuramc.figura.utils.FiguraModMetadata;
import org.figuramc.figura.utils.Version;

public class FiguraModMetadataImpl extends FiguraModMetadata {
    private final ModMetadata fabricMetada;
    protected FiguraModMetadataImpl(String modID) {
        super(modID);
        this.fabricMetada = FabricLoader.getInstance().getModContainer(modID).get().getMetadata();
    }

    public static FiguraModMetadata getMetadataForMod(String modID) {
        return new FiguraModMetadataImpl(modID);
    }

    @Override
    public String getCustomValueAsString(String key) {
        return fabricMetada.getCustomValue(key).getAsString();
    }

    @Override
    public Number getCustomValueAsNumber(String key) {
        return fabricMetada.getCustomValue(key).getAsNumber();
    }

    @Override
    public Boolean getCustomValueAsBoolean(String key) {
        return fabricMetada.getCustomValue(key).getAsBoolean();
    }

    @Override
    public Object getCustomValueAsObject(String key) {
        return fabricMetada.getCustomValue(key).getAsObject();
    }


    @Override
    public Version getModVersion() {
        return new Version(fabricMetada.getVersion().getFriendlyString());
    }
}
