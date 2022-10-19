package org.moon.figura.gui.widgets.trust;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.widgets.lists.PlayerList;
import org.moon.figura.trust.Trust;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.ui.UIHelper;

public class AbstractTrustElement extends AbstractButton implements Comparable<AbstractTrustElement> {

    protected final PlayerList parent;
    protected final TrustContainer trust;

    protected float scale = 1f;

    protected AbstractTrustElement(int height, TrustContainer container, PlayerList parent) {
        super(0, 0, 174, height, TextComponent.EMPTY.copy());
        this.parent = parent;
        this.trust = container;
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
        return parent.isInsideScissors(mouseX, mouseY) && active && visible && UIHelper.isMouseOver(x - dw, y - dh, width + dw, height + dh, mouseX, mouseY);
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }

    @Override
    public void onPress() {
        //set selected entry
        parent.selectedEntry = this;

        //update trust widgets
        parent.parent.updateTrustData(this.trust);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }

    public boolean isVisible() {
        return trust.isVisible();
    }

    public TrustContainer getTrust() {
        return trust;
    }

    @Override
    public int compareTo(AbstractTrustElement other) {
        //compare trust levels first
        int len = Trust.Group.values().length;

        int i;
        if (this instanceof PlayerElement p && p.dragged) {
            i = Math.min(p.index, len - (TrustManager.isLocal(p.trust) ? 1 : 2));
        } else {
            i = this.trust.getGroup().index;
        }

        int j;
        if (other instanceof PlayerElement p && p.dragged) {
            j = Math.min(p.index, len - (TrustManager.isLocal(p.trust) ? 1 : 2));
        } else {
            j = other.trust.getGroup().index;
        }

        int comp = Integer.compare(i, j);
        if (comp == 0) {
            //then compare types
            if (this instanceof GroupElement && other instanceof PlayerElement)
                return -1;
            if (this instanceof PlayerElement && other instanceof GroupElement)
                return 1;

            if (this instanceof PlayerElement player1 && other instanceof PlayerElement player2) {
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
