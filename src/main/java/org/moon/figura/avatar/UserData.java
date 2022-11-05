package org.moon.figura.avatar;

import com.mojang.datafixers.util.Pair;
import org.moon.figura.backend2.NetworkStuff;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class UserData {

    private static final HashMap<UUID, User> USER_MAP = new HashMap<>();

    public static void loadUser(UUID id, List<Pair<String, UUID>> avatars, Pair<BitSet, BitSet> badges) {
        USER_MAP.put(id, new User(avatars, badges));
        for (Pair<String, UUID> avatar : avatars)
            NetworkStuff.getAvatar(id, avatar.getSecond(), avatar.getFirst());
    }

    public static List<Pair<String, UUID>> getAvatars(UUID id) {
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

    private record User(List<Pair<String, UUID>> avatars, Pair<BitSet, BitSet> badges) {}
}
