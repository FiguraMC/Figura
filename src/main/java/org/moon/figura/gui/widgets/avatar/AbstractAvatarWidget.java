package org.moon.figura.gui.widgets.avatar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.moon.figura.avatar.local.LocalAvatarFetcher;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.widgets.AbstractContainerElement;
import org.moon.figura.gui.widgets.ContextMenu;
import org.moon.figura.gui.widgets.TexturedButton;
import org.moon.figura.gui.widgets.lists.AvatarList;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

import java.io.File;

public abstract class AbstractAvatarWidget extends AbstractContainerElement implements Comparable<AbstractAvatarWidget> {

    protected final AvatarList parent;
    protected final int depth;
    protected final ContextMenu context;

    protected LocalAvatarFetcher.AvatarPath avatar;
    protected TexturedButton button;
    protected String filter = "";

    public AbstractAvatarWidget(int depth, int width, LocalAvatarFetcher.AvatarPath avatar, AvatarList parent) {
        super(0, 0, width, 20);
        this.parent = parent;
        this.avatar = avatar;
        this.depth = depth;
        this.context = new ContextMenu(this);

        context.addAction(FiguraText.of("gui.context.open_folder"), button -> {
            File f = avatar.getPath().toFile();
            Util.getPlatform().openFile(f.isDirectory() ? f : f.getParentFile());
        });
        context.addAction(FiguraText.of("gui.context.copy_path"), button -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(avatar.getPath().toString());
            FiguraToast.sendToast(FiguraText.of("toast.clipboard"));
        });
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (UIHelper.getContext() == this.context && this.context.isVisible())
            this.button.setHovered(true);

        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY))
            return false;

        if (super.mouseClicked(mouseX, mouseY, button))
            return true;

        //context menu on right click
        if (button == 1) {
            context.setPos((int) mouseX, (int) mouseY);
            context.setVisible(true);
            UIHelper.setContext(context);
            return true;
        }
        //hide old context menu
        else if (UIHelper.getContext() == context) {
            context.setVisible(false);
        }

        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
    }

    public Component getName() {
        return Component.literal(avatar.getName());
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;

        this.button.x = x;
        this.button.y = y;
    }

    public boolean filtered() {
        return this.getName().getString().toLowerCase().contains(filter.toLowerCase());
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible && filtered());
    }

    @Override
    public int compareTo(AbstractAvatarWidget other) {
        //compare types
        if (this instanceof AvatarFolderWidget && other instanceof AvatarWidget)
            return -1;
        else if (this instanceof AvatarWidget && other instanceof AvatarFolderWidget)
            return 1;

        //then compare names
        else return this.getName().getString().toLowerCase().compareTo(other.getName().getString().toLowerCase());
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof AbstractAvatarWidget other && other.avatar != null && this.avatar != null && this.avatar.getPath().equals(other.avatar.getPath());
    }
}
