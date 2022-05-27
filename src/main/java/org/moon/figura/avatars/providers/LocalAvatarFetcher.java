package org.moon.figura.avatars.providers;

import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

        public AvatarPath(Path path) {
            this.path = path;
            hasAvatar = Files.exists(path.resolve("avatar.json")) || path.toString().endsWith(".moon");
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
    }
}
