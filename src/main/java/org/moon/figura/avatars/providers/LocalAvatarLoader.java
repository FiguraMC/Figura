package org.moon.figura.avatars.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.parsers.AvatarMetadataParser;
import org.moon.figura.parsers.BlockbenchModelParser;
import org.moon.figura.parsers.LuaScriptParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * class used to load avatars from a file
 * and used for hotswapping
 */
public class LocalAvatarLoader {

    private static CompoundTag lastLoadedNbt;
    private static Path lastLoadedPath;

    private static WatchService watcher;
    private static WatchKey key;
    static {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to initialize the watcher service", e);
        }
    }

    /**
     * Loads an NbtCompound from the specified path
     * @param path - the file/folder for loading the avatar
     * @return the NbtCompound from this path
     */
    public static CompoundTag loadAvatar(Path path) {
        lastLoadedPath = path;
        updateWatchKey(path);

        if (path == null)
            return lastLoadedNbt = null;

        //load as nbt (.moon)
        if (path.toString().endsWith(".moon")) {
            try {
                //NbtIo already closes the file stream
                return lastLoadedNbt = NbtIo.readCompressed(new FileInputStream(path.toFile()));
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to load Avatar: " + path.getFileName().toString(), e);
                return lastLoadedNbt = null;
            }
        }

        //load as folder
        CompoundTag nbt = new CompoundTag();

        //scripts
        File[] scripts = getFilesByExtension(path, ".lua");
        if (scripts != null && scripts.length > 0) {
            CompoundTag scriptsNbt = new CompoundTag();
            for (File script : scripts) {
                String name = script.getName();
                scriptsNbt.put(name.substring(0, name.length() - 4), LuaScriptParser.parse(readFile(script)));
            }

            nbt.put("scripts", scriptsNbt);

            //sounds
            //avatar needs a script to load custom sounds
            File[] sounds = getFilesByExtension(path.resolve("sounds"), ".ogg");
            if (sounds != null && sounds.length > 0) {
                CompoundTag soundsNbt = new CompoundTag();
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
            return lastLoadedNbt = nbt;

        CompoundTag modelRoot = new CompoundTag();
        modelRoot.putString("name", "models");

        ListTag children = new ListTag();
        ListTag textures = new ListTag();
        ListTag animations = new ListTag();

        BlockbenchModelParser parser = new BlockbenchModelParser();
        for (File model : models) {
            BlockbenchModelParser.ModelData data = parser.parseModel(readFile(model));
            children.add(data.modelNbt());
            textures.addAll(data.textureList());
            animations.addAll(data.animationList());
        }

        modelRoot.put("chld", children);


        //metadata
        String metadata = readFile(path.resolve("avatar.json").toFile());
        nbt.put("metadata", AvatarMetadataParser.parse(metadata, path.getFileName().toString()));

        //TODO: Better error handling while converting to nbt
        try {
            AvatarMetadataParser.injectToModels(metadata, modelRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //return :3
        nbt.put("models", modelRoot);
        nbt.put("textures", textures);
        nbt.put("animations", animations);

        return lastLoadedNbt = nbt;
    }

    /**
     * Saves the loaded NBT into a folder inside the avatar list
     */
    public static void saveNbt() {
        if (lastLoadedNbt == null)
            return;

        Path directory = LocalAvatarFetcher.getLocalAvatarDirectory().resolve("[§9Figura§r] Cached Avatars");
        Path file = directory.resolve("cache-" + new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date()) + ".moon");
        try {
            Files.createDirectories(directory);
            NbtIo.writeCompressed(lastLoadedNbt, new FileOutputStream(file.toFile()));
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to save avatar: " + file.getFileName().toString(), e);
        }
    }

    /**
     * Tick the watched key for hotswapping avatars
     */
    public static void tickWatchedKey() {
        if (key == null || !key.isValid())
            return;

        for (WatchEvent<?> event : key.pollEvents()) {
            if (event.kind() == StandardWatchEventKinds.OVERFLOW)
                continue;

            //reload avatar
            FiguraMod.LOGGER.debug("Local avatar files changed - Reloading!");
            FiguraMod.LOGGER.debug(event.context().toString());
            AvatarManager.loadLocalAvatar(lastLoadedPath);
            break;
        }
    }

    /**
     * Cancels previous watch key and register a new one
     * @param path the path to register the watch key
     */
    private static void updateWatchKey(Path path) {
        if (key != null)
            key.cancel();

        if (watcher == null)
            return;

        try {
            key = path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to register watcher for " + path, e);
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
            FiguraMod.LOGGER.error("Failed to read File: " + file.toString(), e);
            return "";
        }
    }

    public static CompoundTag getLastLoadedNbt() {
        return lastLoadedNbt;
    }
}
