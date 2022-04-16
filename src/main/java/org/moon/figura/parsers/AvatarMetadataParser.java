package org.moon.figura.parsers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.NbtCompound;
import org.moon.figura.FiguraMod;

//parses a metadata json
//and return a nbt compound of it
public class AvatarMetadataParser {

    public static NbtCompound parse(String json, String filename) {
        //parse json -> object
        Gson gson = new GsonBuilder().create();
        Metadata metadata = gson.fromJson(json, Metadata.class);
        if (metadata == null) metadata = new Metadata();

        //nbt
        NbtCompound nbt = new NbtCompound();

        nbt.putString("name", metadata.name == null ? filename : metadata.name);
        nbt.putString("ver", metadata.version == null ? FiguraMod.VERSION : metadata.version);
        nbt.putString("author", metadata.author == null ? "" : metadata.author);

        return nbt;
    }

    //json object class
    private static class Metadata {
        String name, author, version;
    }
}
