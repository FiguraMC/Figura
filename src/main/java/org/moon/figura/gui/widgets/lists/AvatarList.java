package org.moon.figura.gui.widgets.lists;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.local.LocalAvatarFetcher;
import org.moon.figura.gui.screens.AbstractPanelScreen;
import org.moon.figura.gui.screens.AvatarWizardScreen;
import org.moon.figura.gui.widgets.Button;
import org.moon.figura.gui.widgets.SearchBar;
import org.moon.figura.gui.widgets.avatar.AbstractAvatarWidget;
import org.moon.figura.gui.widgets.avatar.AvatarFolderWidget;
import org.moon.figura.gui.widgets.avatar.AvatarWidget;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AvatarList extends AbstractList {

    // -- Variables -- //
    private final HashMap<Path, AbstractAvatarWidget> avatars = new HashMap<>();
    private final ArrayList<AbstractAvatarWidget> avatarList = new ArrayList<>();

    private int totalHeight = 0;
    private String filter = "";

    public static AvatarWidget selectedEntry;

    // -- Constructors -- //

    public AvatarList(int x, int y, int width, int height, AbstractPanelScreen parentScreen) {
        super(x, y, width, height);

        //search bar
        children.add(new SearchBar(x + 4, y + 4, width - 8, 20, s -> {
            if (!filter.equals(s))
                scrollBar.setScrollProgress(0f);
            filter = s;
        }));

        //new avatar
        children.add(new Button(
                x + width / 2 - 46, y + 28,
                20, 20, 0, 0, 20,
                new FiguraIdentifier("textures/gui/new_avatar.png"),
                60, 20,
                FiguraText.of("gui.wardrobe.new_avatar.tooltip"),
                button -> Minecraft.getInstance().setScreen(new AvatarWizardScreen(parentScreen)))
        );

        //unselect
        children.add(new Button(
                x + width / 2 - 10, y + 28,
                20, 20, 0, 0, 20,
                new FiguraIdentifier("textures/gui/unselect.png"),
                60, 20,
                FiguraText.of("gui.wardrobe.unselect.tooltip"),
                button -> {
                    AvatarManager.loadLocalAvatar(null);
                    selectedEntry = null;
                })
        );

        //root folder
        children.add(new Button(
                x + width / 2 + 26, y + 28,
                20, 20, 0, 0, 20,
                new FiguraIdentifier("textures/gui/folder.png"),
                60, 20,
                FiguraText.of("gui.wardrobe.folder.tooltip"),
                button -> Util.getPlatform().openFile(LocalAvatarFetcher.getLocalAvatarDirectory().toFile()))
        );

        //scrollbar
        this.scrollBar.setY(y + 48);
        this.scrollBar.setHeight(height - 52);

        //scissors
        this.updateScissors(1, 49, -2, -50);

        //initial load
        LocalAvatarFetcher.loadAvatars();
        loadContents();

        scrollToSelected();
    }

    // -- Functions -- //
    @Override
    public void tick() {
        //update list
        loadContents();
        super.tick();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        //background and scissors
        UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE_FILL);
        UIHelper.setupScissor(x + scissorsX, y + scissorsY, width + scissorsWidth, height + scissorsHeight);

        //scrollbar
        totalHeight = 2;
        for (AbstractAvatarWidget avatar : avatarList)
            totalHeight += avatar.getHeight() + 2;
        int entryHeight = avatarList.isEmpty() ? 0 : totalHeight / avatarList.size();

        scrollBar.setVisible(totalHeight > height - 49);
        scrollBar.setScrollRatio(entryHeight, totalHeight - (height - 49));

        //render list
        int xOffset = scrollBar.isVisible() ? 4 : 11;
        int yOffset = scrollBar.isVisible() ? (int) -(Mth.lerp(scrollBar.getScrollProgress(), -49, totalHeight - height)) : 49;
        boolean hidden = false;

        for (AbstractAvatarWidget avatar : avatarList) {
            if (hidden) continue;

            avatar.setX(x + xOffset);
            avatar.setY(y + yOffset);

            if (avatar.getY() + avatar.getHeight() > y + scissorsY)
                avatar.render(stack, mouseX, mouseY, delta);

            yOffset += avatar.getHeight() + 2;
            if (yOffset > height)
                hidden = true;
        }

        //reset scissor
        UIHelper.disableScissor();

        //render children
        super.render(stack, mouseX, mouseY, delta);
    }

    private void loadContents() {
        // Load avatars //
        HashSet<Path> missingPaths = new HashSet<>(avatars.keySet());
        for (LocalAvatarFetcher.AvatarPath avatar : LocalAvatarFetcher.ALL_AVATARS) {
            Path path = avatar.getPath();

            //filter
            if (!avatar.search(filter))
                continue;

            //update current
            AbstractAvatarWidget widget = avatars.get(path);
            if (widget != null)
                widget.update(avatar, filter);

            //do not remove if passed
            missingPaths.remove(path);

            //add to the avatar list
            avatars.computeIfAbsent(path, p -> {
                int width = this.getWidth() - 22;
                AbstractAvatarWidget entry = avatar instanceof LocalAvatarFetcher.FolderPath folder ? new AvatarFolderWidget(0, width, folder, this) : new AvatarWidget(0, width, avatar, this);

                avatarList.add(entry);
                children.add(entry);

                return entry;
            });
        }

        //remove missing avatars
        for (Path missingPath : missingPaths) {
            AbstractAvatarWidget obj = avatars.remove(missingPath);
            avatarList.remove(obj);
            children.remove(obj);
        }

        //sort lists
        avatarList.sort(AbstractAvatarWidget::compareTo);
        children.sort((children1, children2) -> {
            if (children1 instanceof AbstractAvatarWidget avatar1 && children2 instanceof AbstractAvatarWidget avatar2)
                return avatar1.compareTo(avatar2);
            return 0;
        });
    }

    public void updateScroll() {
        //store old scroll pos
        double pastScroll = (totalHeight - getHeight()) * scrollBar.getScrollProgress();

        //get new height
        totalHeight = 2;
        for (AbstractAvatarWidget avatar : avatarList)
            totalHeight += avatar.getHeight() + 2;

        //set new scroll percentage
        scrollBar.setScrollProgress(pastScroll / (totalHeight - getHeight()));
    }

    public void scrollToSelected() {
        double y = 0;

        //get height
        totalHeight = 0;
        for (AbstractAvatarWidget avatar : avatarList) {
            if (avatar.equals(selectedEntry))
                y = totalHeight;
            else
                totalHeight += avatar.getHeight() + 2;
        }

        //set scroll
        scrollBar.setScrollProgressNoAnim(y / totalHeight);
    }

    @Override
    public List<? extends GuiEventListener> contents() {
        return avatarList;
    }
}
