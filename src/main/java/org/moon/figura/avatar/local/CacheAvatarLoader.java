package org.moon.figura.avatar.local;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.UserData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

public class CacheAvatarLoader {

    public static void init() {
        LocalAvatarLoader.async(() -> {
            File file = getAvatarCacheDirectory().toFile();
            if (!file.exists() || !file.isDirectory())
                return;

            File[] children = file.listFiles();
            if (children == null)
                return;

            for (File child : children) {
                try {
                    FileTime time = Files.getLastModifiedTime(child.toPath());
                    long diff = System.currentTimeMillis() - time.toMillis();
                    long elapsed = TimeUnit.MILLISECONDS.toDays(diff);
                    if (elapsed > 7) {
                        if (child.delete()) {
                            FiguraMod.debug("Successfully deleted cache avatar \"{}\" with \"{}\" days old", child.getName(), elapsed);
                        } else {
                            throw new Exception();
                        }
                    }
                } catch (Exception ignored) {
                    FiguraMod.debug("Failed to delete cache avatar \"{}\"", child.getName());
                }
            }
        });
    }

    public static boolean checkAndLoad(String hash, UserData target) {
        Path p = getAvatarCacheDirectory();
        p = p.resolve(hash + ".moon");

        if (Files.exists(p)) {
            load(hash, target);
            return true;
        }

        return false;
    }

    public static void load(String hash, UserData target) {
        LocalAvatarLoader.async(() -> {
            Path path = getAvatarCacheDirectory().resolve(hash + ".moon");
            try {
                target.loadAvatar(NbtIo.readCompressed(new FileInputStream(path.toFile())));
                FiguraMod.debug("Loaded avatar \"{}\" from cache to \"{}\"", hash, target.id);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to load cache avatar: " + hash, e);
            }
        });
    }

    public static void save(String hash, CompoundTag nbt) {
        LocalAvatarLoader.async(() -> {
            Path file = getAvatarCacheDirectory().resolve(hash + ".moon");
            try {
                NbtIo.writeCompressed(nbt, new FileOutputStream(file.toFile()));
                FiguraMod.debug("Saved avatar \"{}\" on cache", hash);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to save avatar on cache: " + hash, e);
            }
        });
    }

    public static void clearCache() {
        LocalAvatarLoader.async(() -> {
            File file = getAvatarCacheDirectory().toFile();

            if (!file.exists() || !file.isDirectory())
                return;

            File[] children = file.listFiles();
            if (children == null)
                return;

            for (File child : children) {
                try {
                    if (!child.delete())
                        throw new Exception();
                } catch (Exception ignored) {
                    FiguraMod.debug("Failed to delete cache avatar \"{}\"", child.getName());
                }
            }

            FiguraMod.debug("Finished clearing avatar cache");
        });
    }

    //cache directory
    public static Path getAvatarCacheDirectory() {
        Path p = FiguraMod.getCacheDirectory().resolve("avatars");
        try {
            Files.createDirectories(p);
        } catch (FileAlreadyExistsException ignored) {
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to create avatar cache directory", e);
        }

        return p;
    }
}
