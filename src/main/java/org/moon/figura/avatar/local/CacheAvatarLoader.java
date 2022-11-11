package org.moon.figura.avatar.local;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.UserData;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CacheAvatarLoader {

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
