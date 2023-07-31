package org.figuramc.figura.utils.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.figuramc.figura.utils.ModMetadataContainer;
import org.figuramc.figura.utils.Version;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModMetadataContainerImpl extends ModMetadataContainer {
    private final IModInfo modInfo;
    protected ModMetadataContainerImpl(String modID) {
        super(modID);
        this.modInfo = ModList.get().getModContainerById(modID).isPresent() ? ModList.get().getModContainerById(modID).get().getModInfo() : null;
    }

    public static ModMetadataContainer getMetadataForMod(String modID) {
        return new ModMetadataContainerImpl(modID);
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

    @Override
    public Map<String, Object> getKeyToObjectMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", modInfo.getModId());
        map.put("name", modInfo.getDisplayName());
        map.put("description", modInfo.getDescription());
        map.put("version", modInfo.getVersion().toString());
        map.put("namespace", modInfo.getNamespace());
        map.put("mod_url", modInfo.getModURL().map(URL::toString).orElse(""));
        map.put("update_url", modInfo.getUpdateURL().map(URL::toString).orElse(""));
        map.put("logo", modInfo.getLogoFile().orElse(""));
        map.put("logo_blurred", modInfo.getLogoBlur());
        map.put("authors", ((ModInfo)modInfo).getConfigElement("authors").toString());
        map.put("credits", ((ModInfo)modInfo).getConfigElement("credits").toString());
        map.put("licenses", modInfo.getOwningFile().getLicense());
        {
            Map<String, Object> values = new HashMap<>();
            for (Map.Entry<String, Object> entry : modInfo.getModProperties().entrySet()) {
                if (entry.getValue().getClass().equals(Number.class)) {
                    values.put(entry.getKey(), entry.getValue());
                } else if (entry.getValue().getClass().equals(Boolean.class)) {
                    values.put(entry.getKey(), entry.getValue());
                } else if (entry.getValue().getClass().equals(String.class)) {
                    values.put(entry.getKey(), entry.getValue().toString());
                }
            }
            map.put("values", values);
        } {
            List<Map<String, Object>> dependencies = new ArrayList<>();
            for (IModInfo.ModVersion dependency : modInfo.getDependencies()) {
                Map<String, Object> dependencyData = new HashMap<>();
                dependencyData.put("id", dependency.getModId());
                dependencyData.put("kind", dependency.getSide());
                {
                    List<String> versions = new ArrayList<>();
                    versions.add(dependency.getVersionRange().getRecommendedVersion().getQualifier());
                    dependencyData.put("versions", versions);
                }
                dependencies.add(dependencyData);
            }
            map.put("dependencies", dependencies);
        } {
            List<Map<String, String>> provided = new ArrayList<>();
            for (IModInfo mod : modInfo.getOwningFile().getMods()) {
                Map<String, String> other = new HashMap<>();
                other.put("id", mod.getModId());
                other.put("version", mod.getVersion().getQualifier());
                provided.add(other);
            }
            map.put("provides", provided);
        }
        return map;
    }
}
