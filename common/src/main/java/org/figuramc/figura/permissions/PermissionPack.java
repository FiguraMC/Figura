package org.figuramc.figura.permissions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class PermissionPack {

    // fields :p
    public String name; // uuid
    private boolean visible = true; // used on UI

    // permission -> value map
    private final Map<Permissions, Integer> permissions = new HashMap<>();
    private final Map<String, Map<Permissions, Integer>> customPermissions = new HashMap<>();

    // constructors // 

    public PermissionPack(String name) {
        this.name = name;
    }

    // functions // 

    public abstract MutableComponent getCategoryName();
    public abstract int getColor();
    public abstract Permissions.Category getCategory();
    public abstract void setCategory(CategoryPermissionPack newParent);

    // read nbt
    public void loadNbt(CompoundTag nbt) {
        // default permissions
        CompoundTag perms = nbt.getCompound("permissions");
        for (Permissions setting : Permissions.DEFAULT) {
            if (perms.contains(setting.name))
                permissions.put(setting, perms.getInt(setting.name));
        }

        // custom permissions
        CompoundTag custom = nbt.getCompound("custom");
        for (Map.Entry<String, Collection<Permissions>> entry : PermissionManager.CUSTOM_PERMISSIONS.entrySet()) {
            String key = entry.getKey();

            Map<Permissions, Integer> map = new HashMap<>();
            CompoundTag customNbt = custom.getCompound(key);

            for (Permissions setting : entry.getValue()) {
                if (customNbt.contains(setting.name))
                    map.put(setting, customNbt.getInt(setting.name));
            }

            customPermissions.put(key, map);
        }
    }

    // write nbt
    public void writeNbt(CompoundTag nbt) {
        // name
        nbt.putString("name", this.name);

        // default permissions
        CompoundTag perms = new CompoundTag();
        for (Map.Entry<Permissions, Integer> entry : this.permissions.entrySet())
            perms.putInt(entry.getKey().name, entry.getValue());

        nbt.put("permissions", perms);

        // custom permissions
        CompoundTag custom = new CompoundTag();
        for (Map.Entry<String, Map<Permissions, Integer>> entry : this.customPermissions.entrySet()) {
            CompoundTag customNbt = new CompoundTag();

            for (Map.Entry<Permissions, Integer> entry2 : entry.getValue().entrySet())
                customNbt.putInt(entry2.getKey().name, entry2.getValue());

            custom.put(entry.getKey(), customNbt);
        }

        nbt.put("custom", custom);
    }

    // get value from permission
    public int get(Permissions permissions) {
        // get setting
        Integer setting = this.permissions.get(permissions);
        if (setting != null)
            return setting;

        for (Map<Permissions, Integer> value : this.customPermissions.values()) {
            setting = value.get(permissions);
            if (setting != null)
                return setting;
        }

        // if no permission is found, return -1
        return -1;
    }

    public void insert(Permissions permissions, Integer value, String id) {
        if (Permissions.DEFAULT.contains(permissions)) {
            this.permissions.put(permissions, value);
            return;
        }

        Map<Permissions, Integer> map = customPermissions.getOrDefault(id, new HashMap<>());
        map.put(permissions, value);
        customPermissions.put(id, map);
    }

    public boolean hasChanges() {
        boolean bool = !permissions.isEmpty();

        if (!bool) {
            for (Map<Permissions, Integer> value : customPermissions.values()) {
                if (!value.isEmpty())
                    return true;
            }
        }

        return bool;
    }

    public boolean isChanged(Permissions permissions) {
        if (this.permissions.containsKey(permissions))
            return true;

        for (Map<Permissions, Integer> map : customPermissions.values()) {
            if (map.containsKey(permissions))
                return true;
        }

        return false;
    }

    public void reset(Permissions permissions) {
        this.permissions.remove(permissions);
        for (Map<Permissions, Integer> map : customPermissions.values())
            map.remove(permissions);
    }

    // clear permissions
    public void clear() {
        permissions.clear();
        customPermissions.clear();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Map<Permissions, Integer> getPermissions() {
        return permissions;
    }

    public Map<String, Map<Permissions, Integer>> getCustomPermissions() {
        return customPermissions;
    }

    // -- types -- // 

    public static class CategoryPermissionPack extends PermissionPack {

        public final Permissions.Category category;

        public CategoryPermissionPack(Permissions.Category category) {
            super(category.name());
            this.category = category;
        }

        @Override
        public MutableComponent getCategoryName() {
            return category.text.copy();
        }

        @Override
        public int getColor() {
            return category.color;
        }

        @Override
        public Permissions.Category getCategory() {
            return category;
        }

        @Override
        public void setCategory(CategoryPermissionPack newParent) {
            // do nothing
        }

        @Override
        public int get(Permissions permissions) {
            int result = super.get(permissions);
            return result != -1 ? result : permissions.getDefault(getCategory());
        }
    }

    public static class PlayerPermissionPack extends PermissionPack {

        public CategoryPermissionPack category;

        public PlayerPermissionPack(CategoryPermissionPack category, String name) {
            super(name);
            this.category = category;
        }

        @Override
        public MutableComponent getCategoryName() {
            return category.getCategoryName();
        }

        @Override
        public int getColor() {
            return category.getColor();
        }

        @Override
        public Permissions.Category getCategory() {
            return category.getCategory();
        }

        @Override
        public void setCategory(CategoryPermissionPack newParent) {
            this.category = newParent;
        }

        @Override
        public void writeNbt(CompoundTag nbt) {
            if (this.getCategory() != Permissions.Category.BLOCKED) {
                super.writeNbt(nbt);
            } else {
                nbt.putString("name", this.name);
            }

            // category
            nbt.putString("category", category.name);
        }

        @Override
        public int get(Permissions permissions) {
            int result = super.get(permissions);
            return result != -1 ? result : category.get(permissions);
        }

        @Override
        public boolean isVisible() {
            return category.isVisible();
        }
    }
}
