package org.moon.figura.avatars.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.parsers.AvatarMetadataParser;
import org.moon.figura.parsers.BlockbenchModelParser;
import org.moon.figura.parsers.LuaScriptParser;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraResourceListener;
import org.moon.figura.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * class used to load avatars from a file
 * and used for hotswapping
 */
public class LocalAvatarLoader {

    private static WatchService watcher;
    private static final HashMap<Path, WatchKey> KEYS = new HashMap<>();
    private static Path lastLoadedPath;

    public static CompoundTag cheese;
    public static final FiguraResourceListener AVATAR_LISTENER = new FiguraResourceListener("avatars", manager -> {
        try {
            cheese = NbtIo.readCompressed(manager.getResource(new FiguraIdentifier("avatars/cheese.moon")).getInputStream());
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load the cheese", e);
        }
    });

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
    public static CompoundTag loadAvatar(Path path) throws IOException {
        lastLoadedPath = path;
        resetWatchKeys();
        addWatchKey(path);

        if (path == null)
            return null;

        //load as nbt (.moon)
        if (path.toString().endsWith(".moon")) {
            //NbtIo already closes the file stream
            return NbtIo.readCompressed(new FileInputStream(path.toFile()));
        }

        //load as folder
        CompoundTag nbt = new CompoundTag();

        //Load metadata first!
        String metadata = IOUtils.readFile(path.resolve("avatar.json").toFile());
        nbt.put("metadata", AvatarMetadataParser.parse(metadata, path.getFileName().toString()));

        //scripts
        loadScripts(path, nbt);

        ListTag textures = new ListTag();
        ListTag animations = new ListTag();
        BlockbenchModelParser parser = new BlockbenchModelParser();

        CompoundTag models = loadModels(path, parser, textures, animations);
        models.putString("name", "models");

        AvatarMetadataParser.injectToModels(metadata, models);

        //return :3
        nbt.put("models", models);
        nbt.put("textures", textures);
        nbt.put("animations", animations);

        return nbt;
    }

    private static void loadScripts(Path path, CompoundTag nbt) throws IOException {
        List<File> scripts = IOUtils.getFilesByExtension(path, ".lua", true);
        if (scripts.size() > 0) {
            CompoundTag scriptsNbt = new CompoundTag();
            String pathRegex = Pattern.quote(path + File.separator);
            for (File script : scripts) {
                String pathStr = script.toPath().toString();
                String name = pathStr.replaceFirst(pathRegex, "");
                name = name.replace(File.separatorChar, '/');
                scriptsNbt.put(name.substring(0, name.length() - 4), LuaScriptParser.parse(IOUtils.readFile(script)));
            }

            nbt.put("scripts", scriptsNbt);

            //sounds
            //avatar needs a script to load custom sounds
            loadSounds(path, nbt);
        }
    }

    private static void loadSounds(Path path, CompoundTag nbt) throws IOException {
        List<File> sounds = IOUtils.getFilesByExtension(path, ".ogg", false);
        if (sounds.size() > 0) {
            CompoundTag soundsNbt = new CompoundTag();
            for (File sound : sounds) {
                String name = sound.getName();
                soundsNbt.putByteArray(name.substring(0, name.length() - 4), IOUtils.readFile(sound).getBytes());
            }
            nbt.put("sounds", soundsNbt);
        }
    }

    private static CompoundTag loadModels(Path path, BlockbenchModelParser parser, ListTag textures, ListTag animations) throws IOException {
        CompoundTag result = new CompoundTag();
        File[] subFiles = path.toFile().listFiles(f -> !f.isHidden() && !f.getName().startsWith("."));
        ListTag children = new ListTag();
        if (subFiles != null)
            for (File file : subFiles) {
                if (file.isDirectory()) {
                    CompoundTag subfolder = loadModels(file.toPath(), parser, textures, animations);
                    subfolder.putString("name", file.getName());
                    children.add(subfolder);
                } else if (file.toString().toLowerCase().endsWith(".bbmodel")) {
                    BlockbenchModelParser.ModelData data = parser.parseModel(IOUtils.readFile(file), file.getName().substring(0, file.getName().length() - 8));
                    children.add(data.modelNbt());
                    textures.addAll(data.textureList());
                    animations.addAll(data.animationList());
                }
            }

        if (children.size() > 0)
            result.put("chld", children);

        return result;
    }



    /**
     * Saves the loaded NBT into a folder inside the avatar list
     */
    public static void saveNbt(CompoundTag nbt) {
        Path directory = LocalAvatarFetcher.getLocalAvatarDirectory().resolve("[§9" + FiguraMod.MOD_NAME + "§r] Cached Avatars");
        Path file = directory.resolve("cache-" + new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date()) + ".moon");
        try {
            Files.createDirectories(directory);
            NbtIo.writeCompressed(nbt, new FileOutputStream(file.toFile()));
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to save avatar: " + file.getFileName().toString(), e);
        }
    }

    /**
     * Tick the watched key for hotswapping avatars
     */
    public static void tickWatchedKey() {
        WatchEvent<?> event = null;
        boolean reload = false;

        for (Map.Entry<Path, WatchKey> entry : KEYS.entrySet()) {
            WatchKey key = entry.getValue();
            if (!key.isValid())
                continue;

            for (WatchEvent<?> watchEvent : key.pollEvents()) {
                if (watchEvent.kind() == StandardWatchEventKinds.OVERFLOW)
                    continue;

                event = watchEvent;
                File file = entry.getKey().resolve(((WatchEvent<Path>) event).context()).toFile();
                String name = file.getName();

                if (file.isHidden() || name.startsWith(".") || (!file.isDirectory() && !name.matches("(.*(\\.lua|\\.bbmodel|\\.ogg|\\.png)$|avatar\\.json)")))
                    continue;

                reload = true;
                break;
            }

            if (reload)
                break;
        }

        //reload avatar
        if (reload) {
            FiguraMod.LOGGER.debug("Local avatar files changed - Reloading!");
            FiguraMod.LOGGER.debug(event.context().toString());
            AvatarManager.loadLocalAvatar(lastLoadedPath);
        }
    }

    private static void resetWatchKeys() {
        for (WatchKey key : KEYS.values())
            key.cancel();
        KEYS.clear();
    }

    /**
     * register new watch keys
     * @param path the path to register the watch key
     */
    private static void addWatchKey(Path path) {
        if (watcher == null || path == null)
            return;

        File file = path.toFile();
        if (!file.isDirectory() || file.isHidden() || file.getName().startsWith("."))
            return;

        try {
            WatchKey key = path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            KEYS.put(path, key);

            File[] children = file.listFiles();
            if (children == null)
                return;

            for (File child : children)
                addWatchKey(child.toPath());
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to register watcher for " + path, e);
        }
    }

    public static Path getLastLoadedPath() {
        return lastLoadedPath;
    }
}
