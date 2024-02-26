package org.figuramc.figura.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.figuramc.figura.FiguraMod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IOUtils {

    public static final String INVALID_FILENAME_REGEX = "CON|PRN|AUX|NUL|COM\\d|LPT\\d|[\\\\/:*?\"<>|\u0000]|\\.$";

    public static List<Path> getFilesByExtension(Path root, String extension) {
        List<Path> result = new ArrayList<>();
        List<Path> children = listPaths(root);
        if (children == null) return result;
        for (Path child : children) {
            if (IOUtils.isHidden(child))
                continue;

            if (Files.isDirectory(child))
                result.addAll(getFilesByExtension(child, extension));
            else if (child.toString().toLowerCase(Locale.US).endsWith(extension.toLowerCase(Locale.US)))
                result.add(child);
        }
        return result;
    }

    public static String readFile(Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            FiguraMod.LOGGER.error("Failed to read File: " + file);
            throw e;
        }
    }

    public static byte[] readFileBytes(Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            return stream.readAllBytes();
        } catch (IOException e) {
            FiguraMod.LOGGER.error("Failed to read File: " + file);
            throw e;
        }
    }

    public static void readCacheFile(String name, Consumer<CompoundTag> consumer) {
        try {
            // get file
            Path path = FiguraMod.getCacheDirectory().resolve(name + ".nbt");

            if (!Files.exists(path))
                return;

            // read file
            InputStream fis = Files.newInputStream(path);
            CompoundTag nbt = NbtIo.readCompressed(fis, NbtAccounter.unlimitedHeap());
            consumer.accept(nbt);
            fis.close();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }

    public static void saveCacheFile(String name, Consumer<CompoundTag> consumer) {
        try {
            // get nbt
            CompoundTag nbt = new CompoundTag();
            consumer.accept(nbt);

            // create file
            Path path = FiguraMod.getCacheDirectory().resolve(name + ".nbt");

            if (!Files.exists(path))
                Files.createFile(path);

            // write file
            OutputStream fs = Files.newOutputStream(path);
            NbtIo.writeCompressed(nbt, fs);
            fs.close();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }

    public static void deleteCacheFile(String name) {
        Path path = FiguraMod.getCacheDirectory().resolve(name + ".nbt");
        deleteFile(path);
    }

    public static Path getOrCreateDir(Path startingPath, String dir) {
        return createDirIfNeeded(startingPath.resolve(dir));
    }

    public static Path createDirIfNeeded(Path path) {
        try {
            Files.createDirectories(path);
        } catch (FileAlreadyExistsException ignored) {
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to create directory", e);
        }

        return path;
    }

    public static void deleteFile(Path file) {
        try {
            if(!Files.exists(file))
                return;

            List<Path> paths = listPaths(file);
            if (paths != null) {
                for (Path path : paths) {
                    deleteFile(path);
                }
            }
            Files.delete(file);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to delete " + file, e);
        }
    }

    public static void writeFile(Path path, byte[] data) throws IOException {
        if (data == null)
            return;

        try (OutputStream fs = Files.newOutputStream(path)) {
            fs.write(data);
        }
    }

    public static List<Path> listPaths(Path dir) {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.sorted(Comparator.comparing(IOUtils::getFileNameOrEmpty)).collect(Collectors.toList());
        } catch (IOException ioe) {
            return null;
        }
    }

    public static String getFileNameOrEmpty(Path path) {
        Path filename = path.getFileName();
        return filename == null ? "" : filename.toString();
    }

    public static boolean isHidden(Path path) {
        boolean hidden;
        try {
            hidden = Files.isHidden(path);
        } catch (IOException e) {
            FiguraMod.LOGGER.error("Failed to get if \"" + path + "\" is hidden", e);
            hidden = false;
        }
        return hidden || getFileNameOrEmpty(path).startsWith(".");
    }

    public static class DirWrapper {
        private final Path path;

        public DirWrapper(Path path) {
            this.path = path;
        }

        public DirWrapper create() {
            createDirIfNeeded(path);
            return this;
        }

        public DirWrapper write(String relativePath, byte[] data) throws IOException {
            writeFile(path.resolve(relativePath), data);
            return this;
        }
    }
}
