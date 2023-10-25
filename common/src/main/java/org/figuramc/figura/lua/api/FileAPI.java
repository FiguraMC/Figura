package org.figuramc.figura.lua.api;

import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.data.FiguraInputStream;
import org.figuramc.figura.lua.api.data.FiguraOutputStream;
import org.figuramc.figura.lua.api.data.providers.FiguraProvider;
import org.figuramc.figura.lua.api.data.readers.FiguraReader;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@LuaWhitelist
@LuaTypeDoc(name = "FileAPI", value = "file")
public class FileAPI {
    public static final String FOLDER_NAME_PATTERN = "^[a-zA-Z_\\-0-9]+$";

    private final Avatar parent;
    private final Path rootFolderPath;

    public FileAPI(Avatar parent) {
        this.parent = parent;
        if (parent.isHost && parent.dataFolder != null && isFolderNameValid(parent.dataFolder)) {
            Path p = FiguraMod.getFiguraDirectory().resolve("data").resolve(parent.dataFolder).toAbsolutePath()
                    .normalize();
            File r = p.toFile();
            if ((r.exists() && !r.isDirectory()) || (!r.exists() && !r.mkdirs())) {
                rootFolderPath = null;
            }
            else rootFolderPath = p;
        }
        else rootFolderPath = null;
    }

    public static boolean isFolderNameValid(String folderName) {
        return folderName.matches(FOLDER_NAME_PATTERN);
    }

    private Path securityCheck(String path) {
        if (!parent.isHost) throw new LuaError("You can't use FileAPI outside of host environment");
        if (rootFolderPath == null) throw new LuaError("Data folder is invalid or not set in avatar metadata");
        Path p = relativizePath(path);
        if (!isPathAllowed(p)) throw new LuaError("Path %s is not allowed in FileAPI".formatted(path));
        return p;
    }

    private Path relativizePath(String path) {
        Path p = Path.of(path);
        if (p.isAbsolute()) return p.normalize();
        return rootFolderPath.resolve(path).toAbsolutePath().normalize();
    }

    @LuaWhitelist
    @LuaMethodDoc("file.is_path_allowed")
    public boolean isPathAllowed(@LuaNotNil String path) {
        if (rootFolderPath == null) return false;
        return isPathAllowed(relativizePath(path));
    }

    public boolean isPathAllowed(Path path) {
        if (rootFolderPath == null) return false;
        return path.toAbsolutePath().startsWith(rootFolderPath);
    }

    @LuaWhitelist
    @LuaMethodDoc("file.allowed")
    public boolean allowed() {
        return parent.isHost && rootFolderPath != null;
    }

    @LuaWhitelist
    @LuaMethodDoc("file.exists")
    public boolean exists(@LuaNotNil String path) {
        Path p = securityCheck(path);
        File f = p.toFile();
        return f.exists();
    }

    @LuaWhitelist
    @LuaMethodDoc("file.is_file")
    public boolean isFile(@LuaNotNil String path) {
        Path p = securityCheck(path);
        File f = p.toFile();
        return f.exists() && f.isFile();
    }

    @LuaWhitelist
    @LuaMethodDoc("file.is_directory")
    public boolean isDirectory(@LuaNotNil String path) {
        Path p = securityCheck(path);
        File f = p.toFile();
        return f.exists() && f.isDirectory();
    }

    @LuaWhitelist
    @LuaMethodDoc("file.open_read_stream")
    public FiguraInputStream openReadStream(@LuaNotNil String path) {
        try {
            Path p = securityCheck(path);
            File f = p.toFile();
            FileInputStream fis = new FileInputStream(f);
            return new FiguraInputStream(fis);
        } catch (FileNotFoundException e) {
            throw new LuaError(e);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("file.open_write_stream")
    public FiguraOutputStream openWriteStream(@LuaNotNil String path) {
        try {
            Path p = securityCheck(path);
            File f = p.toFile();
            FileOutputStream fos = new FileOutputStream(f);
            return new FiguraOutputStream(fos);
        } catch (FileNotFoundException e) {
            throw new LuaError(e);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("file.read")
    public <T> T read(@LuaNotNil String path, @LuaNotNil FiguraReader<T> reader) {
        try (FiguraInputStream fis = openReadStream(path)) {
            return reader.readFrom(fis);
        } catch (IOException e) {
            throw new LuaError(e);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("file.write")
    public <T> void write(@LuaNotNil String path, @LuaNotNil FiguraProvider<T> provider, @LuaNotNil T data) {
        try (FiguraOutputStream fos = openWriteStream(path)) {
            FiguraInputStream fis = provider.getStream(data);
            fis.transferTo(fos);
            fis.close();
        } catch (IOException e) {
            throw new LuaError(e);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("file.mkdir")
    public boolean mkdir(@LuaNotNil String path) {
        Path p = securityCheck(path);
        File f = p.toFile();
        return f.mkdir();
    }

    @LuaWhitelist
    @LuaMethodDoc("file.mkdirs")
    public boolean mkdirs(@LuaNotNil String path) {
        Path p = securityCheck(path);
        File f = p.toFile();
        return f.mkdirs();
    }

    @LuaWhitelist
    @LuaMethodDoc("file.delete")
    public boolean delete(@LuaNotNil String path) {
        Path p = securityCheck(path);
        File f = p.toFile();
        return f.delete();
    }

    @LuaWhitelist
    @LuaMethodDoc("file.list")
    public ArrayList<String> list(@LuaNotNil String path) {
        Path p = securityCheck(path);
        File f = p.toFile();
        ArrayList<String> s = new ArrayList<>();
        Arrays.stream(f.list()).forEach(str -> s.add(str));
        return s;
    }

    @Override
    public String toString() {
        return "FileAPI";
    }
}
