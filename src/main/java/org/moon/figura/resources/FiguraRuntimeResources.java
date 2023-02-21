package org.moon.figura.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.packs.FolderPackResources;
import org.moon.figura.FiguraMod;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.utils.IOUtils;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FiguraRuntimeResources {

    public static final FolderPackResources PACK = new FolderPackResources(getRootDirectory().toFile());

    public static Path getRootDirectory() {
        return IOUtils.getOrCreateDir(FiguraMod.getCacheDirectory(), "resources");
    }

    public static Path getAssetsDirectory() {
        return IOUtils.getOrCreateDir(getRootDirectory(), "assets/" + FiguraMod.MOD_ID);
    }

    private static CompletableFuture<Void> future;

    public static CompletableFuture<Void> init() {
        return future = CompletableFuture.runAsync(() -> {
            FiguraMod.LOGGER.info("Fetching backend resources...");

            JsonObject hashes, oldHashes;

            //get old hashes
            Path hashesPath = getRootDirectory().resolve("hashes.json");
            try (FileReader reader = new FileReader(hashesPath.toFile())) {
                oldHashes = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (Exception ignored) {
                oldHashes = new JsonObject();
            }

            //get new hashes
            try (InputStream stream = NetworkStuff.getResourcesHashes()) {
                byte[] bytes = stream.readAllBytes();
                String s = new String(bytes);
                hashes = JsonParser.parseString(s).getAsJsonObject();

                //save new hashes
                try (FileOutputStream fs = new FileOutputStream(hashesPath.toFile())) {
                    fs.write(bytes);
                } catch (Exception e) {
                    FiguraMod.LOGGER.error("Failed to save resource hashes", e);
                }
            } catch (Exception ignored) {
                FiguraMod.LOGGER.warn("Failed to fetch backend resources");
                return;
            }

            //compare hashes
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
        });
    }

    private static void getAndSaveResource(String path) throws Exception {
        Path target = getAssetsDirectory().resolve(path);
        IOUtils.createDirIfNeeded(target.getParent());
        try (InputStream resource = NetworkStuff.getResource(path); FileOutputStream fs = new FileOutputStream(target.toFile())) {
            fs.write(resource.readAllBytes());
            FiguraMod.debug("Downloaded resource \"" + path + "\"");
        }
    }

    public static void joinFuture() {
        if (future != null && !future.isDone())
            future.join();
    }
}
