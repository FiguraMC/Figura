package org.moon.figura.avatar.local;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.cards.CardBackground;
import org.moon.figura.parsers.AvatarMetadataParser;
import org.moon.figura.utils.FileTexture;
import org.moon.figura.utils.IOUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Navigates through the file system, finding all folders
 * containing avatar.json as well as all .moon files.
 */
public class LocalAvatarFetcher {

    /**
     * After calling load(), this is an AvatarFolder that contains
     * the whole filesystem of avatars.
     */
    public static final List<AvatarPath> ALL_AVATARS = new ArrayList<>();
    private static final Map<String, Properties> SAVED_DATA = new HashMap<>();

    private static final Map<Path, WatchKey> WATCHED_KEYS = new HashMap<>();

    /**
     * Clears out the root AvatarFolder, and regenerates it from the
     * file system.
     */
    public static void loadAvatars() {
        //clear loaded avatars
        ALL_AVATARS.clear();

        //load avatars
        FolderPath root = new FolderPath(getLocalAvatarDirectory());
        root.fetch();

        //add new avatars
        ALL_AVATARS.addAll(root.getChildren());

        FiguraMod.debug("Reloading Avatar List...");
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
            loadAvatars();
    }

    public static void init() {
        load();
        LocalAvatarLoader.addWatchKey(getLocalAvatarDirectory(), WATCHED_KEYS::put);
    }

    /**
     * Loads the folder data from the disk
     * the folder data contains information about the avatar folders
     */
    public static void load() {
        IOUtils.readCacheFile("avatars", nbt -> {
            //loading
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
        if (FiguraMod.DEBUG_MODE && path.toString().toLowerCase().endsWith(".moon"))
            return true;

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

    /**
     * Represents a path which contains an avatar.
     */
    public static class AvatarPath {

        protected final Path path, folder; // murder, why does everything needs to be protected/private :sob: 
        protected final String name, description;
        protected final CardBackground background;
        protected final FileTexture iconTexture;

        protected Properties properties;

        protected AvatarPath(Path path, Path folder, String name) {
            this.path = path;
            this.folder = folder;
            this.name = name;
            this.description = "";
            this.background = CardBackground.DEFAULT;
            this.iconTexture = null;
            this.properties = SAVED_DATA.computeIfAbsent(path.toAbsolutePath().toString(), __ -> new Properties());
        }

        public AvatarPath(Path path, Path folder) {
            this.path = path;
            this.folder = folder;

            properties = SAVED_DATA.computeIfAbsent(path.toAbsolutePath().toString(), __ -> new Properties());

            String filename = IOUtils.getFileNameOrEmpty(path);

            String name = filename;
            String description = "";
            CardBackground bg = CardBackground.DEFAULT;
            FileTexture iconTexture = null;

            if (!path.toString().toLowerCase().endsWith(".moon") && !(this instanceof FolderPath)) {
                //metadata
                try {
                    String str = IOUtils.readFile(path.resolve("avatar.json"));
                    AvatarMetadataParser.Metadata metadata = AvatarMetadataParser.read(str);

                    name = Configs.WARDROBE_FILE_NAMES.value || metadata.name == null || metadata.name.isBlank() ? filename : metadata.name;
                    description = metadata.description == null ? "" : metadata.description;
                    bg = CardBackground.parse(metadata.background);
                } catch (Exception ignored) {}

                //icon
                try {
                    Path p = path.resolve("avatar.png");
                    if (Files.exists(p))
                        iconTexture = FileTexture.of(p);
                } catch (Exception ignored) {}
            }

            this.name = name;
            this.description = description;
            this.background = bg;
            this.iconTexture = iconTexture;
        }

        public boolean search(String query) {
            String q = query.toLowerCase();
            return this.getName().toLowerCase().contains(q) || IOUtils.getFileNameOrEmpty(path).contains(q);
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
            super(fileSystem.getPath(""), folder, IOUtils.getFileNameOrEmpty(path));
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

            //iterate over all files on this path
            //but skip non-folders and non-moon
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
                        if ("jar".equalsIgnoreCase(opened.provider().getScheme())){
                            Path newPath = opened.getPath("");
                            if (isAvatar(newPath)) {
                                children.add(new AvatarPath(newPath, folderPath));
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
                    } catch (IOException ignored) {
                    }
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
