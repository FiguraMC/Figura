package org.moon.figura.avatars.providers;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.packs.resources.ResourceManager;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.parsers.AvatarMetadataParser;
import org.moon.figura.parsers.BlockbenchModelParser;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraResourceListener;
import org.moon.figura.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * class used to load avatars from a file
 * and used for hotswapping
 */
public class LocalAvatarLoader {

    private static WatchService watcher;
    private static final HashMap<Path, WatchKey> KEYS = new HashMap<>();
    private static Path lastLoadedPath;
    private static int loadState;

    public static CompoundTag cheese;
    public static final ArrayList<CompoundTag> SERVER_AVATARS = new ArrayList<>();
    private static final BiFunction<String, ResourceManager, CompoundTag> LOAD_AVATAR = (name, manager) -> {
        try {
            return NbtIo.readCompressed(manager.getResource(new FiguraIdentifier("avatars/" + name + ".moon")).get().open());
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load the " + name + " avatar", e);
            return null;
        }
    };
    public static final FiguraResourceListener AVATAR_LISTENER = new FiguraResourceListener("avatars", manager -> {
        cheese = LOAD_AVATAR.apply("cheese", manager);

        SERVER_AVATARS.clear();
        manager.listResources("avatars/server", resource -> resource.getNamespace().equals(FiguraMod.MOD_ID) && resource.getPath().endsWith(".moon")).forEach((location, resource) -> {
            String name = location.getPath().substring(8, location.getPath().length() - 5);
            SERVER_AVATARS.add(LOAD_AVATAR.apply(name, manager));
        });
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
        loadState = 0;
        resetWatchKeys();
        lastLoadedPath = path;
        addWatchKey(path);

        if (path == null)
            return null;

        //load as nbt (.moon)
        loadState++;
        if (path.toString().endsWith(".moon")) {
            //NbtIo already closes the file stream
            return NbtIo.readCompressed(new FileInputStream(path.toFile()));
        }

        //load as folder
        CompoundTag nbt = new CompoundTag();

        //scripts
        loadState++;
        loadScripts(path, nbt);

        //custom sounds
        loadState++;
        loadSounds(path, nbt);

        //models
        ListTag textures = new ListTag();
        ListTag animations = new ListTag();
        BlockbenchModelParser parser = new BlockbenchModelParser();

        loadState++;
        CompoundTag models = loadModels(path, parser, textures, animations, "");
        models.putString("name", "models");

        //metadata
        loadState++;
        String metadata = IOUtils.readFile(path.resolve("avatar.json").toFile());
        nbt.put("metadata", AvatarMetadataParser.parse(metadata, path.getFileName().toString()));
        AvatarMetadataParser.injectToModels(metadata, models);

        //return :3
        if (!models.isEmpty())
            nbt.put("models", models);
        if (!textures.isEmpty())
            nbt.put("textures", textures);
        if (!animations.isEmpty())
            nbt.put("animations", animations);

        return nbt;
    }

    private static void loadScripts(Path path, CompoundTag nbt) throws IOException {
        List<File> scripts = IOUtils.getFilesByExtension(path, ".lua");
        if (scripts.size() > 0) {
            CompoundTag scriptsNbt = new CompoundTag();
            String pathRegex = Pattern.quote(path + File.separator);
            for (File script : scripts) {
                String pathStr = script.toPath().toString();
                String name = pathStr.replaceFirst(pathRegex, "");
                name = name.replaceAll("[/\\\\]", ".");
                scriptsNbt.put(name.substring(0, name.length() - 4), new ByteArrayTag(IOUtils.readFile(script).getBytes(StandardCharsets.UTF_8)));
            }
            nbt.put("scripts", scriptsNbt);
        }
    }

    private static void loadSounds(Path path, CompoundTag nbt) throws IOException {
        List<File> sounds = IOUtils.getFilesByExtension(path, ".ogg");
        if (sounds.size() > 0) {
            CompoundTag soundsNbt = new CompoundTag();
            String pathRegex = Pattern.quote(path + File.separator);
            for (File sound : sounds) {
                String pathStr = sound.toPath().toString();
                String name = pathStr.replaceFirst(pathRegex, "");
                name = name.replaceAll("[/\\\\]", ".");
                soundsNbt.putByteArray(name.substring(0, name.length() - 4), IOUtils.readFileBytes(sound));
            }
            nbt.put("sounds", soundsNbt);
        }
    }

    private static CompoundTag loadModels(Path path, BlockbenchModelParser parser, ListTag textures, ListTag animations, String folders) throws IOException {
        CompoundTag result = new CompoundTag();
        File[] subFiles = path.toFile().listFiles(f -> !f.isHidden() && !f.getName().startsWith("."));
        ListTag children = new ListTag();
        if (subFiles != null)
            for (File file : subFiles) {
                String name = file.getName();
                if (file.isDirectory()) {
                    CompoundTag subfolder = loadModels(file.toPath(), parser, textures, animations, folders + name + ".");
                    if (!subfolder.isEmpty()) {
                        subfolder.putString("name", name);
                        children.add(subfolder);
                    }
                } else if (file.toString().toLowerCase().endsWith(".bbmodel")) {
                    BlockbenchModelParser.ModelData data = parser.parseModel(IOUtils.readFile(file), name.substring(0, name.length() - 8), folders);
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
        Path directory = LocalAvatarFetcher.getLocalAvatarDirectory().resolve("[" + ChatFormatting.BLUE + FiguraMod.MOD_NAME + ChatFormatting.RESET + "] Cached Avatars");
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

    public static void resetWatchKeys() {
        lastLoadedPath = null;
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

    public static int getLoadState() {
        return loadState;
    }
}
