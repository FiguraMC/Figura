package org.moon.figura.avatar.local;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.UserData;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.parsers.AvatarMetadataParser;
import org.moon.figura.parsers.BlockbenchModelParser;
import org.moon.figura.parsers.LuaScriptParser;
import org.moon.figura.utils.FiguraResourceListener;
import org.moon.figura.utils.FiguraText;
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
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * class used to load avatars from a file
 * and used for hotswapping
 */
public class LocalAvatarLoader {

    public static final boolean IS_WINDOWS = Util.getPlatform() == Util.OS.WINDOWS;
    private static final HashMap<Path, WatchKey> KEYS = new HashMap<>();

    private static CompletableFuture<Void> tasks;
    private static Path lastLoadedPath;
    private static int loadState;
    private static String loadError;

    private static WatchService watcher;

    public static final HashMap<ResourceLocation, CompoundTag> CEM_AVATARS = new HashMap<>();
    public static final FiguraResourceListener AVATAR_LISTENER = new FiguraResourceListener("cem", manager -> {
        CEM_AVATARS.clear();
        AvatarManager.clearCEMAvatars();

        for (Map.Entry<ResourceLocation, Resource> cem : manager.listResources("cem", location -> location.getNamespace().equals(FiguraMod.MOD_ID) && location.getPath().endsWith(".moon")).entrySet()) {
            //id
            ResourceLocation key = cem.getKey();
            String[] split = key.getPath().split("/");
            if (split.length <= 1)
                continue;

            String namespace = split[split.length - 2];
            String path = split[split.length - 1];
            ResourceLocation id = new ResourceLocation(namespace, path.substring(0, path.length() - 5));

            //nbt
            CompoundTag nbt;
            try {
                nbt = NbtIo.readCompressed(cem.getValue().open());
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to load " + id + " avatar", e);
                continue;
            }

            //insert
            FiguraMod.LOGGER.info("Loaded CEM model for " + id);
            CEM_AVATARS.put(id, nbt);
        }
    });

