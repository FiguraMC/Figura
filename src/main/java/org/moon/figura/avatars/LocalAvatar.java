package org.moon.figura.avatars;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.providers.LocalAvatarFetcher;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class that controls the local player avatar
 */
public class LocalAvatar extends Avatar {

    public final NbtCompound nbt;
    private boolean uploaded = false;

    public LocalAvatar(NbtCompound nbt) {
        super(nbt);
        this.nbt = nbt;
    }

    /**
     * Saves the avatar NBT into a folder inside the avatar list
     */
    public void saveNbt() {
        Path directory = LocalAvatarFetcher.getLocalAvatarDirectory().resolve("[§9Figura§r] Cached Avatars");
        Path file = directory.resolve("cache-" + new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date()) + ".moon");
        try {
            Files.createDirectories(directory);
            NbtIo.writeCompressed(nbt, new FileOutputStream(file.toFile()));
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to save avatar: " + file.getFileName().toString());
            FiguraMod.LOGGER.error(e);
        }
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }
}
