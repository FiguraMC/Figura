package org.figuramc.figura.permissions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.entries.FiguraPermissions;
import org.figuramc.figura.utils.IOUtils;

import java.util.*;

public class PermissionManager {

    // container maps
    public static final Map<Permissions.Category, PermissionPack.CategoryPermissionPack> CATEGORIES = new LinkedHashMap<>();
    private static final Map<UUID, PermissionPack.PlayerPermissionPack> PLAYERS = new HashMap<>();
    private static final Set<UUID> BACKEND_CHECKED = new HashSet<>();

    // custom permissions
    public static final Map<String, Collection<Permissions>> CUSTOM_PERMISSIONS = new HashMap<>();

    // main method for loading the permissions
    public static void init() {
        // load groups
        for (Permissions.Category category : Permissions.Category.values()) {
            PermissionPack.CategoryPermissionPack container = new PermissionPack.CategoryPermissionPack(category);
            CATEGORIES.put(category, container);
        }

        // then load nbt
        IOUtils.readCacheFile("permissions", PermissionManager::readNbt);
    }

    public static void reinit() {
        BACKEND_CHECKED.clear();
        CATEGORIES.clear();
        PLAYERS.clear();
        init();
    }

    public static void initEntryPoints(Set<FiguraPermissions> set) {
        // custom permission
        for (FiguraPermissions figuraPermissions : set)
            CUSTOM_PERMISSIONS.put(figuraPermissions.getTitle(), figuraPermissions.getPermissions());
    }

    // read permissions from nbt, adding them into the hash maps
    private static void readNbt(CompoundTag nbt) {
        // get nbt lists
        ListTag groupList = nbt.getList("groups", Tag.TAG_COMPOUND);
        ListTag playerList = nbt.getList("players", Tag.TAG_COMPOUND);

        // groups
        for (Tag nbtElement : groupList) {
            CompoundTag compound = (CompoundTag) nbtElement;

            // parse permissions
            String name = compound.getString("name");

            try {
                Permissions.Category category = Permissions.Category.valueOf(name);
                PermissionPack pack = CATEGORIES.get(category);
                pack.loadNbt(compound);
            } catch (Exception ignored) {
                FiguraMod.LOGGER.warn("Failed to load permissions for \"{}\"", name);
            }
        }

        // players
        for (Tag value : playerList) {
            CompoundTag compound = (CompoundTag) value;

            // parse permissions
            String name = compound.getString("name");

            try {
                UUID uuid = UUID.fromString(name);
                String parent = compound.getString("category");
                Permissions.Category category = Permissions.Category.valueOf(parent);

                PermissionPack.CategoryPermissionPack parentPack = CATEGORIES.get(category);
                PermissionPack.PlayerPermissionPack pack = new PermissionPack.PlayerPermissionPack(parentPack, name);
                pack.loadNbt(compound);

                PLAYERS.put(uuid, pack);
            } catch (Exception ignored) {
                FiguraMod.LOGGER.warn("Failed to load permissions for \"{}\"", name);
            }
        }
    }

    // saves a copy of permissions to disk
    public static void saveToDisk() {
        IOUtils.saveCacheFile("permissions", nbt -> {
            // create dummy lists for later
            ListTag groupList = new ListTag();
            ListTag playerList = new ListTag();

            // get groups nbt
            for (PermissionPack group : CATEGORIES.values()) {
                if (!group.hasChanges())
                    continue;

                CompoundTag container = new CompoundTag();
                group.writeNbt(container);
                groupList.add(container);
            }

            // get players nbt
            for (PermissionPack.PlayerPermissionPack pack : PLAYERS.values()) {
                Permissions.Category category = getDefaultCategory();
                if (category == null) category = Permissions.Category.DEFAULT;
                if (!pack.hasChanges() && pack.getCategory() == category)
                    continue;

                CompoundTag container = new CompoundTag();
                pack.writeNbt(container);
                playerList.add(container);
            }

            // add lists to nbt
            nbt.put("groups", groupList);
            nbt.put("players", playerList);

            FiguraMod.debug("Saved Permissions");
        });
    }

    // get or crate player permissions
    public static PermissionPack.PlayerPermissionPack get(UUID id) {
        if (PLAYERS.containsKey(id))
            return PLAYERS.get(id);

        Permissions.Category category = getDefaultCategory();
        if (FiguraMod.isLocal(id)) {
            category = Permissions.Category.MAX;
        } else if (category == null) {
            category = Permissions.Category.DEFAULT;
        }

        PermissionPack.PlayerPermissionPack pack = new PermissionPack.PlayerPermissionPack(CATEGORIES.get(category), id.toString());
        PLAYERS.put(id, pack);

        FiguraMod.debug("Created Permissions for: " + id);
        return pack;
    }

    public static PermissionPack.PlayerPermissionPack getMobPermissions(UUID id) {
        PermissionPack.PlayerPermissionPack pack = new PermissionPack.PlayerPermissionPack(CATEGORIES.get(Permissions.Category.MAX), id.toString());
        pack.insert(Permissions.OFFSCREEN_RENDERING, 0, FiguraMod.MOD_ID);
        return pack;
    }

    // increase a container category
    public static boolean increaseCategory(PermissionPack container) {
        return changeCategory(container, container.getCategory().index + 1);
    }

    // decrease a container category
    public static boolean decreaseCategory(PermissionPack container) {
        return changeCategory(container, container.getCategory().index - 1);
    }

    private static boolean changeCategory(PermissionPack container, int index) {
        Permissions.Category newCategory = Permissions.Category.indexOf(index);
        if (newCategory == null)
            return false;

        // update permission
        container.setCategory(CATEGORIES.get(newCategory));
        saveToDisk();
        return true;
    }

    public static void setDefaultFor(UUID id, Permissions.Category defaultCat) {
        // default category was already loaded once, do not attempt again
        if (BACKEND_CHECKED.contains(id))
            return;

        boolean canAdd;
        if (!PLAYERS.containsKey(id)) {
            // player do not exist, so pass
            canAdd = true;
        } else {
            // check if the player is still considered default by having no changes on them
            PermissionPack.PlayerPermissionPack pack = PLAYERS.get(id);
            Permissions.Category def = getDefaultCategory();
            if (def == null) def = Permissions.Category.DEFAULT;
            canAdd = !pack.hasChanges() && pack.getCategory() == def;
        }

        // set the new category for the player
        if (canAdd) {
            PermissionPack.PlayerPermissionPack pack = new PermissionPack.PlayerPermissionPack(CATEGORIES.get(defaultCat), id.toString());
            PLAYERS.put(id, pack);
            FiguraMod.debug("Set permissions of {} to {} based on backend userdata", id, defaultCat.name());
        }

        // add this player to not be changed again
        BACKEND_CHECKED.add(id);
    }

    public static Permissions.Category getDefaultCategory() {
        return Permissions.Category.indexOf(Configs.DEFAULT_PERMISSION_LEVEL.value);
    }
}
