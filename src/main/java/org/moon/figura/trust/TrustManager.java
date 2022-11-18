package org.moon.figura.trust;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;
import org.moon.figura.utils.IOUtils;

import java.util.*;

public class TrustManager {

    //container maps
    public static final Map<Trust.Group, TrustContainer.GroupContainer> GROUPS = new LinkedHashMap<>();
    private static final Map<UUID, TrustContainer.PlayerContainer> PLAYERS = new HashMap<>();

    //custom trusts
    public static final Map<String, Collection<Trust>> CUSTOM_TRUST = new HashMap<>();

    //main method for loading trust
    public static void init() {
        //custom trust
        for (FiguraTrust figuraTrust : IOUtils.loadEntryPoints("figura_trust", FiguraTrust.class))
            CUSTOM_TRUST.put(figuraTrust.getTitle(), figuraTrust.getTrusts());

        //load groups
        for (Trust.Group group : Trust.Group.values()) {
            TrustContainer.GroupContainer container = new TrustContainer.GroupContainer(group);
            GROUPS.put(group, container);
        }

        //then load nbt
        IOUtils.readCacheFile("trust", TrustManager::readNbt);
    }

    //read trust from nbt, adding them into the hash maps
    private static void readNbt(CompoundTag nbt) {
        //get nbt lists
        ListTag groupList = nbt.getList("groups", Tag.TAG_COMPOUND);
        ListTag playerList = nbt.getList("players", Tag.TAG_COMPOUND);

        //groups
        for (Tag nbtElement : groupList) {
            CompoundTag compound = (CompoundTag) nbtElement;

            //parse trust
            String name = compound.getString("name");

            try {
                Trust.Group group = Trust.Group.valueOf(name);
                TrustContainer trust = GROUPS.get(group);
                trust.loadNbt(compound);
            } catch (Exception ignored) {
                FiguraMod.LOGGER.warn("Failed to load trust for \"{}\"", name);
            }
        }

        //players
        for (Tag value : playerList) {
            CompoundTag compound = (CompoundTag) value;

            //parse trust
            String name = compound.getString("name");

            try {
                UUID uuid = UUID.fromString(name);
                if (FiguraMod.isLocal(uuid)) //dont load trust for local player
                    continue;

                String parent = compound.getString("parent");
                Trust.Group group = Trust.Group.valueOf(parent);

                TrustContainer.GroupContainer parentTrust = GROUPS.get(group);
                TrustContainer.PlayerContainer trust = new TrustContainer.PlayerContainer(parentTrust, name);
                trust.loadNbt(compound);

                PLAYERS.put(uuid, trust);
            } catch (Exception ignored) {
                FiguraMod.LOGGER.warn("Failed to load trust for \"{}\"", name);
            }
        }
    }

    //saves a copy of trust to disk
    public static void saveToDisk() {
        IOUtils.saveCacheFile("trust", nbt -> {
            //create dummy lists for later
            ListTag groupList = new ListTag();
            ListTag playerList = new ListTag();

            //get groups nbt
            for (TrustContainer group : GROUPS.values()) {
                if (group.getGroup() == Trust.Group.LOCAL || group.getGroup() == Trust.Group.BLOCKED || !group.hasChanges())
                    continue;

                CompoundTag container = new CompoundTag();
                group.writeNbt(container);
                groupList.add(container);
            }

            //get players nbt
            for (TrustContainer.PlayerContainer trust : PLAYERS.values()) {
                //dont save local
                if (isLocal(trust))
                    continue;

                Trust.Group group = Trust.Group.indexOf(Config.DEFAULT_TRUST.asInt());
                if (group == null) group = Trust.Group.UNTRUSTED;
                if ((!trust.hasChanges() && trust.getGroup() == group))
                    continue;

                CompoundTag container = new CompoundTag();
                trust.writeNbt(container);
                playerList.add(container);
            }

            //add lists to nbt
            nbt.put("groups", groupList);
            nbt.put("players", playerList);

            FiguraMod.debug("Saved Trust Settings");
        });
    }

    //get or crate player trust
    public static TrustContainer.PlayerContainer get(UUID id) {
        if (PLAYERS.containsKey(id))
            return PLAYERS.get(id);

        Trust.Group group = Trust.Group.indexOf(Config.DEFAULT_TRUST.asInt());
        if (FiguraMod.isLocal(id)) {
            group = Trust.Group.LOCAL;
        } else if (group == null) {
            group = Trust.Group.UNTRUSTED;
        }

        TrustContainer.PlayerContainer trust = new TrustContainer.PlayerContainer(GROUPS.get(group), id.toString());
        PLAYERS.put(id, trust);

        FiguraMod.debug("Created trust for: " + id);
        return trust;
    }

    //increase a container trust
    public static boolean increaseTrust(TrustContainer container) {
        Trust.Group group = container.getGroup();

        Trust.Group newGroup = Trust.Group.indexOf(group.index + 1);
        UUID id = UUID.fromString(container.name); //names are uuids

        if (newGroup == null || (!FiguraMod.isLocal(id) && newGroup == Trust.Group.LOCAL))
            return false;

        //update trust
        container.setParent(GROUPS.get(newGroup));
        saveToDisk();
        return true;
    }

    //decrease a container trust
    public static boolean decreaseTrust(TrustContainer container) {
        Trust.Group group = container.getGroup();
        Trust.Group newGroup = Trust.Group.indexOf(group.index - 1);

        if (newGroup == null)
            return false;

        //update trust
        container.setParent(GROUPS.get(newGroup));
        saveToDisk();
        return true;
    }

    public static boolean isLocal(TrustContainer container) {
        return (container instanceof TrustContainer.PlayerContainer p && FiguraMod.isLocal(UUID.fromString(p.name))) || container.getGroup() == Trust.Group.LOCAL;
    }
}
