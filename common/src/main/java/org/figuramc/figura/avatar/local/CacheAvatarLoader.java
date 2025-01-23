package org.figuramc.figura.avatar.local;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.UserData;
import org.figuramc.figura.utils.IOUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CacheAvatarLoader {

    public static void init() {
        LocalAvatarLoader.async(() -> {
            Path file = getAvatarCacheDirectory();
            if (!(Files.exists(file) && Files.isDirectory(file)))
                return;

            List<Path> children = IOUtils.listPaths(file);
            if (children == null)
                return;

            sendDebugMessage("Initialized avatar cache with %d entries", children.size());
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
                sendDebugMessage(
                        "Loaded avatar \"%s\" from cache for player \"%s\"",
                        colorize(hash, TextColor.fromRgb(0x55FFFF))
                    );
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
                sendDebugMessage("Saved avatar \"%s\" on cache", hash);
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
                    FiguraMod.debug("Failed to delete cache avatar \"%s\"", IOUtils.getFileNameOrEmpty(child));
                }
            }

            FiguraMod.debug("Finished clearing avatar cache");
        });
    }
    
    // cache directory
    public static Path getAvatarCacheDirectory() {
        return IOUtils.getOrCreateDir(FiguraMod.getCacheDirectory(), "avatars");
    }

    private static void sendDebugMessage(String message, Object... args) {
        String formattedMessage = String.format(message, args);
        FiguraMod.debug(formattedMessage); // Send to the log

        // Send to in-game chat if a player is available
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("[DEBUG] " + formattedMessage));
        }
    }
        private static Component colorize(String text, TextColor color) {
        return Component.literal(text).setStyle(Style.EMPTY.withColor(color));
    }
}
