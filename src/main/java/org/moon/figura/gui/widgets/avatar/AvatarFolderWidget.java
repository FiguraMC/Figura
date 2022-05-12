package org.moon.figura.gui.widgets.avatar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.avatars.providers.LocalAvatarFetcher;
import org.moon.figura.gui.widgets.ContainerButton;
import org.moon.figura.gui.widgets.lists.AvatarList;

import java.util.ArrayList;

public class AvatarFolderWidget extends AbstractAvatarWidget {

    private final ArrayList<AbstractAvatarWidget> entries = new ArrayList<>();
    private ContainerButton groupButton;

    public AvatarFolderWidget(int depth, int width, LocalAvatarFetcher.AvatarPath avatar, AvatarList parent) {
        super(depth, width, avatar.getPath(), parent);

        AvatarFolderWidget instance = this;
        this.groupButton = new ContainerButton(parent, x, y, width, 20, new TextComponent("  ".repeat(depth)).append(getName()), null, button -> {
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
        this.groupButton.setToggled(true);
        this.groupButton.shouldHaveBackground(false);
        children.add(this.groupButton);

        for (LocalAvatarFetcher.AvatarPath child : avatar.getChildren()) {
            AbstractAvatarWidget entry = child.hasAvatar() ? new AvatarWidget(depth + 1, width, child.getPath(), parent) : new AvatarFolderWidget(depth + 1, width, child, parent);
            entries.add(entry);
            children.add(entry);
            this.height += entry.getHeight() + 2;
        }

        entries.sort(AbstractAvatarWidget::compareTo);
        children.sort((children1, children2) -> {
            if (children1 instanceof AbstractAvatarWidget avatar1 && children2 instanceof AbstractAvatarWidget avatar2)
                return avatar1.compareTo(avatar2);
            return 0;
        });
    }

    public void toggleEntries(boolean bool) {
        boolean toggle = bool && this.groupButton.isToggled();

        for (AbstractAvatarWidget widget : entries) {
            widget.setVisible(toggle);

            if (widget instanceof AvatarFolderWidget folder)
                folder.toggleEntries(toggle);
        }
    }

    @Override
    public int getHeight() {
        int height = 20;
        for (AbstractAvatarWidget widget : entries) {
            height += widget.getHeight() + 2;
        }
        return groupButton.isToggled() ? height : 20;
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
            y += widget.getHeight() + 2;
        }
    }
}
