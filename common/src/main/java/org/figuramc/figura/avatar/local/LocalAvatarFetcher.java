package org.figuramc.figura.avatar.local;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.gui.cards.CardBackground;
import org.figuramc.figura.parsers.AvatarMetadataParser;
import org.figuramc.figura.utils.FileTexture;
import org.figuramc.figura.utils.IOUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Navigates through the file system, finding all folders containing avatar.json
 */
public class LocalAvatarFetcher {

    /**
     * After calling load(), this is an AvatarFolder that contains
     * the whole filesystem of avatars.
     */
    public static final List<AvatarPath> ALL_AVATARS = new ArrayList<>();
    private static final Map<String, Properties> SAVED_DATA = new HashMap<>();

    private static final Map<Path, WatchKey> WATCHED_KEYS = new HashMap<>();

    private static boolean requireReload = true, loaded;

    /**
     * Clears out the root AvatarFolder, and regenerates it from the
     * file system.
     */
    public static void loadAvatars() {
        loaded = false;
        FiguraMod.debug("Reloading Avatar List...");

        // load avatars
        FolderPath root = new FolderPath(getLocalAvatarDirectory());
        root.fetch();

        // add new avatars
        ALL_AVATARS.clear();
        ALL_AVATARS.addAll(root.getChildren());
        loaded = true;
    }

    public static CompletableFuture<Void> reloadAvatars() {
        if (!requireReload)
            return CompletableFuture.completedFuture(null);

        requireReload = false;
        return CompletableFuture.runAsync(LocalAvatarFetcher::loadAvatars);
    }

    public static void tick() {
        boolean reload = false;

        for (Map.Entry<Path, WatchKey> entry : WATCHED_KEYS.entrySet()) {
            WatchKey key = entry.getValue();
            if (!key.isValid())
                continue;

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW)
                    continue;

                if (kind == StandardWatchEventKinds.ENTRY_CREATE && !LocalAvatarLoader.IS_WINDOWS) {
                    Path child = entry.getKey().resolve((Path) event.context());
                    LocalAvatarLoader.addWatchKey(child, WATCHED_KEYS::put);
                }

                reload = true;
            }

