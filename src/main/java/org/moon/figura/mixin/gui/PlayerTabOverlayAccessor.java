package org.moon.figura.mixin.gui;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Comparator;

@Mixin(PlayerTabOverlay.class)
public interface PlayerTabOverlayAccessor {

    @Intrinsic
    @Accessor("PLAYER_COMPARATOR")
    static Comparator<PlayerInfo> getPlayerComparator() {
        throw new AssertionError();
    }
}
