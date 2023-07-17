package org.figuramc.figura.utils.fabric;

import org.quiltmc.loader.api.*;

import java.util.*;

/**
 * interface that fills in relevant info for a mod in quilt loader context.
 * use <code>fill(map, id)</code> to fill in data about mod with <code>id</code>
 * into the given map (hopefully) fills in all the details available in
 * <code>quilt-loader-0.19.2</code>.
 */
interface QuiltModMetadataGetter {
    static void fill(Map<String, Object> map, String id) {
        Optional<ModContainer> modContainer = QuiltLoader.getModContainer(id);
        if (modContainer.isEmpty()) return;
        ModMetadata metadata = modContainer.get().metadata();
        map.put("id", metadata.id());
        map.put("name", metadata.name());
        map.put("description", metadata.description());
        map.put("contact_info", metadata.contactInfo());
        map.put("version", metadata.version().raw());
        map.put("group", metadata.group());
        map.put("icon", metadata.icon(512));
        {
            Map<String, Object> values = new HashMap<>();
            for (Map.Entry<String, LoaderValue> entry : metadata.values().entrySet()) {
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
            for (ModContributor contributor : metadata.contributors()) {
                contributors.put(contributor.name(), contributor.roles());
            }
            map.put("contributors", contributors);
        } {
            List<Object> depends = new ArrayList<>();
            for (ModDependency dependency : metadata.depends()) {
                depends.add(makeDependency(dependency));
            }
            map.put("dependencies", depends);
        } {
            List<Object> breaks = new ArrayList<>();
            for (ModDependency dependency : metadata.breaks()) {
                breaks.add(makeDependency(dependency));
            }
            map.put("breaks", breaks);
        } {
            List<Map<String, String>> licenses = new ArrayList<>();
            for (ModLicense license : metadata.licenses()) {
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
            for (ModMetadata.ProvidedMod mod : metadata.provides()) {
                Map<String, String> other = new HashMap<>();
                other.put("id", mod.id());
                other.put("group", mod.group());
                other.put("version", mod.version().raw());
                provided.add(other);
            }
            map.put("provides", provided);
        }
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
}
