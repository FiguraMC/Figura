package org.moon.figura.avatars.providers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.cards.CardBackground;
import org.moon.figura.parsers.AvatarMetadataParser;
import org.moon.figura.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    private static final HashMap<String, Boolean> FOLDER_DATA = new HashMap<>();

    /**
     * Clears out the root AvatarFolder, and regenerates it from the
     * file system.
     */
    public static void load() {
        //clear loaded avatars
        ALL_AVATARS.clear();

        //load avatars, however we do not want to accept avatars in the root folder,
        //so we skip right into the children loading
        AvatarPath root = new AvatarPath(getLocalAvatarDirectory());
        root.fill(true);

        //add new avatars
        ALL_AVATARS.addAll(root.getChildren());
    }

    /**
     * Loads the folder data from the disk
     * the folder data contains information about the avatar folders
     */
    public static void init() {
        try {
            //get file
            Path targetPath = FiguraMod.getCacheDirectory().resolve("folders.nbt");

            if (!Files.exists(targetPath))
                return;

            //read file
            FileInputStream fis = new FileInputStream(targetPath.toFile());
            CompoundTag nbt = NbtIo.readCompressed(fis);

            //loading
            ListTag groupList = nbt.getList("folders", Tag.TAG_COMPOUND);
            for (Tag tag : groupList) {
                CompoundTag compound = (CompoundTag) tag;

                String path = compound.getString("path");
                boolean expanded = compound.getBoolean("expanded");
                FOLDER_DATA.put(path, expanded);
            }

            fis.close();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }

    /**
     * Saves the folder data to disk
     */
    public static void save() {
        try {
            //get nbt
            CompoundTag nbt = new CompoundTag();
            ListTag list = new ListTag();

            for (Map.Entry<String, Boolean> entry : FOLDER_DATA.entrySet()) {
                CompoundTag compound = new CompoundTag();
                compound.putString("path", entry.getKey());
                compound.putBoolean("expanded", entry.getValue());
                list.add(compound);
            }

            nbt.put("folders", list);

            //create file
            Path targetPath = FiguraMod.getCacheDirectory().resolve("folders.nbt");

            if (!Files.exists(targetPath))
                Files.createFile(targetPath);

            //write file
            FileOutputStream fs = new FileOutputStream(targetPath.toFile());
            NbtIo.writeCompressed(nbt, fs);
            fs.close();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }

    /**
     * Returns the directory where all local avatars are stored.
     * The directory is always under main directory.
     */
    public static Path getLocalAvatarDirectory() {
        Path p = FiguraMod.getFiguraDirectory().resolve("avatars");
        try {
            Files.createDirectories(p);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to create avatar directory", e);
        }

        return p;
    }

    /**
     * Represents a path which (perhaps indirectly) contains an avatar.
     * Either this AvatarPath itself contains an avatar, in which case
     * hasAvatar will be true, or one of its children contains an avatar.
     */
    public static class AvatarPath {

        private final boolean hasAvatar;
        private final List<AvatarPath> children = new ArrayList<>();
        private final Path path;

        //metadata
        private final String name;
        private final CardBackground background;

        private boolean expanded;

        public AvatarPath(Path path) {
            this.path = path;
            Path folderPath = path.resolve("avatar.json");
            boolean isMoonFile = path.toString().endsWith(".moon") && !Files.isDirectory(path);
            this.hasAvatar = (Files.exists(folderPath) && !Files.isDirectory(folderPath)) || isMoonFile;

            String filename = path.getFileName().toString();
            String name;
            CardBackground bg;

            if (!hasAvatar || isMoonFile) {
                name = filename;
                bg = CardBackground.DEFAULT;
            } else {
                try {
                    String str = IOUtils.readFile(path.resolve("avatar.json").toFile());
                    AvatarMetadataParser.Metadata metadata = AvatarMetadataParser.read(str);

                    name = metadata.name == null ? filename : metadata.name;
                    bg = CardBackground.parse(metadata.background);
                } catch (Exception e) {
                    FiguraMod.LOGGER.warn("Failed to read metadata for \"" + path + "\"", e);
                    name = filename;
                    bg = CardBackground.DEFAULT;
                }
            }

            this.name = name;
            this.background = bg;

            String absPath = path.toFile().getAbsolutePath();
            this.expanded = !FOLDER_DATA.containsKey(absPath) || FOLDER_DATA.get(absPath);
        }

        /**
         * Recursively traverses the filesystem looking for avatars under this folder.
         * @param ignoreAvatars Whether the search should ignore avatar.json files
         *                      inside itself. This is used on the root folder call, as we
         *                      don't want "figura/avatars/avatar.json" to work, it needs to
         *                      be in a subfolder.
         * @return Whether we found an avatar in our recursive searching. If we didn't, then
         * this folder can get ignored and not added as a child in another folder. We only want
         * our AvatarFolder to contain sub-folders that actually have avatars.
         */
        public boolean fill(boolean ignoreAvatars) {
            //do not fill the children if we already found an avatar
            if (hasAvatar && !ignoreAvatars)
                return true;

            File[] files = path.toFile().listFiles();
            if (files == null)
                return false;

            //iterate over all files on this path
            //but skip non-folders and non-moon
            boolean foundAvatar = false;
            for (File file : files) {
                if (!file.isDirectory() && !file.getName().endsWith(".moon"))
                    continue;

                //attempt to load avatars from subfolder
                AvatarPath folder = new AvatarPath(file.toPath());
                boolean foundAvatarHere = folder.fill(false);
                foundAvatar |= foundAvatarHere;
                if (foundAvatarHere)
                    children.add(folder);
            }

            return foundAvatar;
        }

        public List<AvatarPath> getChildren() {
            return children;
        }

        public Path getPath() {
            return path;
        }

        public boolean hasAvatar() {
            return hasAvatar;
        }

        public String getName() {
            return name;
        }

        public CardBackground getBackground() {
            return background;
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
            if (!this.hasAvatar) {
                String key = this.path.toFile().getAbsolutePath();
                if (!this.expanded)
                    FOLDER_DATA.put(key, false);
                else
                    FOLDER_DATA.remove(key);
            }
        }
    }
}
