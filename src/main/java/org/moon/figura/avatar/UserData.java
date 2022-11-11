package org.moon.figura.avatar;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.local.CacheAvatarLoader;
import org.moon.figura.backend2.NetworkStuff;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

public class UserData {

    public final UUID id;
    private final List<Avatar> avatars = new ArrayList<>();
    private Pair<BitSet, BitSet> badges;

    public UserData(UUID id) {
        this.id = id;
    }

    public void loadData(ArrayList<Pair<String, Pair<String, UUID>>> avatars, Pair<BitSet,BitSet> badges) {
        loadBadges(badges);
        clear();
        for (Pair<String, Pair<String, UUID>> avatar : avatars) {
            if (!CacheAvatarLoader.checkAndLoad(avatar.getFirst(), this)) {
                Pair<String, UUID> pair = avatar.getSecond();
                NetworkStuff.getAvatar(this, pair.getSecond(), pair.getFirst(), avatar.getFirst());
            }
        }
    }

    public void loadAvatar(CompoundTag nbt) {
        Avatar avatar = new Avatar(id);
        this.avatars.add(avatar);
        avatar.load(nbt);
        FiguraMod.debug("Loaded avatar for " + id);
    }

    public void loadBadges(Pair<BitSet, BitSet> pair) {
        this.badges = pair;
    }

    public Pair<BitSet, BitSet> getBadges() {
        return badges;
    }

    public List<Avatar> getAvatars() {
        return avatars;
    }

    public Avatar getMainAvatar() {
        return avatars.size() > 0 ? avatars.get(0) : null;
    }

    public void clear() {
        for (Avatar avatar : avatars)
            avatar.clean();
        avatars.clear();
    }
}
