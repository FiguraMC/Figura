package org.figuramc.figura.gui.widgets.avatar;

import net.minecraft.client.gui.GuiGraphics;
import org.figuramc.figura.avatar.local.LocalAvatarFetcher;
import org.figuramc.figura.gui.widgets.ContainerButton;
import org.figuramc.figura.gui.widgets.lists.AvatarList;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AvatarFolderWidget extends AbstractAvatarWidget {

    private final HashMap<String, AbstractAvatarWidget> entries = new HashMap<>();
    private final ArrayList<AbstractAvatarWidget> sortedEntries = new ArrayList<>();

    public AvatarFolderWidget(int depth, int width, LocalAvatarFetcher.FolderPath avatar, AvatarList parent) {
        super(depth, width, 20, avatar, parent);

        AvatarFolderWidget instance = this;
        this.button = new ContainerButton(parent, getX(), getY(), width, 20, getName(), null, button -> {
            toggleEntries(showChildren());
            parent.updateScroll();
        }) {
            @Override
            protected void renderText(GuiGraphics gui, float delta) {
                // ugly hack
                int x = getX();
                int width = getWidth();

                int space = Math.max(SPACING * depth - 2, 0);

                setX(x + space);
                setWidth(width - space);

                super.renderText(gui, delta);

                setX(x);
                setWidth(width);

                // fix tooltip
                if (tooltip() == getMessage())
                    setTooltip(instance.getName());
            }

            @Override
            public void setHovered(boolean hovered) {
                if (!hovered && UIHelper.getContext() == context && context.isVisible())
                    hovered = true;

                super.setHovered(hovered);
            }
        };

        children.add(this.button);

        update(avatar, "");

        boolean expanded = avatar.isExpanded();
        ((ContainerButton) this.button).setToggled(expanded);
        this.button.shouldHaveBackground(false);

        toggleEntries(expanded);
        parent.updateScroll();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        if (!isVisible() || !this.button.isVisible())
            return;

        if (showChildren()) {
            int y0 = parent.getY() + parent.scissorsY;
            int y1 = y0 + parent.getHeight() + parent.scissorsHeight;

            for (AbstractAvatarWidget value : entries.values())
                value.button.setVisible(value.getY() < y1 && value.getY() + value.getHeight() > y0);

            super.render(gui, mouseX, mouseY, delta);

            for (AbstractAvatarWidget value : entries.values())
                value.button.setVisible(true);
        } else {
            super.render(gui, mouseX, mouseY, delta);
        }
    }

    @Override
    public void update(LocalAvatarFetcher.AvatarPath path, String filter) {
        super.update(path, filter);

        if (!(path instanceof LocalAvatarFetcher.FolderPath folderPath))
            return;

        for (AbstractAvatarWidget value : entries.values())
            value.filter = this.filter;

        // update children
        HashSet<String> missingPaths = new HashSet<>(entries.keySet());
        for (LocalAvatarFetcher.AvatarPath child : folderPath.getChildren()) {
            String str = child.getPath() + child.getName();

            // skip unfiltered
            if (!child.search(filter))
                continue;

            // update children
            AbstractAvatarWidget childEntry = entries.get(str);
            if (childEntry != null)
                childEntry.update(child, filter);

            // remove from exclusion list
            missingPaths.remove(str);

            // add children
            this.entries.computeIfAbsent(str, s -> {
                AbstractAvatarWidget entry = child instanceof LocalAvatarFetcher.FolderPath folder ? new AvatarFolderWidget(depth + 1, getWidth(), folder, parent) : new AvatarWidget(depth + 1, getWidth(), child, parent);
                children.add(entry);
                entry.setVisible(showChildren());
                return entry;
            });
        }

        // remove missing avatars
        for (String str : missingPaths)
            children.remove(entries.remove(str));

        sortedEntries.clear();
        sortedEntries.addAll(entries.values());

        // sort children
        children.sort((children1, children2) -> {
            if (children1 instanceof AbstractAvatarWidget avatar1 && children2 instanceof AbstractAvatarWidget avatar2)
                return avatar1.compareTo(avatar2);
            return 0;
        });
        sortedEntries.sort(AbstractAvatarWidget::compareTo);

        // update height
        updateHeight();
    }

    public void toggleEntries(boolean toggle) {
        toggle = toggle && showChildren();
        avatar.setExpanded(toggle);

        for (AbstractAvatarWidget widget : entries.values()) {
            widget.setVisible(toggle);

            if (widget instanceof AvatarFolderWidget folder)
                folder.toggleEntries(toggle);
        }

        updateHeight();
    }

    private void updateHeight() {
        int height = 20;

        boolean show = showChildren();
        for (AbstractAvatarWidget entry : entries.values()) {
            if (entry instanceof AvatarFolderWidget folder)
                folder.updateHeight();
            if (show) height += entry.getHeight() + 2;
        }

        this.setHeight(height);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        for (AbstractAvatarWidget widget : sortedEntries) {
            if (widget.isVisible())
                widget.setX(x);
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        y = 22;
        for (AbstractAvatarWidget widget : sortedEntries) {
            if (widget.isVisible()) {
                widget.setY(this.getY() + y);
                y += widget.getHeight() + 2;
            }
        }
    }

    @Override
    public boolean filtered() {
        boolean result = super.filtered();

        for (AbstractAvatarWidget value : entries.values()) {
            if (result) break;
            result = value.filtered();
        }

        return result;
    }

    public boolean showChildren() {
        return ((ContainerButton) this.button).isToggled();
    }
}
