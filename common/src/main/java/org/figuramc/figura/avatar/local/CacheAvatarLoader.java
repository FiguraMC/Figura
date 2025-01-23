package org.figuramc.figura.avatar.local;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.UserData;
import org.figuramc.figura.utils.IOUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CacheAvatarLoader {

    public static void init() {
        LocalAvatarLoader.async(() -> {
            Path file = getAvatarCacheDirectory();
            if (!(Files.exists(file) && Files.isDirectory(file)))
                return;

            List<Path> children = IOUtils.listPaths(file);
            if (children == null)
                return;

        });
    }

    public static boolean checkAndLoad(String hash, UserData target) {
        Path p = getAvatarCacheDirectory();
        p = p.resolve(hash + ".nbt");

        if (Files.exists(p)) {
            load(hash, target);
            return true;
        }

        return false;
    }

    public static void load(String hash, UserData target) {
        LocalAvatarLoader.async(() -> {
            Path path = getAvatarCacheDirectory().resolve(hash + ".nbt");
            try {
                target.loadAvatar(NbtIo.readCompressed(Files.newInputStream(path)));
                debugAndChat("Loaded avatar \"{}\" from cache to \"{}\"", hash, target.id);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to load cache avatar: " + hash, e);
            }
        });
    }

    public static void save(String hash, CompoundTag nbt) {
        LocalAvatarLoader.async(() -> {
            Path file = getAvatarCacheDirectory().resolve(hash + ".nbt");
            try {
                NbtIo.writeCompressed(nbt, Files.newOutputStream(file));
                debugAndChat("Saved avatar \"{}\" on cache", hash);
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to save avatar on cache: " + hash, e);
            }
        });
    }

    public static void clearCache() {
        LocalAvatarLoader.async(() -> {
            Path file = getAvatarCacheDirectory();

            if (!(Files.exists(file) && Files.isDirectory(file)))
                return;

            List<Path> children = IOUtils.listPaths(file);
            if (children == null)
                return;

            for (Path child : children) {
                try {
                    if (!Files.deleteIfExists(child))
                        throw new Exception();
                } catch (Exception ignored) {
                    debugAndChat("Failed to delete cache avatar \"{}\"", IOUtils.getFileNameOrEmpty(child));
                }
            }

            debugAndChat("Finished clearing avatar cache");
        });
    }

    // cache directory
    public static Path getAvatarCacheDirectory() {
        return IOUtils.getOrCreateDir(FiguraMod.getCacheDirectory(), "avatars");
    }

    // Helper method to send debug messages to both log and chat
    private static void debugAndChat(String message, Object... args) {
        String formattedMessage = String.format(message, args);

        // Log the message to game logs
        FiguraMod.debug(formattedMessage);

        // Send the message to player chat
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(Component.literal(formattedMessage));
        }
    }
}
