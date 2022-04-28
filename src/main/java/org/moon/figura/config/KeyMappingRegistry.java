package org.moon.figura.config;

import com.google.common.collect.Lists;
import net.minecraft.client.KeyMapping;
import org.moon.figura.mixin.access.KeyMappingAccess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyMappingRegistry {

    private static final Map<String, KeyMapping> keys = new HashMap<>();

    public static void registerKeyMapping(KeyMapping key) {
        //add key
        keys.putIfAbsent(key.getName(), key);

        //add category
        String category = key.getCategory();
        Map<String, Integer> categories = KeyMappingAccess.getCategoryMap();

        if (!categories.containsKey(category)) {
            //get the largest index, so our category will be inserted at the end
            int index = categories.values().stream().max(Integer::compareTo).orElse(0) + 1;
            categories.put(category, index);
        }
    }

    public static KeyMapping[] appendKeys(KeyMapping[] oldKeys) {
        //transform the old array into a list
        //then we remove the duplicate entries from the list
        //and return an array from the list
        List<KeyMapping> newKeys = Lists.newArrayList(oldKeys);
        newKeys.removeAll(keys.values());
        newKeys.addAll(keys.values());
        return newKeys.toArray(new KeyMapping[0]);
    }
}
