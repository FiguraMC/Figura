package org.figuramc.figura.parsers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.nbt.*;

import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.model.rendering.texture.RenderTypes;
import org.figuramc.figura.utils.PathUtils;
import org.figuramc.figura.utils.Version;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// parses a metadata json
// and return a nbt compound of it
public class AvatarMetadataParser {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, String> PARTS_TO_MOVE = new HashMap<>();

    public static Metadata read(String json) {
        Metadata metadata = GSON.fromJson(json, Metadata.class);
        return metadata == null ? new Metadata() : metadata;
    }

    public static CompoundTag parse(String json, String filename) {
        // parse json -> object
        Metadata metadata = read(json);

        // nbt
        CompoundTag nbt = new CompoundTag();
        JsonElement jsonElement = JsonParser.parseString(json);
        if (jsonElement != null && !jsonElement.isJsonNull() && !jsonElement.getAsJsonObject().asMap().isEmpty()) {
            for (Map.Entry<String, JsonElement> jsonElementEntry : jsonElement.getAsJsonObject().entrySet()) {
                if (jsonElementEntry.getKey() != null && !jsonElementEntry.getKey().isBlank() && jsonElementEntry.getKey().contains("badge_color_")) {
                    nbt.putString(jsonElementEntry.getKey(), jsonElementEntry.getValue().getAsString());
                }
            }
        }

        // version
        Version version = new Version(metadata.version);
        if (version.invalid)
            version = FiguraMod.VERSION;

        nbt.putString("name", metadata.name == null || metadata.name.isBlank() ? filename : metadata.name);
        nbt.putString("ver", version.toString());
        if (metadata.color != null) nbt.putString("color", metadata.color);
        if (metadata.background != null) nbt.putString("bg", metadata.background);
        if (metadata.id != null) nbt.putString("id", metadata.id);

        if (metadata.authors != null) {
            StringBuilder authors = new StringBuilder();

            for (int i = 0; i < metadata.authors.length; i++) {
                String name = metadata.authors[i];
                authors.append(name);

                if (i < metadata.authors.length - 1)
                    authors.append("\n");
            }

            nbt.putString("authors", authors.toString());
        } else {
            nbt.putString("authors", metadata.author == null ? "?" : metadata.author);
        }

        if (metadata.autoScripts != null) {
            ListTag autoScripts = new ListTag();
            for (String name : metadata.autoScripts) {
                autoScripts.add(StringTag.valueOf(PathUtils.computeSafeString(name.replaceAll("\\.lua$", ""))));
            }
            nbt.put("autoScripts", autoScripts);
        }

        if (Configs.FORMAT_SCRIPT.value >= 2)
            nbt.putBoolean("minify", true);

        if (metadata.autoAnims != null) {
            ListTag autoAnims = new ListTag();
            for (String name : metadata.autoAnims)
                autoAnims.add(StringTag.valueOf(name));
            nbt.put("autoAnims", autoAnims);
        }

        if (metadata.resources != null) {
            ListTag resourcesPaths = new ListTag();
            for (String resource :
                    metadata.resources) {
                resourcesPaths.add(StringTag.valueOf(resource));
            }
            nbt.put("resources_paths", resourcesPaths);
        }

        return nbt;
    }

    public static void injectToModels(String json, CompoundTag models) throws IOException {
        PARTS_TO_MOVE.clear();

        Metadata metadata = GSON.fromJson(json, Metadata.class);
        if (metadata != null && metadata.customizations != null) {
            for (Map.Entry<String, Customization> entry : metadata.customizations.entrySet())
                injectCustomization(entry.getKey(), entry.getValue(), models);
        }

        for (Map.Entry<String, String> entry : PARTS_TO_MOVE.entrySet()) {
            CompoundTag modelPart = getTag(models, entry.getKey(), true);
            CompoundTag targetPart = getTag(models, entry.getValue(), false);

            ListTag list = !targetPart.contains("chld") ? new ListTag() : targetPart.getList("chld", Tag.TAG_COMPOUND);
            list.add(modelPart);
            targetPart.put("chld", list);
        }
    }

    private static void injectCustomization(String path, Customization customization, CompoundTag models) throws IOException {
        boolean remove = customization.remove != null && customization.remove;
        CompoundTag modelPart = getTag(models, path, remove);

        // Add more of these later
        if (remove) {
            return;
        }
        if (customization.primaryRenderType != null) {
            try {
                modelPart.putString("primary", RenderTypes.valueOf(customization.primaryRenderType.toUpperCase(Locale.US)).name());
            } catch (Exception ignored) {
                throw new IOException("Invalid render type \"" + customization.primaryRenderType + "\"!");
            }
        }
        if (customization.secondaryRenderType != null) {
            try {
                modelPart.putString("secondary", RenderTypes.valueOf(customization.secondaryRenderType.toUpperCase(Locale.US)).name());
            } catch (Exception ignored) {
                throw new IOException("Invalid render type \"" + customization.secondaryRenderType + "\"!");
            }
        }
        if (customization.parentType != null) {
            ParentType type = ParentType.get(customization.parentType);

            if (type == ParentType.None)
                modelPart.remove("pt");
            else
                modelPart.putString("pt", type.name());
        }
        if (customization.moveTo != null) {
            PARTS_TO_MOVE.put(path, customization.moveTo);
        }
        if (customization.visible != null) {
            if (customization.visible) {
                modelPart.remove("vsb");
            } else {
                modelPart.putBoolean("vsb", false);
            }
        }
        if (customization.smooth != null) {
            modelPart.putBoolean("smo", customization.smooth);
        }
    }

    private static CompoundTag getTag(CompoundTag models, String path, boolean remove) throws IOException {
        String[] keys = path.replaceFirst("^models", "").split("\\.", 0);
        CompoundTag current = models;

        for (int i = 0; i < keys.length; i++) {
            if (keys[i].isEmpty())
                continue;

            if (!current.contains("chld"))
                throw new IOException("Invalid part path: \"" + path + "\"");

            ListTag children = current.getList("chld", Tag.TAG_COMPOUND);
            int j = 0;
            for (; j < children.size(); j++) {
                CompoundTag child = children.getCompound(j);

                if (child.getString("name").equals(keys[i])) {
                    current = child;
                    break;
                }

                if (j == children.size() - 1)
                    throw new IOException("Invalid part path: \"" + path + "\"");
            }

            if (remove && i == keys.length - 1)
                children.remove(j);
        }

        return current;
    }

    public static void injectToTextures(String json, CompoundTag textures) {
        Metadata metadata = GSON.fromJson(json, Metadata.class);
        if (metadata == null || metadata.ignoredTextures == null)
            return;

        CompoundTag src = textures.getCompound("src");

        for (String texture : metadata.ignoredTextures) {
            byte[] bytes = src.getByteArray(texture);
            int[] size = BlockbenchModelParser.getTextureSize(bytes);
            ListTag list = new ListTag();
            list.add(IntTag.valueOf(size[0]));
            list.add(IntTag.valueOf(size[1]));
            src.put(texture, list);
        }
    }

    // json object class
    public static class Metadata {
        public String name, description, author, version, color, background, id;
        public String[] authors, autoScripts, autoAnims, ignoredTextures, resources;
        public HashMap<String, Customization> customizations;
    }

    /**
     * Contains only things you can't normally set in blockbench.
     * So nothing about position, rotation, scale, uv, whatever
     * customizations you could just put in the model regularly yourself.
     */
    public static class Customization {
        public String primaryRenderType, secondaryRenderType;
        public String parentType;
        public String moveTo;
        public Boolean visible, remove, smooth;
    }
}
