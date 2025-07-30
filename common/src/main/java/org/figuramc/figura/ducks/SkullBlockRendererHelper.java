package org.figuramc.figura.ducks;

import org.figuramc.figura.avatar.Avatar;

public class SkullBlockRendererHelper {
    private static Avatar avatar;
    public static void setAvatar(Avatar av) {
        avatar = av;
    }

    public static Avatar getAvatar() {
        return avatar;
    }
}
