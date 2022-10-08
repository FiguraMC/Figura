package org.moon.figura.avatar.local;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.cards.CardBackground;
import org.moon.figura.parsers.AvatarMetadataParser;
import org.moon.figura.utils.IOUtils;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final Map<String, Boolean> FOLDER_DATA = new HashMap<>();

    /**
     * Clears out the root AvatarFolder, and regenerates it from the
     * file system.
     */
    public static void load() {
        //clear loaded avatars
        ALL_AVATARS.clear();

        //load avatars
        FolderPath root = new FolderPath(getLocalAvatarDirectory());
        root.fetch();

        //add new avatars
        ALL_AVATARS.addAll(root.getChildren());
    }

    /**
     * Loads the folder data from the disk
     * the folder data contains information about the avatar folders
     */
    public static void init() {
        IOUtils.readCacheFile("folders", nbt -> {
            //loading
            ListTag groupList = nbt.getList("folders", Tag.TAG_COMPOUND);
            for (Tag tag : groupList) {
                CompoundTag compound = (CompoundTag) tag;

                String path = compound.getString("path");
                boolean expanded = compound.getBoolean("expanded");
                FOLDER_DATA.put(path, expanded);
            }
        });
    }

    /**
     * Saves the folder data to disk
     */
    public static void save() {
        IOUtils.saveCacheFile("folders", nbt -> {
            ListTag list = new ListTag();

            for (Map.Entry<String, Boolean> entry : FOLDER_DATA.entrySet()) {
                CompoundTag compound = new CompoundTag();
                compound.putString("path", entry.getKey());
                compound.putBoolean("expanded", entry.getValue());
                list.add(compound);
            }

            nbt.put("folders", list);
        });
    }

    /**
     * Returns the directory where all local avatars are stored.
     * The directory is always under main directory.
     */
    public static Path getLocalAvatarDirectory() {
        Path p = FiguraMod.getFiguraDirectory().resolve("avatars");
        try {
            Files.createDirectories(p);
        } catch (FileAlreadyExistsException ignored) {
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to create avatar directory", e);
        }

        return p;
    }

    /**
     * Represents a path which contains an avatar.
     */
    public static class AvatarPath {

        protected final Path path;
        protected final String name;
        protected final CardBackground background;

        public AvatarPath(Path path) {
            this.path = path;
            String filename = path.getFileName().toString();

            String name;
            CardBackground bg;

            if (path.toString().toLowerCase().endsWith(".moon") || this instanceof FolderPath) {
                name = filename;
                bg = CardBackground.DEFAULT;
            } else {
                try {
                    String str = IOUtils.readFile(path.resolve("avatar.json").toFile());
                    AvatarMetadataParser.Metadata metadata = AvatarMetadataParser.read(str);

                    name = metadata.name == null || metadata.name.isBlank() ? filename : metadata.name;
                    bg = CardBackground.parse(metadata.background);
                } catch (Exception e) {
                    FiguraMod.LOGGER.warn("Failed to read metadata for \"" + path + "\". Likely invalid avatar.json.");
                    name = filename;
                    bg = CardBackground.DEFAULT;
                }
            }

            this.name = name;
            this.background = bg;
        }

        public boolean search(String query) {
            return this.getName().toLowerCase().contains(query.toLowerCase());
        }

        public Path getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        public CardBackground getBackground() {
            return background;
        }
    }

    /**
     * Represents a path were its sub paths contains an avatar.
     */
    public static class FolderPath extends AvatarPath {

        protected final List<AvatarPath> children = new ArrayList<>();
        protected boolean expanded = true;

        public FolderPath(Path path) {
            super(path);

            Boolean expanded = FOLDER_DATA.get(this.path.toFile().getAbsolutePath());
            if (expanded != null)
                this.expanded = expanded;
        }

        /**
         * Recursively traverses the filesystem looking for avatars under this folder.
         * @return Whether we found an avatar in our recursive searching.
         * Either in this folder or in one of its sub folders.
         * If we didn't, then this folder can get ignored and not added as a child in another folder.
         * We only want our FolderPath to contain sub-folders that actually have avatars.
         */
        public boolean fetch() {
            File[] files = path.toFile().listFiles();
            if (files == null)
                return false;

            boolean found = false;

            //iterate over all files on this path
            //but skip non-folders and non-moon
            for (File file : files) {
                Path path = file.toPath();
                boolean moon = path.toString().toLowerCase().endsWith(".moon");

                if (!Files.isDirectory(path) && !moon)
                    continue;

                Path metadata = path.resolve("avatar.json");
                if (moon || (Files.exists(metadata) && !Files.isDirectory(metadata))) {
                    children.add(new AvatarPath(path));
                    found = true;
                } else {
                    FolderPath folder = new FolderPath(file.toPath());
                    if (folder.fetch()) {
                        children.add(folder);
                        found = true;
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

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;

            String key = this.path.toFile().getAbsolutePath();
            if (!this.expanded) FOLDER_DATA.put(key, false);
            else FOLDER_DATA.remove(key);
        }
    }
}
