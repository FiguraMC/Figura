package org.moon.figura.gui.widgets.avatar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.moon.figura.avatars.providers.LocalAvatarFetcher;
import org.moon.figura.gui.widgets.ContainerButton;
import org.moon.figura.gui.widgets.lists.AvatarList;

import java.util.ArrayList;

public class AvatarFolderWidget extends AbstractAvatarWidget {

    private final ArrayList<AbstractAvatarWidget> entries = new ArrayList<>();
    private ContainerButton groupButton;

    public AvatarFolderWidget(int depth, int width, LocalAvatarFetcher.AvatarPath avatar, AvatarList parent) {
        super(depth, width, avatar, parent);

        AvatarFolderWidget instance = this;
        this.groupButton = new ContainerButton(parent, x, y, width, 20, Component.literal("  ".repeat(depth)).append(getName()), null, button -> {
            toggleEntries(this.groupButton.isToggled());
            parent.updateScroll();
        }) {
            @Override
            protected  void renderText(PoseStack stack) {
                super.renderText(stack);

                //fix tooltip
                if (getTooltip() == getMessage())
                    setTooltip(instance.getName());
            }
        };

        children.add(this.groupButton);

        for (LocalAvatarFetcher.AvatarPath child : avatar.getChildren()) {
            AbstractAvatarWidget entry = child.hasAvatar() ? new AvatarWidget(depth + 1, width, child, parent) : new AvatarFolderWidget(depth + 1, width, child, parent);
            entries.add(entry);
            children.add(entry);
            this.height += entry.height + 2;
        }

        entries.sort(AbstractAvatarWidget::compareTo);
        children.sort((children1, children2) -> {
            if (children1 instanceof AbstractAvatarWidget avatar1 && children2 instanceof AbstractAvatarWidget avatar2)
                return avatar1.compareTo(avatar2);
            return 0;
        });

        boolean expanded = avatar.isExpanded();
        this.groupButton.setToggled(expanded);
        this.groupButton.shouldHaveBackground(false);

        toggleEntries(expanded);
        parent.updateScroll();
    }

    public void toggleEntries(boolean bool) {
        boolean toggle = bool && this.groupButton.isToggled();
        int height = 20;

        for (AbstractAvatarWidget widget : entries) {
            widget.setVisible(toggle);

            if (widget instanceof AvatarFolderWidget folder)
                folder.toggleEntries(toggle);

            height += widget.height + 2;
        }

        this.height = groupButton.isToggled() ? height : 20;
    }

    public void filterChildren() {

    }

    @Override
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;

        this.groupButton.x = x;
        this.groupButton.y = y;

        y = 22;
        for (AbstractAvatarWidget widget : entries) {
            widget.setPos(x, this.y + y);
            y += widget.height + 2;
        }
    }
}
