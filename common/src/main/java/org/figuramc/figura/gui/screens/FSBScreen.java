package org.figuramc.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.gui.widgets.FiguraWidget;
import org.figuramc.figura.gui.widgets.fsb_pages.*;
import org.figuramc.figura.gui.widgets.lists.FSBPageList;
import org.figuramc.figura.utils.FiguraText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FSBScreen extends AbstractPanelScreen {

    // Hues for sidebar sections
    public static final int SECTION_CLIENT = 235;
    public static final int SECTION_LAN = 160;
    public static final int SECTION_REMOTE = 55;
    public static final int SECTION_EXIT = 0;

    public static final int TOP_MARGIN = 28 - 4; // height of top tabs
    public static final int LEFT_PANEL_MIN = 150;
    public static final int LEFT_PANEL_MAX = 250;
    public static final int CENTER_PANEL_IDEAL_SIZE = 420;
    public static final int BUTTON_HEIGHT = 20;
    public static final int SPACE = 2;

    private FSBPageList pageList = null;
    private AbstractFSBPage page = null;

    private AbstractFSBPage currentPage;

    public FSBScreen(Screen parentScreen) {
        super(parentScreen, FiguraText.of("gui.panels.title.fsb"));

        currentPage = make(ConnectionList::new);
    }

    public void switchTo(AbstractFSBPage target) {
        if (page != null) {
            this.removeWidget(page);
        }
        page = target;
        this.addRenderableWidget(page);
        relayout();
    }

    private void runLayoutCentered() {
        /*
         * padding around
         */
        /*
         * 1. Consider how wide the left panel will be. We need a minimum of 150u for that.
         * 2. Place the center panel depending on the subpage. Either way, it either needs to be
         *    centered, or needs to take up the entire screen. If there isn't enough space to center
         *    it without shrinking the left panel, go for the 'entire screen' strategy. If the
         *    sub-page requires the entire screen, always do that.
         * 3. If we decide on "center", expand the left panel so that it lines up and isn't
         *    just awkwardly floating off to the side. Max out at ~250u and start stretching
         *    the center panel anyway after that point (make sure it's symmetrical though!).
         * 4. We might have more space on the right. If we do, fit the mandatory "back" button there.
         *    Otherwise, chop off some space from the bottom of all three sections to fit it.
         */
        int total = width;
        // Total width being odd leads to all sorts of problems with centering containers.
        if (total % 2 == 1) total--;

        int leftPanelWidth = LEFT_PANEL_MIN;
        int centerPanelWidth = CENTER_PANEL_IDEAL_SIZE;

        // Step 2: Can we even fit the center panel in there?
        int spaceRemaining = total / 2 - leftPanelWidth - (centerPanelWidth / 2);
        if (spaceRemaining < 0) {
            // ... no, we can't.
            runLayoutMaximized();
            return;
        }
        // Step 3a: Expand the left panel up to LEFT_PANEL_MAX.
        int expandLeftPanelBy = Math.min(spaceRemaining, LEFT_PANEL_MAX - LEFT_PANEL_MIN);
        leftPanelWidth += expandLeftPanelBy;
        spaceRemaining -= expandLeftPanelBy;

        if (spaceRemaining > 0) {
            // Step 3b:
            // The layout sliding around when switching between tabs is horrible UX.
            // So instead we make the center panel bigger against its wishes to fill space to prevent that.
            centerPanelWidth += spaceRemaining * 2;
            spaceRemaining = 0;
        }

        // Lay out the top left corners of everything so far...
        int col2x = leftPanelWidth;

        pageList.setBox(0, TOP_MARGIN, leftPanelWidth, height - TOP_MARGIN - 1);
        page.setBox(col2x, TOP_MARGIN, centerPanelWidth, height - TOP_MARGIN - 1);
        page.isActuallyMaximized(false);
    }

    private void runLayoutMaximized() {
        // Emulate #runLayoutCentered to avoid layout shifts
        int emuTotal = width;
        int leftPanelWidth = LEFT_PANEL_MIN;
        if (emuTotal % 2 == 1) emuTotal--;
        int emuSpaceRemaining = emuTotal / 2 - leftPanelWidth - (CENTER_PANEL_IDEAL_SIZE / 2);
        if (emuSpaceRemaining > 0) {
            leftPanelWidth += Math.min(emuSpaceRemaining, LEFT_PANEL_MAX - LEFT_PANEL_MIN);
        }

        // Fill out the rest
        pageList.setBox(0, TOP_MARGIN, leftPanelWidth, height - TOP_MARGIN - 1);
        page.setBox(leftPanelWidth, TOP_MARGIN, width - leftPanelWidth, height - TOP_MARGIN - 1);
        page.isActuallyMaximized(true);
    }

    private void relayout() {
        if (page.isMaximized()) runLayoutMaximized();
        else runLayoutCentered();

        pageList.relayout();
        page.relayout();
    }

    private <T extends AbstractFSBPage> T make(PageCtor<T> ctor) {
        return ctor.auto(this);
    }

    private void connectionsPage() {
        FiguraMod.LOGGER.info("conneectionfrwjriw page");
        switchTo(make(ConnectionList::new));
    }
    private void clientSettingsPage() {
        FiguraMod.LOGGER.info("settignwejrowej page");
        switchTo(make(DebugFSBPage::new));
    }

    private int pageListHash = -1;

    private int computePageListHash() {
        return Objects.hash();
    }

    @Override
    public void tick() {
        int newContentH = computePageListHash();
        if (newContentH != pageListHash) {
            pageList.updateContent(this::updatePageList);
            pageListHash = newContentH;
        }

        super.tick();
    }

    private void updatePageList(List<FiguraWidget> target) {
        target.clear();

        // Always present
        // TODO: TRANS (RIGHTS) (i mean I18N)
        target.add(PageButton.of(Component.literal("Connections"), null, 16, q -> connectionsPage(), SECTION_CLIENT));
        target.add(PageButton.of(Component.literal("Client Settings"), null, 16, q -> clientSettingsPage(), SECTION_CLIENT));
    }

    @Override
    public void init() {
        super.init();

        // List of sub-pages
        pageListHash = computePageListHash();
        ArrayList<FiguraWidget> listContent = new ArrayList<>(8);
        updatePageList(listContent);
        pageList = new FSBPageList(16, 16, 150, 150, listContent);
        addRenderableWidget(pageList);

        switchTo(currentPage);

        relayout();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


}
