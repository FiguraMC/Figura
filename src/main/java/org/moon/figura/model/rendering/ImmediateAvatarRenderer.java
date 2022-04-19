package org.moon.figura.model.rendering;

import net.minecraft.nbt.NbtCompound;
import org.moon.figura.avatars.Avatar;

public class ImmediateAvatarRenderer extends AvatarRenderer {

    private FiguraBuffer buffer;

    public ImmediateAvatarRenderer(Avatar avatar, NbtCompound avatarCompound) {
        super(avatar, avatarCompound);
    }

    @Override
    public void render() {

    }
}
