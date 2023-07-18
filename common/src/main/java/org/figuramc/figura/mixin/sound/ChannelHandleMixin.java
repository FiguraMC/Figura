package org.figuramc.figura.mixin.sound;

import net.minecraft.client.sounds.ChannelAccess;
import org.figuramc.figura.ducks.ChannelHandleAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(ChannelAccess.ChannelHandle.class)
public class ChannelHandleMixin implements ChannelHandleAccessor {

    @Unique private UUID owner;
    @Unique private String name;

    @Override
    public UUID getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
