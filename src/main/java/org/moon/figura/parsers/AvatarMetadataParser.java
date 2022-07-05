package org.moon.figura.parsers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//parses a metadata json
//and return a nbt compound of it
public class AvatarMetadataParser {

    private static final Gson GSON = new GsonBuilder().create();
    private static final String SEPARATOR_REGEX = "\\.";

    public static CompoundTag parse(String json, String filename) {
        //parse json -> object
        Metadata metadata = GSON.fromJson(json, Metadata.class);
        if (metadata == null) metadata = new Metadata();

        //nbt
        CompoundTag nbt = new CompoundTag();

        nbt.putString("name", metadata.name == null ? filename : metadata.name);
        nbt.putString("ver", metadata.version == null ? FiguraMod.VERSION : metadata.version);
        nbt.putString("color", metadata.color == null ? "default" : metadata.color);

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
            for (String scriptName : metadata.autoScripts)
                autoScripts.add(StringTag.valueOf(scriptName.replace(".lua", "")));
            nbt.put("autoScripts", autoScripts);
        }

        return nbt;
    }

    public static void injectToModels(String json, CompoundTag models) throws IOException {
        Metadata metadata = GSON.fromJson(json, Metadata.class);
        if (metadata != null && metadata.customizations != null)
            for (Map.Entry<String, Customization> entry : metadata.customizations.entrySet())
                injectCustomization(entry.getKey(), entry.getValue(), models);
    }

    private static void injectCustomization(String path, Customization customization, CompoundTag models) throws IOException {
        CompoundTag modelPart = getTag(models, path);

        //Add more of these later
        if (customization.primaryRenderType != null) {
            try {
                modelPart.putString("primary", FiguraTextureSet.RenderTypes.valueOf(customization.primaryRenderType).name());
            } catch (Exception ignored) {
                throw new IOException("Invalid render type \"" + customization.primaryRenderType + "\"!");
            }
        }
        if (customization.secondaryRenderType != null) {
            try {
                modelPart.putString("secondary", FiguraTextureSet.RenderTypes.valueOf(customization.secondaryRenderType).name());
            } catch (Exception ignored) {
                throw new IOException("Invalid render type \"" + customization.secondaryRenderType + "\"!");
            }
        }
        if (customization.parentType != null) {
            FiguraModelPart.ParentType type = FiguraModelPart.ParentType.get(customization.parentType);

            if (type == FiguraModelPart.ParentType.None)
                modelPart.remove("pt");
            else
                modelPart.putString("pt", type.name());
        }
    }

    private static CompoundTag getTag(CompoundTag models, String path) throws IOException {
        String[] keys = path.split(SEPARATOR_REGEX);
        CompoundTag current = models;
        for (String key : keys) {
            if (current.contains("chld")) {
                ListTag children = current.getList("chld", Tag.TAG_COMPOUND);
                for (int j = 0; j < children.size(); j++) {
                    CompoundTag child = children.getCompound(j);
                    if (child.getString("name").equals(key)) {
                        current = child;
                        break;
                    }
                    if (j == children.size() - 1)
                        throw new IOException("Invalid part path: \"" + path + "\".");
                }
            } else
                throw new IOException("Invalid part path: \"" + path + "\".");
        }
        return current;
    }

    //json object class
    private static class Metadata {
        String name, author, version, color;
        String[] authors, autoScripts;
        HashMap<String, Customization> customizations;
    }

    /**
     * Contains only things you can't normally set in blockbench.
     * So nothing about position, rotation, scale, uv, whatever
     * customizations you could just put in the model regularly yourself.
     */
    private static class Customization {
        String primaryRenderType, secondaryRenderType;
        String parentType;
    }

}
