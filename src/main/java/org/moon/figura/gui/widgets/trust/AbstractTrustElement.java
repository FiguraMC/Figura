package org.moon.figura.gui.widgets.trust;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.gui.widgets.lists.PlayerList;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;

import java.util.ArrayList;

public class AbstractTrustElement extends AbstractButton implements Comparable<AbstractTrustElement> {

    protected final PlayerList parent;
    protected final TrustContainer trust;

    protected float scale = 1f;

    protected AbstractTrustElement(int height, TrustContainer container, PlayerList parent) {
        super(0, 0, 174, height, Component.empty());
        this.parent = parent;
        this.trust = container;
    }

    protected void animate(int mouseX, int mouseY, float delta) {
        if (this.isMouseOver(mouseX, mouseY) || this.isFocused()) {
            scale = (float) Mth.lerp(1 - Math.pow(0.2, delta), scale, 1.2f);
        } else {
            scale = (float) Mth.lerp(1 - Math.pow(0.3, delta), scale, 1f);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.parent.isInsideScissors(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
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
        return trust.getParentGroup().visible;
    }

    public TrustContainer getTrust() {
        return trust;
    }

    @Override
    public int compareTo(AbstractTrustElement other) {
        //compare trust levels first
        ArrayList<TrustContainer> list = new ArrayList<>(TrustManager.GROUPS.values());
        int comp = Integer.compare(list.indexOf(this.trust.getParentGroup()), list.indexOf(other.trust.getParentGroup()));

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
