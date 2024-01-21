package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface GuiAccessor {
    @Intrinsic
    @Accessor("title")
    Component getTitle();

    @Intrinsic
    @Accessor("subtitle")
    Component getSubtitle();

    @Intrinsic
    @Accessor("overlayMessageString")
    Component getActionbar();

    @Intrinsic
    @Accessor("titleTime")
    int getTime();

    @Intrinsic
    @Accessor("overlayMessageTime")
    int getActionbarTime();
}
