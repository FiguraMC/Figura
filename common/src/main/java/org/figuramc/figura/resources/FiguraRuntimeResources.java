package org.figuramc.figura.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.packs.FolderPackResources;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.utils.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FiguraRuntimeResources {

    private static String ASSETS_VERSION;
    public static final FolderPackResources PACK = new FolderPackResources(getRootDirectory().toFile());

    public static Path getRootDirectory() {
        return IOUtils.getOrCreateDir(FiguraMod.getCacheDirectory(), "resources");
    }

    public static Path getAssetsDirectory() {
        return IOUtils.getOrCreateDir(getRootDirectory(), "assets/" + FiguraMod.MOD_ID);
    }

    public static String getAssetsVersion() {
        if (ASSETS_VERSION == null)
             ASSETS_VERSION = FiguraMod.METADATA.getCustomValueAsString("assets_version");
        return ASSETS_VERSION;
    }

    private static CompletableFuture<Void> future;

    public static void clearCache() {
        IOUtils.deleteFile(getRootDirectory());
    }

    static JsonParser parser = new JsonParser();
    public static CompletableFuture<Void> init() {
        return future = CompletableFuture.runAsync(() -> {
            FiguraMod.LOGGER.info("Fetching backend resources...");

            JsonObject hashes, oldHashes;

            // get old hashes
            Path hashesPath = getRootDirectory().resolve("hashes.json");
            try (BufferedReader reader = Files.newBufferedReader(hashesPath)) {
                oldHashes = parser.parse(reader).getAsJsonObject();
            } catch (Exception ignored) {
                oldHashes = new JsonObject();
            }

            // get new hashes
            try (InputStream stream = NetworkStuff.getResourcesHashes(getAssetsVersion())) {
                byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(stream);
                String s = new String(bytes);
                hashes = parser.parse(s).getAsJsonObject();

                // save new hashes
                try (OutputStream fs = Files.newOutputStream(hashesPath)) {
                    fs.write(bytes);
                } catch (Exception e) {
                    FiguraMod.LOGGER.error("Failed to save resource hashes", e);
                }
            } catch (Exception ignored) {
                FiguraMod.LOGGER.warn("Failed to fetch backend resources");
                return;
            }

            // compare hashes
            for (Map.Entry<String, JsonElement> entry : hashes.entrySet()) {
                String key = entry.getKey();
                JsonElement oldHash = oldHashes.get(key);
                try {
                    if (oldHash == null || !oldHash.getAsString().equals(entry.getValue().getAsString())) {
                        getAndSaveResource(key);
                    }
                } catch (Exception e) {
                    FiguraMod.debug("Failed to download resource \"" + key + "\"", e);
                }
            }
            File assetsDirectoryFile = getAssetsDirectory().toFile();
            JsonObject directoryJsonObject = constructAssetsDirectoryJsonObject(assetsDirectoryFile.listFiles());
            for (Map.Entry<String, JsonElement> entry : directoryJsonObject.entrySet()) {
                if (!hashes.has(entry.getKey()) && !Configs.LOCAL_ASSETS.value) {
                    IOUtils.deleteFile(getAssetsDirectory().resolve(entry.getKey()));
                }
            }
        });
    }

    public static JsonObject constructAssetsDirectoryJsonObject(File[] files) {
        JsonObject object = new JsonObject();
        for (File file : files) {
            if (file.isDirectory()) {
                for (Map.Entry<String, JsonElement> entry : constructAssetsDirectoryJsonObject(file.listFiles()).entrySet()) {
                    object.addProperty(entry.getKey(), entry.getValue().getAsString());
                }
            } else if (!file.isHidden()){
                object.addProperty(getAssetsDirectory().toUri().relativize(file.toURI()).getPath(), "0");
            }
        }
        return object;
    }
    private static void getAndSaveResource(String path) throws Exception {
        if (Configs.LOCAL_ASSETS.value) return;
        Path target = getAssetsDirectory().resolve(path);
        IOUtils.createDirIfNeeded(target.getParent());
        try (InputStream resource = NetworkStuff.getResource(getAssetsVersion(), path); OutputStream fs = Files.newOutputStream(target)) {
            fs.write(org.apache.commons.io.IOUtils.toByteArray(resource));
            FiguraMod.debug("Downloaded resource \"" + path + "\"");
        }
    }

    public static void joinFuture() {
        if (future != null && !future.isDone())
            future.join();
    }
}
