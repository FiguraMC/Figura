package org.figuramc.figura.utils.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.fabricmc.loader.api.metadata.version.VersionInterval;
import org.figuramc.figura.utils.ModMetadataContainer;
import org.figuramc.figura.utils.PlatformUtils;
import org.figuramc.figura.utils.Version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModMetadataContainerImpl extends ModMetadataContainer {
    private final ModMetadata fabricMetadata;
    private static final boolean HAS_QUILT = PlatformUtils.isModLoaded("quilt_loader"); //separated to avoid quering for each getModMetadata call

    protected ModMetadataContainerImpl(String modID) {
        super(modID);
        this.fabricMetadata = FabricLoader.getInstance().getModContainer(modID).isPresent() ? FabricLoader.getInstance().getModContainer(modID).get().getMetadata() : null;
    }

    public static ModMetadataContainer getMetadataForMod(String modID) {
        return HAS_QUILT ? new ModMetadataContainerQuilt(modID) : new ModMetadataContainerImpl(modID);
    }

    @Override
    public String getCustomValueAsString(String key) {
        return fabricMetadata.getCustomValue(key).getAsString();
    }

    @Override
    public Number getCustomValueAsNumber(String key) {
        return fabricMetadata.getCustomValue(key).getAsNumber();
    }

    @Override
    public Boolean getCustomValueAsBoolean(String key) {
        return fabricMetadata.getCustomValue(key).getAsBoolean();
    }

    @Override
    public Object getCustomValueAsObject(String key) {
        return fabricMetadata.getCustomValue(key).getAsObject();
    }


    @Override
    public Version getModVersion() {
        return new Version(fabricMetadata.getVersion().getFriendlyString());
    }

    Map<String, Object> map = new HashMap<>();
    @Override
    public Map<String, Object> getKeyToObjectMap() {
        if (map.isEmpty()) {
            map.put("id", fabricMetadata.getId());
            map.put("name", fabricMetadata.getName());
            map.put("description", fabricMetadata.getDescription());
            map.put("contact_info", fabricMetadata.getContact().asMap());
            map.put("version", fabricMetadata.getVersion().getFriendlyString());
            map.put("icon", fabricMetadata.getIconPath(512).orElse(null));
            map.put("type", fabricMetadata.getType());
            map.put("licenses", fabricMetadata.getLicense());
            map.put("provides", fabricMetadata.getProvides());
            map.put("environment", fabricMetadata.getEnvironment().name().toLowerCase());
            {
                Map<String, Object> authors = new HashMap<>();
                for (Person author : fabricMetadata.getAuthors()) {
                    authors.put(author.getName(), author.getContact().asMap());
                }
                map.put("authors", authors);
            } {
                Map<String, Object> contributors = new HashMap<>();
                for (Person author : fabricMetadata.getContributors()) {
                    contributors.put(author.getName(), author.getContact().asMap());
                }
                map.put("contributors", contributors);
            } {
                Map<String, Object> values = new HashMap<>();
                for (Map.Entry<String, CustomValue> entry : fabricMetadata.getCustomValues().entrySet()) {
                    switch (entry.getValue().getType()) {
                        case BOOLEAN -> values.put(entry.getKey(), entry.getValue().getAsBoolean());
                        case STRING -> values.put(entry.getKey(), entry.getValue().getAsString());
                        case NUMBER -> values.put(entry.getKey(), entry.getValue().getAsNumber().doubleValue());
                    }
                }
                map.put("values", values);
            } {
                List<Map<String, Object>> dependencies = new ArrayList<>();
                for (ModDependency dependency : fabricMetadata.getDependencies()) {
                    Map<String, Object> dependencyData = new HashMap<>();
                    dependencyData.put("id", dependency.getModId());
                    dependencyData.put("kind", dependency.getKind().getKey());
                    {
                        List<String> versions = new ArrayList<>();
                        for(VersionInterval interval : dependency.getVersionIntervals()) {
                            versions.add(interval.toString());
                        }
                        dependencyData.put("versions", versions);
                    }
                    dependencies.add(dependencyData);
                }
                map.put("dependencies", dependencies);
            }
        }
        return map;
    }
}
