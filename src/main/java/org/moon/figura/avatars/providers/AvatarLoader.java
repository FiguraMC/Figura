package org.moon.figura.avatars.providers;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.parsers.AvatarMetadataParser;
import org.moon.figura.parsers.BlockbenchModelParser;
import org.moon.figura.parsers.LuaScriptParser;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;

//class used to load avatars from a file
//returns an Avatar
public class AvatarLoader {

    // -- loaders -- //

    public static Avatar loadAvatar(Path path) {
        //load as nbt (.moon)
        if (path.toString().endsWith(".moon")) {
            try {
                FileInputStream fis = new FileInputStream(path.toFile());
                return new Avatar(NbtIo.readCompressed(fis));
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to load Avatar: " + path.getFileName().toString());
                FiguraMod.LOGGER.error(e);
                return null;
            }
        }

        //load as folder
        NbtCompound nbt = new NbtCompound();

        //metadata
        File metadata = path.resolve("avatar.json").toFile();
        nbt.put("metadata", AvatarMetadataParser.parse(readFile(metadata), path.getFileName().toString()));

        //scripts
        File[] scripts = getFilesByExtension(path, ".lua");
        if (scripts != null && scripts.length > 0) {
            NbtCompound scriptsNbt = new NbtCompound();
            for (File script : scripts) {
                String name = script.getName();
                scriptsNbt.put(name.substring(0, name.length() - 4), LuaScriptParser.parse(readFile(script)));
            }

            nbt.put("scripts", scriptsNbt);

            //sounds
            //avatar needs a script to load custom sounds
            File[] sounds = getFilesByExtension(path.resolve("sounds"), ".ogg");
            if (sounds != null && sounds.length > 0) {
                NbtCompound soundsNbt = new NbtCompound();
                for (File sound : sounds) {
                    String name = sound.getName();
                    soundsNbt.putByteArray(name.substring(0, name.length() - 4), readFile(sound).getBytes());
                }

                nbt.put("sounds", soundsNbt);
            }
        }

        //models
        File[] models = getFilesByExtension(path, ".bbmodel");

        //if no model is found we can return the avatar here
        if (models == null || models.length == 0)
            return new Avatar(nbt);

        NbtCompound modelRoot = new NbtCompound();
        modelRoot.putString("name", "models");

        NbtList children = new NbtList();
        NbtList textures = new NbtList();
        NbtList animations = new NbtList();

        BlockbenchModelParser parser = new BlockbenchModelParser();
        for (File model : models) {
            BlockbenchModelParser.ModelData data = parser.parseModel(readFile(model));
            children.add(data.modelNbt());
            textures.addAll(data.textureList());
            animations.addAll(data.animationList());
        }

        modelRoot.put("chld", children);

        //return :3
        nbt.put("models", modelRoot);
        nbt.put("textures", textures);
        nbt.put("animations", animations);

        return new Avatar(nbt);
    }

    // -- helper functions -- //

    public static File[] getFilesByExtension(Path root, String extension) {
        return root.toFile().listFiles(name -> name.toString().toLowerCase().endsWith(extension.toLowerCase()));
    }

    public static String readFile(File f) {
        try {
            FileInputStream fs = new FileInputStream(f);
            return new String(fs.readAllBytes());
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to read File: " + f.toString());
            FiguraMod.LOGGER.error(e);
        }

        return "";
    }
}
