package org.figuramc.figura.backend2;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.UserData;
import org.figuramc.figura.avatar.local.CacheAvatarLoader;
import org.figuramc.figura.parsers.Buwwet.BlockBenchPart;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.UUID;


public class BuwwetNetworkStuff extends NetworkStuff {

    @Nullable
    public static String getUUIDfromUsername(String username) throws IOException {
        // Request the minecraft API to get somebody's id
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject)jsonParser.parse(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"));

            // Return the UUID if found.
            String uuid_raw = jsonObject.get("id").getAsString();
            String uuid = new StringBuilder(uuid_raw)
                    .insert(20, "-")
                    .insert(16, "-")
                    .insert(12, "-")
                    .insert(8, "-").toString();
            return uuid;
        } else {
            return null;
        }
    }

    @Nullable
    public static String downloadUser(String username) throws Exception {
        String uuid = getUUIDfromUsername(username);
        if (uuid == null) {
            return null;
        }
        // Get the avatars of our user.
        HttpRequest userRequest = NetworkStuff.api.getUser(UUID.fromString(uuid));
        HttpResponse<InputStream> userResponse = client.send(userRequest, HttpResponse.BodyHandlers.ofInputStream());

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject)jsonParser.parse(
                new InputStreamReader(userResponse.body(), "UTF-8"));

        // Avatar list
        ArrayList<Pair<String, Pair<String, UUID>>> avatars = new ArrayList<>();

        JsonArray equippedAvatars = jsonObject.getAsJsonArray("equipped");
        for (JsonElement element : equippedAvatars) {
            // Get all of the data for each avatar.
            JsonObject entry = element.getAsJsonObject();
            UUID owner = UUID.fromString(entry.get("owner").getAsString());
            avatars.add(Pair.of(entry.get("hash").getAsString(), Pair.of(entry.get("id").getAsString(), owner)));
        }

        downloadAvatars(avatars);

        FiguraMod.LOGGER.info("Downloaded " + username);
        return "Success";
    }

    public static void downloadAvatars(ArrayList<Pair<String, Pair<String, UUID>>> avatars) throws IOException, InterruptedException {
        for (Pair<String, Pair<String, UUID>> avatar : avatars) {
            // Send a request to the API for each avatar saved by a user.
            // (id of avatar, owner of avatar)
            Pair<String, UUID> pair = avatar.getSecond();

            HttpRequest request = NetworkStuff.api.getAvatar(pair.getSecond(), pair.getFirst());
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            // Transform it to NBT.
            InputStream inputStream = response.body();
            String s;
            try {
                s = response.statusCode() == 200 ? "<avatar data>" : new String(inputStream.readAllBytes());
            } catch (Exception e) {
                s = e.getMessage();
            }

            if (response.statusCode() != 200) {
                return;
            }

            CompoundTag nbt = NbtIo.readCompressed(inputStream);
            FiguraMod.LOGGER.info(String.valueOf(nbt));

            // Get model.

            BlockBenchPart.parseNBTchildren(nbt.getCompound("models"));

            CacheAvatarLoader.save(avatar.getFirst(), nbt);
        }



    }

    public static void copy_avatar(String hash) {
        CacheAvatarLoader.load(hash, new UserData(Minecraft.getInstance().getUser().getProfileId()));
    }
}
