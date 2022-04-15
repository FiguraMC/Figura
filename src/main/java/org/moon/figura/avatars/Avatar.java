package org.moon.figura.avatars;

import net.minecraft.nbt.NbtCompound;

//the avatar class
//contains all things related to the avatar
//and also related to the owner, like trust settings
public class Avatar {

    private AvatarMetadata metadata;

    public Avatar(NbtCompound nbt) {
        
    }

    public static class AvatarMetadata {
        String name, author, version;
        float size;
    }
}
