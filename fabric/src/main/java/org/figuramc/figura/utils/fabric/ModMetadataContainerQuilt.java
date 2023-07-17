package org.figuramc.figura.utils.fabric;

import org.figuramc.figura.utils.ModMetadataContainer;
import org.figuramc.figura.utils.Version;
import org.quiltmc.loader.api.LoaderValue;
import org.quiltmc.loader.api.ModContributor;
import org.quiltmc.loader.api.ModDependency;
import org.quiltmc.loader.api.ModLicense;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.api.QuiltLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * class that fills in relevant info for a mod in quilt loader context.
 * use <code>getKeyToObjectMap()</code> to fill in data about a mod
 * into a map.
 * <code>quilt-loader-0.19.2</code>.
 */
public class ModMetadataContainerQuilt extends ModMetadataContainer {
    private final ModMetadata quiltMetadata;
    protected ModMetadataContainerQuilt(String modID) {
        super(modID);
        this.quiltMetadata = QuiltLoader.getModContainer(modID).isPresent() ? QuiltLoader.getModContainer(modID).get().metadata() : null;
    }

    private static Object makeDependency(ModDependency dependency) {
        if (dependency instanceof ModDependency.Only only) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", only.id().toString());
            map.put("optional", only.optional());
            map.put("reason", only.reason());
            map.put("version_range", only.versionRange().toString());
            map.put("unless", makeDependency(only.unless()));
            return map;
        } else {
            Collection<ModDependency.Only> set = dependency instanceof ModDependency.All all ? all : dependency instanceof ModDependency.Any any ? any : null;
            if(set == null) return null;
            List<Object> list = new ArrayList<>();
            for (ModDependency.Only only : set) {
                list.add(makeDependency(only));
            }
            return list;
        }
    }

    @Override
    public String getCustomValueAsString(String key) {
        return quiltMetadata.value(key).asString();
    }

    @Override
    public Number getCustomValueAsNumber(String key) {
        return quiltMetadata.value(key).asNumber();
    }

    @Override
    public Boolean getCustomValueAsBoolean(String key) {
        return quiltMetadata.value(key).asBoolean();
    }

    @Override
    public Object getCustomValueAsObject(String key) {
        return quiltMetadata.value(key);
    }

    @Override
    public Version getModVersion() {
        return new Version(quiltMetadata.version().raw());
    }

    Map<String, Object> map = new HashMap<>();
    @Override
    public Map<String, Object> getKeyToObjectMap() {
        if (map.isEmpty()) {
            map.put("id", quiltMetadata.id());
            map.put("name", quiltMetadata.name());
            map.put("description", quiltMetadata.description());
            map.put("contact_info", quiltMetadata.contactInfo());
            map.put("version", quiltMetadata.version().raw());
            map.put("group", quiltMetadata.group());
            map.put("icon", quiltMetadata.icon(512));
            {
                Map<String, Object> values = new HashMap<>();
                for (Map.Entry<String, LoaderValue> entry : quiltMetadata.values().entrySet()) {
                    switch (entry.getValue().type()) {
                        case BOOLEAN -> values.put(entry.getKey(), entry.getValue().asBoolean());
                        case STRING -> values.put(entry.getKey(), entry.getValue().asString());
                        case NUMBER -> values.put(entry.getKey(), entry.getValue().asNumber().doubleValue());
                        default -> values.put(entry.getKey(), null);
                    }
                }
                map.put("values", values);
            } {
                Map<String, Object> contributors = new HashMap<>();
                for (ModContributor contributor : quiltMetadata.contributors()) {
                    contributors.put(contributor.name(), contributor.roles());
                }
                map.put("contributors", contributors);
            } {
                List<Object> depends = new ArrayList<>();
                for (ModDependency dependency : quiltMetadata.depends()) {
                    depends.add(makeDependency(dependency));
                }
                map.put("dependencies", depends);
            } {
                List<Object> breaks = new ArrayList<>();
                for (ModDependency dependency : quiltMetadata.breaks()) {
                    breaks.add(makeDependency(dependency));
                }
                map.put("breaks", breaks);
            } {
                List<Map<String, String>> licenses = new ArrayList<>();
                for (ModLicense license : quiltMetadata.licenses()) {
                    Map<String, String> licenseData = new HashMap<>();
                    licenseData.put("id", license.id());
                    licenseData.put("description", license.description());
                    licenseData.put("name", license.name());
                    licenseData.put("url", license.url());
                    licenses.add(licenseData);
                }
                map.put("licenses", licenses);
            } {
                List<Map<String, String>> provided = new ArrayList<>();
                for (ModMetadata.ProvidedMod mod : quiltMetadata.provides()) {
                    Map<String, String> other = new HashMap<>();
                    other.put("id", mod.id());
                    other.put("group", mod.group());
                    other.put("version", mod.version().raw());
                    provided.add(other);
                }
                map.put("provides", provided);
            }
        }
        return map;
    }
}
