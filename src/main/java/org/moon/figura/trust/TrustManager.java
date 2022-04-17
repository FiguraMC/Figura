package org.moon.figura.trust;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import org.moon.figura.FiguraMod;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class TrustManager {

    //trust maps
    private static final Map<Identifier, TrustContainer> DEFAULT_GROUPS = new HashMap<>();
    public static final Map<Identifier, TrustContainer> GROUPS = new LinkedHashMap<>();
    public static final Map<Identifier, TrustContainer> PLAYERS = new HashMap<>();

    //main method for loading trust
    public static void init() {
        //load from presets file first then load from disk
        loadDefaultGroups();
        loadFromDisk();
    }

    //load default groups from preset file
    public static void loadDefaultGroups() {
        try {
            //load presets file from resources
            Path presets = FabricLoader.getInstance().getModContainer(FiguraMod.MOD_ID).get().getRootPath().resolve("assets/figura/trust/presets.json");
            InputStreamReader fileReader = new InputStreamReader(Files.newInputStream(presets));
            JsonObject rootObject = (JsonObject) JsonParser.parseReader(fileReader);

            //load trust values
            for (Map.Entry<String, JsonElement> entry : rootObject.entrySet()) {
                String name = entry.getKey();

                //add values
                NbtCompound nbt = new NbtCompound();
                for (Map.Entry<String, JsonElement> trust : entry.getValue().getAsJsonObject().entrySet()) {
                    nbt.put(trust.getKey(), NbtInt.of(trust.getValue().getAsInt()));
                }

                //of container
                Identifier parentID = new Identifier("default_group", name);
                TrustContainer defaultGroup = new TrustContainer(name, null, nbt);
                TrustContainer parent = new TrustContainer(name, parentID, new NbtCompound());

                //add container to map
                DEFAULT_GROUPS.put(parentID, defaultGroup);
                GROUPS.put(new Identifier("group", name), parent);
            }

            FiguraMod.LOGGER.debug("Loaded trust presets from assets");
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Could not load presets from assets");
            FiguraMod.LOGGER.error(e);
        }
    }

    //load all saved trust from disk
    public static void loadFromDisk() {
        try {
            //get file
            Path targetPath = FiguraMod.getFiguraDirectory().resolve("trust_settings.nbt");

            if (!Files.exists(targetPath))
                return;

            //read file
            FileInputStream fis = new FileInputStream(targetPath.toFile());
            NbtCompound getTag = NbtIo.readCompressed(fis);
            readNbt(getTag);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //saves a copy of trust to disk
    public static void saveToDisk() {
        try {
            //get nbt
            NbtCompound targetTag = new NbtCompound();
            writeNbt(targetTag);

            //of file
            Path targetPath = FiguraMod.getFiguraDirectory();
            targetPath = targetPath.resolve("trust_settings.nbt");

            if (!Files.exists(targetPath))
                Files.createFile(targetPath);

            //write file
            FileOutputStream fs = new FileOutputStream(targetPath.toFile());
            NbtIo.writeCompressed(targetTag, fs);
            fs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //write trust to nbt
    public static void writeNbt(NbtCompound nbt) {
        //of dummy lists for later
        NbtList groupList = new NbtList();
        NbtList playerList = new NbtList();

        //get groups nbt
        for (Map.Entry<Identifier, TrustContainer> entry : GROUPS.entrySet()) {
            NbtCompound container = new NbtCompound();
            entry.getValue().writeNbt(container);
            groupList.add(container);
        }

        //get players nbt
        for (Map.Entry<Identifier, TrustContainer> entry : PLAYERS.entrySet()) {
            TrustContainer trust = entry.getValue();
            if (!isLocal(trust) && isTrustChanged(trust)) {
                NbtCompound container = new NbtCompound();
                trust.writeNbt(container);
                playerList.add(container);
            }
        }

        //add lists to nbt
        nbt.put("groups", groupList);
        nbt.put("players", playerList);
    }

    //read trust from nbt, adding them into the hash maps
    public static void readNbt(NbtCompound nbt) {
        //get nbt lists
        NbtList groupList = nbt.getList("groups", NbtElement.COMPOUND_TYPE);
        NbtList playerList = nbt.getList("players", NbtElement.COMPOUND_TYPE);

        //groups
        for (NbtElement nbtElement : groupList) {
            NbtCompound compound = (NbtCompound) nbtElement;

            //parse trust
            String name = compound.getString("name");

            Identifier parentID = null;
            if (compound.contains("parent")) {
                parentID = new Identifier(compound.getString("parent"));
            }

            //add to list
            GROUPS.put(new Identifier("group", name), new TrustContainer(name, parentID, compound.getCompound("trust")));
        }

        //players
        for (NbtElement value : playerList) {
            NbtCompound compound = (NbtCompound) value;

            //parse trust
            String name = compound.getString("name");
            Identifier parentID = new Identifier(compound.getString("parent"));
            TrustContainer container = new TrustContainer(name, parentID, compound.getCompound("trust"));

            //add to list
            PLAYERS.put(new Identifier("player", name), container);
        }
    }

    //get trust from id
    public static TrustContainer get(Identifier id) {
        if (PLAYERS.containsKey(id))
            return PLAYERS.get(id);

        if (GROUPS.containsKey(id))
            return GROUPS.get(id);

        if (DEFAULT_GROUPS.containsKey(id))
            return DEFAULT_GROUPS.get(id);

        FiguraMod.LOGGER.debug("Created trust for: " + id.toString());
        return create(id);
    }

    //get player trust
    public static TrustContainer get(UUID uuid) {
        return get(new Identifier("player", uuid.toString()));
    }

    //of player trust
    private static TrustContainer create(Identifier id) {
        //of trust
        boolean isLocal = isLocal(id.getPath());
        Identifier parentID = new Identifier("group", isLocal ? "local" : "untrusted");
        TrustContainer trust =  new TrustContainer(id.getPath(), parentID, new HashMap<>());

        //add and return
        PLAYERS.put(id, trust);
        return trust;
    }

    //increase a container trust
    public static boolean increaseTrust(TrustContainer tc) {
        Identifier parentID = tc.getParentID();

        //get next group
        int i = 0;
        Identifier nextID = null;
        for (Map.Entry<Identifier, TrustContainer> entry : GROUPS.entrySet()) {
            //if next ID is not null, "return" it
            if (nextID != null) {
                nextID = entry.getKey();
                break;
            }

            //set next ID pointer
            if (entry.getKey().equals(parentID))
                nextID = entry.getKey();

            i++;
        }

        //fail if there is no next ID, or next ID is local but it is not a local player, or if it is already the last group
        if (nextID == null || (nextID.getPath().equals("local") && !isLocal(tc)) || i == GROUPS.size())
            return false;

        //update trust
        tc.setParent(nextID);
        saveToDisk();
        return true;
    }

    //decrease a container trust
    public static boolean decreaseTrust(TrustContainer tc) {
        Identifier parentID = tc.getParentID();

        //get previous group
        int i = 0;
        Identifier prevID = null;
        for (Map.Entry<Identifier, TrustContainer> entry : GROUPS.entrySet()) {
            //if it is already the first group, exit
            if (entry.getKey().equals(parentID))
                break;

            //save previous ID
            prevID = entry.getKey();
            i++;
        }

        //fail if there is no previous ID or if it is already the first group
        if (prevID == null || i == GROUPS.size())
            return false;

        //update trust
        tc.setParent(prevID);
        saveToDisk();
        return true;
    }

    //get local player ID
    private static String getClientPlayerID() {
        return MinecraftClient.getInstance().getSession().getProfile().getId().toString();
    }

    //check if trust is from local player
    public static boolean isLocal(TrustContainer trust) {
        return trust.name.equals(getClientPlayerID()) || trust.name.equals("local");
    }

    //check if id is from local player
    public static boolean isLocal(String id) {
        return id.equals(getClientPlayerID());
    }

    //check if trust has been changed
    private static boolean isTrustChanged(TrustContainer trust) {
         return !trust.getSettings().isEmpty() || !trust.getParentID().getPath().equals("untrusted");
    }
}
