package org.figuramc.figura.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.figuramc.figura.FiguraMod;
import org.jetbrains.annotations.NotNull;

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

    /** Converts 26.3 SDL input numbers to the legacy GLFW numbers exposed to Lua. */
    public static int toLegacyInputCode(InputConstants.Key key) {
        int value = key.getValue();
        if (key.getType() == InputConstants.Type.MOUSE) {
            return switch (value) {
                case InputConstants.MOUSE_BUTTON_LEFT -> 0;
                case InputConstants.MOUSE_BUTTON_RIGHT -> 1;
                case InputConstants.MOUSE_BUTTON_MIDDLE -> 2;
                case 4, 5, 6, 7, 8 -> value - 1;
                default -> InputConstants.UNKNOWN.getValue();
            };
        }
        if (key.getType() != InputConstants.Type.KEYBOARD)
            return InputConstants.UNKNOWN.getValue();

        if (value >= InputConstants.KEY_A && value <= InputConstants.KEY_Z)
            return 65 + value - InputConstants.KEY_A;
        if (value >= InputConstants.KEY_1 && value <= InputConstants.KEY_9)
            return 49 + value - InputConstants.KEY_1;
        if (value >= InputConstants.KEY_F1 && value <= InputConstants.KEY_F12)
            return 290 + value - InputConstants.KEY_F1;
        if (value >= InputConstants.KEY_F13 && value <= InputConstants.KEY_F24)
            return 302 + value - InputConstants.KEY_F13;

        return switch (value) {
            case InputConstants.KEY_0 -> 48;
            case 118 -> 348; // SDL_SCANCODE_MENU -> GLFW_KEY_MENU.
            case InputConstants.KEY_NUMPAD0 -> 320;
            case InputConstants.KEY_NUMPAD1 -> 321;
            case InputConstants.KEY_NUMPAD2 -> 322;
            case InputConstants.KEY_NUMPAD3 -> 323;
            case InputConstants.KEY_NUMPAD4 -> 324;
            case InputConstants.KEY_NUMPAD5 -> 325;
            case InputConstants.KEY_NUMPAD6 -> 326;
            case InputConstants.KEY_NUMPAD7 -> 327;
            case InputConstants.KEY_NUMPAD8 -> 328;
            case InputConstants.KEY_NUMPAD9 -> 329;
            case InputConstants.KEY_NUMPADCOMMA -> 330;
            case 84 -> 331; // SDL_SCANCODE_KP_DIVIDE -> GLFW keypad divide.
            case InputConstants.KEY_NUMPADENTER -> 335;
            case InputConstants.KEY_NUMPADEQUALS -> 336;
            case 86 -> 333; // SDL_SCANCODE_KP_MINUS -> GLFW keypad subtract.
            case InputConstants.KEY_ADD -> 334;
            case InputConstants.KEY_MULTIPLY -> 332;
            case InputConstants.KEY_APOSTROPHE -> 39;
            case InputConstants.KEY_BACKSLASH -> 92;
            case InputConstants.KEY_COMMA -> 44;
            case InputConstants.KEY_EQUALS -> 61;
            case InputConstants.KEY_GRAVE -> 96;
            case InputConstants.KEY_LBRACKET -> 91;
            case InputConstants.KEY_MINUS -> 45;
            case InputConstants.KEY_PERIOD -> 46;
            case InputConstants.KEY_RBRACKET -> 93;
            case InputConstants.KEY_SEMICOLON -> 59;
            case InputConstants.KEY_SLASH -> 47;
            case InputConstants.KEY_SPACE -> 32;
            case InputConstants.KEY_TAB -> 258;
            case InputConstants.KEY_LALT -> 342;
            case InputConstants.KEY_LCONTROL -> 341;
            case InputConstants.KEY_LSHIFT -> 340;
            case InputConstants.KEY_LGUI -> 343;
            case InputConstants.KEY_RALT -> 346;
            case InputConstants.KEY_RCONTROL -> 345;
            case InputConstants.KEY_RSHIFT -> 344;
            case InputConstants.KEY_RGUI -> 347;
            case InputConstants.KEY_RETURN -> 257;
            case InputConstants.KEY_ESCAPE -> 256;
            case InputConstants.KEY_BACKSPACE -> 259;
            case InputConstants.KEY_DELETE -> 261;
            case InputConstants.KEY_END -> 269;
            case InputConstants.KEY_HOME -> 268;
            case InputConstants.KEY_INSERT -> 260;
            case InputConstants.KEY_PAGEDOWN -> 267;
            case InputConstants.KEY_PAGEUP -> 266;
            case InputConstants.KEY_CAPSLOCK -> 280;
            case InputConstants.KEY_PAUSE -> 284;
            case InputConstants.KEY_SCROLLLOCK -> 281;
            case InputConstants.KEY_PRINTSCREEN -> 283;
            case InputConstants.KEY_NUMLOCK -> 282;
            case InputConstants.KEY_DOWN -> 264;
            case InputConstants.KEY_LEFT -> 263;
            case InputConstants.KEY_RIGHT -> 262;
            case InputConstants.KEY_UP -> 265;
            default -> InputConstants.UNKNOWN.getValue();
        };
    }

    /** Converts SDL modifier bits to the GLFW-compatible mask historically sent to Lua. */
    public static int toLegacyModifiers(int modifiers) {
        if (modifiers == -1)
            return -1;

        int legacy = 0;
        if ((modifiers & 3) != 0) legacy |= 1;       // shift
        if ((modifiers & 192) != 0) legacy |= 2;     // control
        if ((modifiers & 768) != 0) legacy |= 4;     // alt
        if ((modifiers & 3072) != 0) legacy |= 8;    // super / GUI
        if ((modifiers & 8192) != 0) legacy |= 16;   // caps lock
        if ((modifiers & 4096) != 0) legacy |= 32;   // num lock
        return legacy;
    }

    public static boolean hasControlDown() {
        return Util.getPlatform() == Util.OS.OSX
                ? InputConstants.isKeyDown(InputConstants.KEY_LGUI)
                || InputConstants.isKeyDown(InputConstants.KEY_RGUI)
                : InputConstants.isKeyDown(InputConstants.KEY_LCONTROL)
                || InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);
    }

    public static boolean hasShiftDown() {
        return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
    }

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

    /**
     * Checks, if given file is a hidden avatar resource.
     * Avatar resource is hidden, if it is contained within
     * a hidden folder. Folder is considered "hidden" if either
     * it is marked as "hidden" by the OS or folder's name starts
     * with a `{@code .}` (dot), or if it is contained within
     * another hidden folder.
     *
     * @param path Path of the file to check
     * @return {@code true} if given file is hidden
     */
    public static boolean isHiddenAvatarResource(@NotNull Path path) {
        final Path avatarsDirectory = FiguraMod.getFiguraDirectory().resolve("avatars");
        if (!path.startsWith(avatarsDirectory))
            return true;

        try {
            // Iterate through all parent folders of the avatars
            // resource to find a hidden one (if any)
            for (Path parent = path;
                 !Files.isSameFile(parent, avatarsDirectory);
                 parent = parent.resolve("..").normalize()) {
                if (Files.isHidden(parent) || parent.getFileName().toString().startsWith(".")) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            FiguraMod.LOGGER.error("Failed to get if \"" + path + "\" is hidden", e);
            return false;
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
