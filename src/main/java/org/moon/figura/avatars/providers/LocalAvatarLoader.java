package org.moon.figura.avatars.providers;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import org.moon.figura.FiguraMod;
import org.moon.figura.parsers.AvatarMetadataParser;
import org.moon.figura.parsers.BlockbenchModelParser;
import org.moon.figura.parsers.LuaScriptParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * class used to load avatars from a file
 * and used for hotswapping
 */
public class LocalAvatarLoader {

    private static NbtCompound LAST_LOADED_NBT;
    private static Path LAST_LOADED_PATH;

    /**
     * Loads an NbtCompound from the specified path
     * @param path - the file/folder for loading the avatar
     * @return the NbtCompound from this path
     */
    public static NbtCompound loadAvatar(Path path) {
        LAST_LOADED_PATH = path;

        //load as nbt (.moon)
        if (path.toString().endsWith(".moon")) {
            try {
                //NbtIo already closes the file stream
                return LAST_LOADED_NBT = NbtIo.readCompressed(new FileInputStream(path.toFile()));
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to load Avatar: " + path.getFileName().toString());
                FiguraMod.LOGGER.error(e);
                return LAST_LOADED_NBT = null;
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
            return LAST_LOADED_NBT = nbt;

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

        return LAST_LOADED_NBT = nbt;
    }

    /**
     * Saves the loaded NBT into a folder inside the avatar list
     */
    public static void saveNbt() {
        if (LAST_LOADED_NBT == null)
            return;

        Path directory = LocalAvatarFetcher.getLocalAvatarDirectory().resolve("[§9Figura§r] Cached Avatars");
        Path file = directory.resolve("cache-" + new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date()) + ".moon");
        try {
            Files.createDirectories(directory);
            NbtIo.writeCompressed(LAST_LOADED_NBT, new FileOutputStream(file.toFile()));
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to save avatar: " + file.getFileName().toString());
            FiguraMod.LOGGER.error(e);
        }
    }

    // -- helper functions -- //

    public static File[] getFilesByExtension(Path root, String extension) {
        return root.toFile().listFiles(name -> name.toString().toLowerCase().endsWith(extension.toLowerCase()));
    }

    public static String readFile(File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            String fileContent = new String(stream.readAllBytes());
            stream.close();
            return fileContent;
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to read File: " + file.toString());
            FiguraMod.LOGGER.error(e);
            return "";
        }
    }

    public static NbtCompound getLastLoadedNbt() {
        return LAST_LOADED_NBT;
    }
}
