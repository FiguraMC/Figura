package org.moon.figura.avatar;

import com.mojang.datafixers.util.Pair;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class UserData {

    private static final HashMap<UUID, User> USER_MAP = new HashMap<>();

    public static void loadUser(UUID id, List<String> avatars, BitSet prideBadges, BitSet specialBadges) {
        USER_MAP.put(id, new User(avatars, Pair.of(prideBadges, specialBadges)));
    }

    public static List<String> getAvatars(UUID id) {
        User user = USER_MAP.get(id);
        return user == null ? null : user.avatars();
    }

    public static Pair<BitSet, BitSet> getBadges(UUID id) {
        User user = USER_MAP.get(id);
        return user == null ? null : user.badges();
    }

    public static void clear(UUID id) {
        USER_MAP.remove(id);
    }

    private record User(List<String> avatars, Pair<BitSet, BitSet> badges) {}
}
