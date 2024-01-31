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

    @Intrinsic
    @Accessor("titleFadeInTime")
    void setTitleFadeInTime(int time);

    @Intrinsic
    @Accessor("titleStayTime")
    void setTitleStayTime(int time);

    @Intrinsic
    @Accessor("titleFadeOutTime")
    void setTitleFadeOutTime(int time);

    @Intrinsic
    @Accessor("titleTime")
    void setTitleTime(int time);

    @Intrinsic
    @Accessor("titleFadeInTime")
    int getTitleFadeInTime();

    @Intrinsic
    @Accessor("titleStayTime")
    int getTitleStayTime();

    @Intrinsic
    @Accessor("titleFadeOutTime")
    int getTitleFadeOutTime();

    @Intrinsic
    @Accessor("titleTime")
    int getTitleTime();

    @Intrinsic
    @Accessor("title")
    void setTitle(Component title);

    @Intrinsic
    @Accessor("title")
    void setSubtitle(Component subtitle);
}