    static {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to initialize the watcher service", e);
        }
    }

    protected static void async(Runnable toRun) {
        if (tasks == null || tasks.isDone()) {
            tasks = CompletableFuture.runAsync(toRun);
        } else {
            tasks.thenRun(toRun);
        }
    }

    /**
     * Loads an NbtCompound from the specified path
     *
     * @param path - the file/folder for loading the avatar
     */
    public static void loadAvatar(Path path, UserData target) {
        loadError = null;
        loadState = 0;
        resetWatchKeys();
        lastLoadedPath = path;
        addWatchKey(path, KEYS::put);

        if (path == null || target == null)
            return;

        async(() -> {
            try {
                //load as nbt (.moon)
                loadState++;
                if (path.toString().endsWith(".moon")) {
                    //NbtIo already closes the file stream
                    target.loadAvatar(NbtIo.readCompressed(new FileInputStream(path.toFile())));
                    return;
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
                CompoundTag textures = new CompoundTag();
                ListTag animations = new ListTag();
                BlockbenchModelParser modelParser = new BlockbenchModelParser();

                loadState++;
                CompoundTag models = loadModels(path.toFile().getCanonicalPath(), path, modelParser, textures, animations, "");
                models.putString("name", "models");

                //metadata
                loadState++;
                String metadata = IOUtils.readFile(path.resolve("avatar.json").toFile());
                nbt.put("metadata", AvatarMetadataParser.parse(metadata, path.getFileName().toString()));
                AvatarMetadataParser.injectToModels(metadata, models);
                AvatarMetadataParser.injectToTextures(metadata, textures);

                //return :3
                if (!models.isEmpty())
                    nbt.put("models", models);
                if (!textures.isEmpty())
                    nbt.put("textures", textures);
                if (!animations.isEmpty())
                    nbt.put("animations", animations);

                //load
                target.loadAvatar(nbt);
            } catch (Exception e) {
                loadError = e.getMessage();
                FiguraMod.LOGGER.error("Failed to load avatar from " + path, e);
                FiguraToast.sendToast(FiguraText.of("toast.load_error"), FiguraText.of("gui.load_error." + LocalAvatarLoader.getLoadState()), FiguraToast.ToastType.ERROR);
            }
        });
    }

    private static void loadScripts(Path path, CompoundTag nbt) throws IOException {
        List<File> scripts = IOUtils.getFilesByExtension(path, ".lua");
        if (scripts.size() > 0) {
            CompoundTag scriptsNbt = new CompoundTag();
            String pathRegex = Pattern.quote(path + File.separator);
            for (File script : scripts) {
                String name = script.toPath().toString()
                        .replaceFirst(pathRegex, "")
                        .replaceAll("[/\\\\]", ".");
                name = name.substring(0, name.length() - 4);
                scriptsNbt.put(name, LuaScriptParser.parseScript(name, IOUtils.readFile(script)));
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
                String name = sound.toPath().toString()
                        .replaceFirst(pathRegex, "")
                        .replaceAll("[/\\\\]", ".");
                name = name.substring(0, name.length() - 4);
                soundsNbt.putByteArray(name, IOUtils.readFileBytes(sound));
            }
            nbt.put("sounds", soundsNbt);
        }
    }

    private static CompoundTag loadModels(String avatarFolder, Path currentFile, BlockbenchModelParser parser, CompoundTag textures, ListTag animations, String folders) throws Exception {
        CompoundTag result = new CompoundTag();
        File[] subFiles = currentFile.toFile().listFiles(f -> !f.isHidden() && !f.getName().startsWith("."));
        ListTag children = new ListTag();
        if (subFiles != null)
            for (File file : subFiles) {
                String name = file.getName();
                if (file.isDirectory()) {
                    CompoundTag subfolder = loadModels(avatarFolder, file.toPath(), parser, textures, animations, folders + name + ".");
                    if (!subfolder.isEmpty()) {
                        subfolder.putString("name", name);
                        BlockbenchModelParser.parseParent(name, subfolder);
                        children.add(subfolder);
                    }
                } else if (file.toString().toLowerCase().endsWith(".bbmodel")) {
                    BlockbenchModelParser.ModelData data = parser.parseModel(avatarFolder, file, IOUtils.readFile(file), name.substring(0, name.length() - 8), folders);
                    children.add(data.modelNbt());
                    animations.addAll(data.animationList());

                    CompoundTag dataTag = data.textures();
                    if (dataTag.isEmpty())
                        continue;

                    if (textures.isEmpty()) {
                        textures.put("data", new ListTag());
                        textures.put("src", new CompoundTag());
                    }

                    textures.getList("data", Tag.TAG_COMPOUND).addAll(dataTag.getList("data", Tag.TAG_COMPOUND));
                    textures.getCompound("src").merge(dataTag.getCompound("src"));
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
            IOUtils.createDirIfNeeded(directory);
            NbtIo.writeCompressed(nbt, new FileOutputStream(file.toFile()));
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to save avatar: " + file.getFileName().toString(), e);
        }
    }

    /**
     * Tick the watched key for hotswapping avatars
     */
    public static void tick() {
        WatchEvent<?> event = null;
        boolean reload = false;

        for (Map.Entry<Path, WatchKey> entry : KEYS.entrySet()) {
            WatchKey key = entry.getValue();
            if (!key.isValid())
                continue;

            for (WatchEvent<?> watchEvent : key.pollEvents()) {
                WatchEvent.Kind<?> kind = watchEvent.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW)
                    continue;

                event = watchEvent;
                File file = entry.getKey().resolve(((WatchEvent<Path>) event).context()).toFile();
                String name = file.getName();

                if (file.isHidden() || name.startsWith(".") || (!file.isDirectory() && !name.matches("(.*(\\.lua|\\.bbmodel|\\.ogg|\\.png)$|avatar\\.json)")))
                    continue;

                if (kind == StandardWatchEventKinds.ENTRY_CREATE && !IS_WINDOWS)
                    addWatchKey(file.toPath(), KEYS::put);

                reload = true;
                break;
            }

            if (reload)
                break;
        }

        //reload avatar
        if (reload) {
            FiguraMod.debug("Detected file changes in the Avatar directory (" + event.context().toString() + "), reloading!");
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
     *
     * @param path the path to register the watch key
     * @param consumer a consumer that will process the watch key and its path
     */
    protected static void addWatchKey(Path path, BiConsumer<Path, WatchKey> consumer) {
        if (watcher == null || path == null)
            return;

        File file = path.toFile();
        if (!file.isDirectory() || file.isHidden() || file.getName().startsWith("."))
            return;

        try {
            WatchEvent.Kind<?>[] events = {StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY};
            WatchKey key = IS_WINDOWS ? path.register(watcher, events, com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE) : path.register(watcher, events);

            consumer.accept(path, key);

            File[] children = file.listFiles();
            if (children == null || IS_WINDOWS)
                return;

            for (File child : children)
                addWatchKey(child.toPath(), consumer);
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

    public static String getLoadError() {
        return loadError;
    }
}
