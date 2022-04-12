package org.moon.figura.avatars;

import java.util.ArrayList;

//class that contains all avatar data
//contains all avatars, all customizations, owner, trust
public class AvatarData {

    private final ArrayList<Avatar> avatars = new ArrayList<>();

    public Avatar getAvatar(int index) {
        return index >= 0 && avatars.size() > index ? avatars.get(index) : null;
    }

    public String getName() {
        Avatar avatar = getAvatar(0);
        return avatar != null ? avatar.getName() : "";
    }

    public String getAuthor() {
        Avatar avatar = getAvatar(0);
        return avatar != null ? avatar.getAuthor() : "";
    }

    public String getVersion() {
        Avatar avatar = getAvatar(0);
        return avatar != null ? avatar.getVersion() : "";
    }
}
