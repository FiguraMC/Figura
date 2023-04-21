package org.moon.figura.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.moon.figura.FiguraMod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IOUtils {

    public static final String INVALID_FILENAME_REGEX = "CON|PRN|AUX|NUL|COM\\d|LPT\\d|[\\\\/:*?\"<>|\u0000]|\\.$";

    public static List<File> getFilesByExtension(Path root, String extension) {
        List<File> result = new ArrayList<>();
        File rf = root.toFile();
        File[] children = rf.listFiles();
        if (children == null) return result;
        for (File child : children) {
            if (child.isDirectory() && !child.isHidden() && !child.getName().startsWith("."))
                result.addAll(getFilesByExtension(child.toPath(), extension));
            else if (child.toString().toLowerCase().endsWith(extension.toLowerCase()))
                result.add(child);
        }
        return result;
    }

    public static String readFile(File file) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            FiguraMod.LOGGER.error("Failed to read File: " + file);
            throw e;
        }
    }

    public static byte[] readFileBytes(File file) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            return stream.readAllBytes();
        } catch (IOException e) {
            FiguraMod.LOGGER.error("Failed to read File: " + file);
            throw e;
        }
    }

    public static void readCacheFile(String name, Consumer<CompoundTag> consumer) {
        try {
            //get file
            Path path = FiguraMod.getCacheDirectory().resolve(name + ".nbt");

            if (!Files.exists(path))
                return;

            //read file
            FileInputStream fis = new FileInputStream(path.toFile());
            CompoundTag nbt = NbtIo.readCompressed(fis);
            consumer.accept(nbt);
            fis.close();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }

    public static void saveCacheFile(String name, Consumer<CompoundTag> consumer) {
        try {
            //get nbt
            CompoundTag nbt = new CompoundTag();
            consumer.accept(nbt);

            //create file
            Path path = FiguraMod.getCacheDirectory().resolve(name + ".nbt");

            if (!Files.exists(path))
                Files.createFile(path);

            //write file
            FileOutputStream fs = new FileOutputStream(path.toFile());
            NbtIo.writeCompressed(nbt, fs);
            fs.close();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
    }

    public static void deleteCacheFile(String name) {
        Path path = FiguraMod.getCacheDirectory().resolve(name + ".nbt");
        deleteFile(path.toFile());
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

    public static void deleteFile(File file) {
        if (!file.exists())
            return;

        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFile(f);
                } else {
                    f.delete();
                }
            }
        }

        file.delete();
    }

    public static void writeFile(Path path, byte[] data) throws IOException {
        if (data == null)
            return;

        try (FileOutputStream fs = new FileOutputStream(path.toFile())) {
            fs.write(data);
        }
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
