package org.figuramc.figura.gui.widgets.permissions;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.gui.widgets.FiguraWidget;
import org.figuramc.figura.gui.widgets.lists.PlayerList;
import org.figuramc.figura.permissions.PermissionPack;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.MathUtils;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.Locale;

public class AbstractPermPackElement extends Button implements Comparable<AbstractPermPackElement>, FiguraWidget {

    protected final PlayerList parent;
    protected final PermissionPack pack;

    protected float scale = 1f;

    protected AbstractPermPackElement(int width, int height, PermissionPack pack, PlayerList parent) {
        super(0, 0, width, height, Component.empty(), null, bx -> {});
        this.parent = parent;
        this.pack = pack;
    }

    protected void animate(float delta, boolean anim) {
        if (anim) {
            float lerpDelta = MathUtils.magicDelta(0.2f, delta);
            scale = Mth.lerp(lerpDelta, scale, 1.2f);
        } else {
            float lerpDelta = MathUtils.magicDelta(0.3f, delta);
            scale = Mth.lerp(lerpDelta, scale, 1f);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        int width = getWidth();
        int height = getHeight();
        int x = getX() + width / 2;
        int y = getY() + height / 2;
        width *= scale / 2f;
        height *= scale / 2f;
        return parent.isInsideScissors(mouseX, mouseY) && isActive() && isVisible() && UIHelper.isMouseOver(x - width, y - height, width * 2, height * 2, mouseX, mouseY);
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }

    @Override
    public void onPress() {
        // set selected entry
        parent.selectedEntry = this;

        // update permissions widgets
        parent.parent.updatePermissions(this.pack);
    }

    public PermissionPack getPack() {
        return pack;
    }

    @Override
    public int compareTo(AbstractPermPackElement other) {
        // compare permission categories first
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
            // then compare types
            if (this instanceof CategoryPermPackElement && other instanceof PlayerPermPackElement)
                return -1;
            if (this instanceof PlayerPermPackElement && other instanceof CategoryPermPackElement)
                return 1;

            if (this instanceof PlayerPermPackElement player1 && other instanceof PlayerPermPackElement player2) {
                Avatar avatar1 = AvatarManager.getAvatarForPlayer(player1.getOwner());
                Avatar avatar2 = AvatarManager.getAvatarForPlayer(player2.getOwner());

                // compare avatar
                if (avatar1 != null && avatar2 == null)
                    return -1;
                if (avatar1 == null && avatar2 != null)
                    return  1;

                // and then compare names
                return player1.getName().toLowerCase(Locale.US).compareTo(player2.getName().toLowerCase(Locale.US));
            }
        }

        // return
        return comp;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.pack.setVisible(visible);
    }
}
