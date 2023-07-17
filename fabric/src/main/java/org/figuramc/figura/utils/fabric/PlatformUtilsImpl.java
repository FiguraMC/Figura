package org.figuramc.figura.utils.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.fabricmc.loader.api.metadata.version.VersionInterval;
import org.figuramc.figura.FiguraMod;

import java.nio.file.Path;
import java.util.*;

public class PlatformUtilsImpl {
    private static final boolean HAS_QUILT = isModLoaded("quilt_loader"); //separated to avoid quering for each getModMetadata call
    public static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    public static String getModVersionString() {
        return FabricLoader.getInstance().getModContainer(FiguraMod.MOD_ID).get().getMetadata().getVersion().getFriendlyString();
    }

    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
    
    public static Map<String, Object> getModMetadata(String d){
        if (!isModLoaded(d)) return null;
        Map<String, Object> map = new HashMap<>();
        if (HAS_QUILT) {
            QuiltModMetadataGetter.fill(map, d);
            if(!map.isEmpty())
                return map;
        }
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(d);
        if (modContainer.isEmpty()) return null;
        ModMetadata metadata = modContainer.get().getMetadata();
        map.put("id", metadata.getId());
        map.put("name", metadata.getName());
        map.put("description", metadata.getDescription());
        map.put("contact_info", metadata.getContact().asMap());
        map.put("version", metadata.getVersion().getFriendlyString());
        map.put("icon", metadata.getIconPath(512).orElse(null));
        map.put("type", metadata.getType());
        map.put("licenses", metadata.getLicense());
        map.put("provides", metadata.getProvides());
        map.put("environment", metadata.getEnvironment().name().toLowerCase());
        {
            Map<String, Object> authors = new HashMap<>();
            for (Person author : metadata.getAuthors()) {
                authors.put(author.getName(), author.getContact().asMap());
            }
            map.put("authors", authors);
        } {
            Map<String, Object> contributors = new HashMap<>();
            for (Person author : metadata.getContributors()) {
                contributors.put(author.getName(), author.getContact().asMap());
            }
            map.put("contributors", contributors);
        } {
            Map<String, Object> values = new HashMap<>();
            for (Map.Entry<String, CustomValue> entry : metadata.getCustomValues().entrySet()) {
                switch (entry.getValue().getType()) {
                    case BOOLEAN -> values.put(entry.getKey(), entry.getValue().getAsBoolean());
                    case STRING -> values.put(entry.getKey(), entry.getValue().getAsString());
                    case NUMBER -> values.put(entry.getKey(), entry.getValue().getAsNumber().doubleValue());
                }
            }
            map.put("values", values);
        } {
            List<Map<String, Object>> dependencies = new ArrayList<>();
            for (ModDependency dependency : metadata.getDependencies()) {
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
        return map;
    }
}
