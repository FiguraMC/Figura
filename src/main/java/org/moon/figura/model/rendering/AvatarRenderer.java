package org.moon.figura.model.rendering;

import net.minecraft.nbt.NbtCompound;
import org.moon.figura.avatars.Avatar;


/**
 * Mainly exists as an abstract superclass for VAO-based and
 * immediate mode avatar renderers.
 */
public abstract class AvatarRenderer {

    protected final Avatar avatar;

    public AvatarRenderer(Avatar avatar, NbtCompound avatarCompound) {
        this.avatar = avatar;
    }

    public abstract void render();
}
