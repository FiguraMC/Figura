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
        allAvatars.getChildren().addAll(loadChildren(allAvatars));
    }

    private static List<AvatarPath> loadFromDirectory(AvatarFolder parent) {
        ArrayList<AvatarPath> avatars = new ArrayList<>();
        Path path = parent.getPath();

        //attempt to load single
        if (hasAvatar(path))
            avatars.add(new AvatarPath(path));

        //attempt to load children
        avatars.addAll(loadChildren(parent));

        return avatars;
    }

    private static List<AvatarPath> loadChildren(AvatarFolder parent) {
        ArrayList<AvatarPath> avatars = new ArrayList<>();
        File[] files = parent.getPath().toFile().listFiles();

        if (files == null) return avatars;

        for (File file : files) {
            if (!file.isDirectory())
                continue;

            AvatarFolder avatarPath = new AvatarFolder(file.toPath());

            //load from folder
            avatarPath.children.addAll(loadFromDirectory(avatarPath));

            //load from nbt
            //TODO

            //add to return list
            if (!avatarPath.children.isEmpty())
                avatars.add(avatarPath);
        }

        return avatars;
    }

    private static boolean hasAvatar(Path dir) {
        dir = dir.resolve("avatar.json");
        return Files.exists(dir);
    }

    public static class AvatarPath {

        private final Path path;

        public AvatarPath(Path path) {
            this.path = path;
        }

        public Path getPath() {
            return path;
        }
    }

    public static class AvatarFolder extends AvatarPath {

        private final List<AvatarPath> children = new ArrayList<>();

        public AvatarFolder(Path path) {
            super(path);
        }

        public List<AvatarPath> getChildren() {
            return children;
        }
    }
}
