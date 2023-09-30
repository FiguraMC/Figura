package org.figuramc.figura.backend2;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.UserData;
import org.figuramc.figura.avatar.local.CacheAvatarLoader;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.parsers.Buwwet.BlockBenchPart;
import org.figuramc.figura.parsers.Buwwet.FiguraModelParser;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.ast.Str;

import java.io.*;

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

        try {
            String avatarName = downloadAvatars(avatars);
            if (avatarName != null) {
                FiguraMod.LOGGER.info("Downloaded " + username + "'s avatar: " + avatarName);
                FiguraToast.sendToast(Component.literal("Completed downloading the avatar of " + username + ": " + avatarName));
            } else {
                FiguraMod.LOGGER.warn(username + " has no equipped avatars!");
                FiguraToast.sendToast(Component.literal("Failed to download any avatars as " + username + " has no equipped avatars."), FiguraToast.ToastType.ERROR);
            }

            return avatarName;

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString(); // stack trace as a string
            FiguraMod.LOGGER.error(sStackTrace);
        }

        return "Success";
    }

    public static String downloadAvatars(ArrayList<Pair<String, Pair<String, UUID>>> avatars) throws IOException, InterruptedException {
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
                return "NONE";
            }

            CompoundTag nbt = NbtIo.readCompressed(inputStream);
            FiguraMod.LOGGER.info(String.valueOf(nbt));

            // Return avatar.
            return FiguraModelParser.parseAvatar(nbt);
            //BlockBenchPart.parseNBTchildren(nbt.getCompound("models"));
        }

        return null;
    }

    public static void copy_avatar(String hash) {
        CacheAvatarLoader.load(hash, new UserData(Minecraft.getInstance().getUser().getProfileId()));
    }
}
