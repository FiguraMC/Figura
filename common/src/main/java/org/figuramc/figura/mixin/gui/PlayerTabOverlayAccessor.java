package org.figuramc.figura.mixin.gui;

import com.google.common.collect.Ordering;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(PlayerTabOverlay.class)
public interface PlayerTabOverlayAccessor {

    @Intrinsic
    @Accessor("header")
    Component getHeader();

    @Intrinsic
    @Accessor("footer")
    Component getFooter();

    @Intrinsic
    @Accessor("PLAYER_ORDERING")
    static Ordering<PlayerInfo> getPlayerOrdering() {
        throw new AssertionError();
    }
}
