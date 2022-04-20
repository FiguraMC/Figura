package org.moon.figura.model.rendering;

import net.minecraft.nbt.NbtCompound;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.model.FiguraModelPart;

public class ImmediateAvatarRenderer extends AvatarRenderer {

    private FiguraBuffer buffer;
    private FiguraModelPart root;

    public ImmediateAvatarRenderer(Avatar avatar, NbtCompound avatarCompound) {
        super(avatar, avatarCompound);

        FiguraBuffer.Builder builder = FiguraBuffer.builder();

    }




    @Override
    public void render() {

    }
}
