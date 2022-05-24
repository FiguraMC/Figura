package org.moon.figura.utils;

import org.moon.figura.FiguraMod;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IOUtils {

    public static List<File> getFilesByExtension(Path root, String extension, boolean recurse) {
        List<File> result = new ArrayList<>();
        File rf = root.toFile();
        File[] children = rf.listFiles();
        if (children == null) return result;
        for (File child : children) {
            if (recurse && child.isDirectory() && !child.isHidden() && !child.getName().startsWith("."))
                result.addAll(getFilesByExtension(child.toPath(), extension, true));
            else if (child.toString().toLowerCase().endsWith(extension.toLowerCase()))
                result.add(child);
        }
        return result;
    }

    public static String readFile(File file) throws IOException {
        try {
            FileInputStream stream = new FileInputStream(file);
            String fileContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            stream.close();
            return fileContent;
        } catch (IOException e) {
            FiguraMod.LOGGER.error("Failed to read File: " + file);
            throw e;
        }
    }

}
