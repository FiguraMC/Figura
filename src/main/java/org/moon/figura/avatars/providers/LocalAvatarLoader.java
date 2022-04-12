package org.moon.figura.avatars.providers;

import org.moon.figura.FiguraMod;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//class that loads avatars from the file system
//allows folders or .moon (nbt)
public class LocalAvatarLoader {

    public static AvatarFolder allAvatars;

    public static void load() {
        //reset all avatars
        allAvatars = new AvatarFolder(FiguraMod.getLocalAvatarDirectory());

        //we do not want to accept avatars in the root folder,
        //so we skip right into the children loading
        allAvatars.fill(true);
    }

    public static class AvatarFolder {

        private final boolean hasAvatar;
        private final List<AvatarFolder> children = new ArrayList<>();
        private final Path path;

        public AvatarFolder(Path path) {
            this.path = path;
            hasAvatar = Files.exists(path.resolve("avatar.json")) || path.toString().endsWith(".moon");
        }

        public List<AvatarFolder> getChildren() {
            return children;
        }
        public Path getPath() {
            return path;
        }
        public boolean hasAvatar() {
            return hasAvatar;
        }

        public boolean fill(boolean ignoreAvatars) {
            if (ignoreAvatars || !hasAvatar) {
                boolean foundAvatar = false;
                File[] files = path.toFile().listFiles();
                if (files != null)
                    for (File file : files) {
                        if (file.isDirectory() || file.getName().endsWith(".moon")) {
                            AvatarFolder folder = new AvatarFolder(file.toPath());
                            boolean foundAvatarHere = folder.fill(false);
                            foundAvatar |= foundAvatarHere;
                            if (foundAvatarHere)
                                children.add(folder);
                        }
                    }
                return foundAvatar;
            }
            return true;
        }
    }
}
