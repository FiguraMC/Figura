package org.moon.figura.lang;

import com.google.gson.*;
import net.minecraft.server.packs.PathPackResources;
import org.moon.figura.FiguraMod;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.utils.IOUtils;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Map;

public class FiguraLangManager {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    public static final PathPackResources LANG_PACK = new PathPackResources(FiguraMod.MOD_NAME + " backend lang pack", getLangRootDirectory(), true);

    public static Path getLangRootDirectory() {
        return IOUtils.getOrCreateDir(FiguraMod.getCacheDirectory(), "lang");
    }

    public static Path getLangDirectory() {
        return IOUtils.getOrCreateDir(getLangRootDirectory(), "assets/figura/lang");
    }

    public static void init() {
        String langData = NetworkStuff.getLangMetadata();
        if (langData == null)
            return;

        //load old hashes
        JsonObject hashes;
        Path hashesPath = getLangRootDirectory().resolve("hashes.json");
        try (FileReader reader = new FileReader(hashesPath.toFile())) {
            hashes = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception ignored) {
            hashes = new JsonObject();
        }

        try {
            JsonObject json = JsonParser.parseString(langData).getAsJsonObject();

            //check and download translations
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                String key = entry.getKey();
                JsonElement oldHash = hashes.get(key);
                if (oldHash == null || !oldHash.getAsString().equals(entry.getValue().getAsString()))
                    downloadLang(entry.getKey());
            }

            //save hashes
            try (FileOutputStream fs = new FileOutputStream(hashesPath.toFile())) {
                fs.write(GSON.toJson(json).getBytes());
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to save translations hash file", e);
            }
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load backend lang files", e);
        }
    }

    private static void downloadLang(String lang) {
        try {
            Path p = getLangDirectory().resolve(lang + ".json");
            String translations = NetworkStuff.getLang(lang);
            JsonElement json = JsonParser.parseString(translations);
            try (FileOutputStream fs = new FileOutputStream(p.toFile())) {
                fs.write(GSON.toJson(json).getBytes());
            }
            FiguraMod.LOGGER.info("Downloaded translations for \"" + lang + "\"");
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to download \"" + lang + "\" translations", e);
        }
    }
}
