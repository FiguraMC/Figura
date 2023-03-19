package org.moon.figura.gui.widgets.permissions;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.widgets.lists.PlayerList;
import org.moon.figura.permissions.PermissionPack;
import org.moon.figura.permissions.Permissions;
import org.moon.figura.utils.ui.UIHelper;

public class AbstractPermPackElement extends AbstractButton implements Comparable<AbstractPermPackElement> {

    protected final PlayerList parent;
    protected final PermissionPack pack;

    protected float scale = 1f;

    protected AbstractPermPackElement(int width, int height, PermissionPack pack, PlayerList parent) {
        super(0, 0, width, height, Component.empty());
        this.parent = parent;
        this.pack = pack;
    }

    protected void animate(float delta, boolean anim) {
        if (anim) {
            scale = (float) Mth.lerp(1 - Math.pow(0.2, delta), scale, 1.2f);
        } else {
            scale = (float) Mth.lerp(1 - Math.pow(0.3, delta), scale, 1f);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        int dw = (int) ((width * scale - width) / 2f);
        int dh = (int) ((height * scale - height) / 2f);
        return parent.isInsideScissors(mouseX, mouseY) && active && visible && UIHelper.isMouseOver(getX() - dw, getY() - dh, width + dw, height + dh, mouseX, mouseY);
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }

    @Override
    public void onPress() {
        //set selected entry
        parent.selectedEntry = this;

        //update permissions widgets
        parent.parent.updatePermissions(this.pack);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }

    public boolean isVisible() {
        return pack.isVisible();
    }

    public PermissionPack getPack() {
        return pack;
    }

    @Override
    public int compareTo(AbstractPermPackElement other) {
        //compare permission categories first
        int len = Permissions.Category.values().length;

        int i;
        if (this instanceof PlayerPermPackElement p && p.dragged) {
            i = Math.min(p.index, len - 1);
        } else {
            i = this.pack.getCategory().index;
        }

        int j;
        if (other instanceof PlayerPermPackElement p && p.dragged) {
            j = Math.min(p.index, len - 1);
        } else {
            j = other.pack.getCategory().index;
        }

        int comp = Integer.compare(i, j);
        if (comp == 0) {
            //then compare types
            if (this instanceof CategoryPermPackElement && other instanceof PlayerPermPackElement)
                return -1;
            if (this instanceof PlayerPermPackElement && other instanceof CategoryPermPackElement)
                return 1;

            if (this instanceof PlayerPermPackElement player1 && other instanceof PlayerPermPackElement player2) {
                Avatar avatar1 = AvatarManager.getAvatarForPlayer(player1.getOwner());
                Avatar avatar2 = AvatarManager.getAvatarForPlayer(player2.getOwner());

                //compare avatar
                if (avatar1 != null && avatar2 == null)
                    return -1;
                if (avatar1 == null && avatar2 != null)
                    return  1;

                //and then compare names
                return player1.getName().toLowerCase().compareTo(player2.getName().toLowerCase());
            }
        }

        //return
        return comp;
    }
}