            if (reload)
                break;
        }

        if (reload)
            requireReload = true;
    }

    public static void init() {
        load();
        LocalAvatarLoader.addWatchKey(getLocalAvatarDirectory(), WATCHED_KEYS::put);
    }

    public static void reinit() {
        WATCHED_KEYS.clear();
        SAVED_DATA.clear();
        ALL_AVATARS.clear();
        requireReload = true;
        loaded = false;
        init();
    }

    /**
     * Loads the folder data from the disk
     * the folder data contains information about the avatar folders
     */
    public static void load() {
        IOUtils.readCacheFile("avatars", nbt -> {
            // loading
            ListTag list = nbt.getList("properties", Tag.TAG_COMPOUND);
            for (Tag tag : list) {
                CompoundTag compound = (CompoundTag) tag;

                String path = compound.getString("path");
                Properties properties = new Properties();
                properties.expanded = compound.getBoolean("expanded");
                properties.favourite = compound.getBoolean("favourite");

                SAVED_DATA.put(path, properties);
            }
        });
    }

    /**
     * Saves the folder data to disk
     */
    public static void save() {
        IOUtils.saveCacheFile("avatars", nbt -> {
            ListTag properties = new ListTag();

            for (Map.Entry<String, Properties> entry : SAVED_DATA.entrySet()) {
                CompoundTag compound = new CompoundTag();

                Properties prop = entry.getValue();
                if (!prop.expanded)
                    compound.putBoolean("expanded", false);
                if (prop.favourite)
                    compound.putBoolean("favourite", true);

                if (!compound.isEmpty()) {
                    compound.putString("path", entry.getKey());
                    properties.add(compound);
                }
            }

            nbt.put("properties", properties);
        });
    }

    public static void clearCache() {
        IOUtils.deleteCacheFile("avatars");
    }

    /**
     * Returns the directory where all local avatars are stored.
     * The directory is always under main directory.
     */
    public static Path getLocalAvatarDirectory() {
        return IOUtils.getOrCreateDir(FiguraMod.getFiguraDirectory(), "avatars");
    }

    public static boolean isAvatar(Path path) {
        if (!Files.exists(path))
            return false;

        Path metadata = path.resolve("avatar.json");
        return Files.exists(metadata) && !Files.isDirectory(metadata);
    }

    public static void loadExternal(List<Path> paths) throws IOException {
        for (Path path : paths) {
            Path dest = getLocalAvatarDirectory();
            try (Stream<Path> stream = Files.walk(path)) {
                for (Path p : stream.toList()) {
                    Util.copyBetweenDirs(path.getParent(), dest, p);
                }
            }
        }
    }

    public static boolean isLoaded() {
        return loaded && !isReloadRequired();
    }

    public static boolean isReloadRequired() {
        return requireReload;
    }

    /**
     * Represents a path which contains an avatar.
     */
    public static class AvatarPath {

        // im going insane... or better saying, crazy, speaking of which, I was crazy once
        protected final Path path, folder, theActualPathForThis; // murder, why does everything needs to be protected/private :sob:
        protected final String name, description;
        protected final CardBackground background;
        protected Properties properties;
        // icon
        protected final Path iconPath;
        protected boolean iconLoaded;
        protected FileTexture iconTexture;

        protected AvatarPath(Path path, Path folder, Path theActualPathForThis, String name) {
            this.path = path;
            this.folder = folder;
            this.theActualPathForThis = theActualPathForThis;
            this.name = name;
            this.description = "";
            this.background = CardBackground.DEFAULT;
            this.iconPath = null;
            this.properties = SAVED_DATA.computeIfAbsent(path.toAbsolutePath().toString(), __ -> new Properties());
        }

        public AvatarPath(Path path, Path folder) {
            this(path, folder, path);
        }

        public AvatarPath(Path path, Path folder, Path theActualPathForThis) {
            this.path = path;
            this.folder = folder;
            this.theActualPathForThis = theActualPathForThis;

            properties = SAVED_DATA.computeIfAbsent(path.toAbsolutePath().toString(), __ -> new Properties());

            String filename = IOUtils.getFileNameOrEmpty(path);

            String name = filename;
            String description = "";
            CardBackground bg = CardBackground.DEFAULT;
            Path iconPath = null;

            if (!(this instanceof FolderPath)) {
                // metadata
                try {
                    String str = IOUtils.readFile(path.resolve("avatar.json"));
                    AvatarMetadataParser.Metadata metadata = AvatarMetadataParser.read(str);

                    name = Configs.WARDROBE_FILE_NAMES.value || metadata.name == null || metadata.name.isBlank() ? filename : metadata.name;
                    description = metadata.description == null ? "" : metadata.description;
                    bg = CardBackground.parse(metadata.background);
                } catch (Exception e) {
                    FiguraMod.LOGGER.error("Failed to load metadata for \"" + path + "\"", e);
                }

                // icon
                Path p = path.resolve("avatar.png");
                if (Files.exists(p))
                    iconPath = p;
            }

            this.name = name;
            this.description = description;
            this.background = bg;
            this.iconPath = iconPath;
        }

        public boolean search(String query) {
            String q = query.toLowerCase(Locale.US);
            return this.getName().toLowerCase(Locale.US).contains(q) || IOUtils.getFileNameOrEmpty(path).contains(q);
        }

        public Path getPath() {
            return path;
        }

        public Path getFolder() {
            return folder;
        }

        public Path getFSPath() {
            Path path = getPath();
            Path folder = getFolder();
            return path.getFileSystem() == folder.getFileSystem() ? path : folder;
        }

        public Path getTheActualPathForThis() {
            return theActualPathForThis;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public CardBackground getBackground() {
            return background;
        }

        public FileTexture getIcon() {
            if (!iconLoaded) {
                iconLoaded = true;
                try {
                    if (iconPath != null)
                        this.iconTexture = FileTexture.of(iconPath);
                } catch (Exception e) {
                    FiguraMod.LOGGER.error("Failed to load icon for \"" + path + "\"", e);
                }
            }
            return iconTexture;
        }

        public boolean isExpanded() {
            return properties.expanded;
        }

        public void setExpanded(boolean expanded) {
            properties.expanded = expanded;
            saveProperties();
        }

        public boolean isFavourite() {
            return properties.favourite;
        }

        public void setFavourite(boolean favourite) {
            properties.favourite = favourite;
            saveProperties();
        }

        private void saveProperties() {
            String key = this.path.toAbsolutePath().toString();
            SAVED_DATA.put(key, properties);
        }
    }

    /**
     * Represents a path which contains avatar(s) in it's sub-paths.
     */
    public static class FolderPath extends AvatarPath {

        protected final List<AvatarPath> children = new ArrayList<>();
        protected final FileSystem fileSystem;

        public FolderPath(FileSystem fileSystem, Path folder, Path path) {
            super(fileSystem.getPath(""), folder, path, IOUtils.getFileNameOrEmpty(path));
            this.fileSystem = fileSystem;
        }

        public FolderPath(Path path, Path folder) {
            super(path, folder);
            this.fileSystem = path.getFileSystem();
        }

        public FolderPath(Path path) {
            this(path, path);
        }

        /**
         * Recursively traverses the filesystem looking for avatars under this folder.
         * @return Whether we found an avatar in our recursive searching.
         * Either in this folder or in one of its sub folders.
         * If we didn't, then this folder can get ignored and not added as a child in another folder.
         * We only want our FolderPath to contain sub-folders that actually have avatars.
         */
        public boolean fetch() {
            List<Path> files = IOUtils.listPaths(getPath());
            if (files == null)
                return false;

            boolean found = false;

            Path folderPath = this.path.getFileSystem() == FileSystems.getDefault() ? path : this.folder;

            // iterate over all files on this path
            // but skip non-folders
            for (Path path : files) {
                if (isAvatar(path)) {
                    children.add(new AvatarPath(path, folderPath));
                    found = true;
                } else if (Files.isDirectory(path)) {
                    FolderPath folder = new FolderPath(path, folderPath);
                    if (folder.fetch()) {
                        children.add(folder);
                        found = true;
                    }
                } else if (IOUtils.getFileNameOrEmpty(path).endsWith(".zip")) {
                    try {
                        FileSystem opened = FileSystems.newFileSystem(path);
                        if ("jar".equalsIgnoreCase(opened.provider().getScheme())) {
                            Path newPath = opened.getPath("");
                            if (isAvatar(newPath)) {
                                children.add(new AvatarPath(newPath, folderPath, path));
                                found = true;
                            } else {
                                FolderPath folder = new FolderPath(opened, folderPath, path);
                                if (folder.fetch()) {
                                    children.add(folder);
                                    found = true;
                                } else
                                    opened.close();
                            }
                        } else
                            opened.close();
                    } catch (IOException ignored) {}
                }
            }

            return found;
        }

        @Override
        public boolean search(String query) {
            boolean result = super.search(query);

            for (AvatarPath child : children) {
                if (result) break;
                result = child.search(query);
            }

            return result;
        }

        public List<AvatarPath> getChildren() {
            return children;
        }
    }

    private static class Properties {
        public boolean expanded = true;
        public boolean favourite;
    }
}
