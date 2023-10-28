package org.figuramc.figura.gui.widgets.lists;

import net.minecraft.client.gui.GuiGraphics;

import org.figuramc.figura.config.ConfigType;
import org.figuramc.figura.gui.widgets.*;
import org.figuramc.figura.lua.api.net.NetworkingAPI;
import org.figuramc.figura.utils.FiguraText;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class NetworkFilterList extends AbstractList {
    private static final int LIST_ELEMENT_Y_GAP = 4;

    private final ConfigType.NetworkFilterConfig config;
    private final List<NetworkFilterEntry> entries = new ArrayList<>();
    private final Button addEntryButton;
    private int scroll, listElementsHeightDifference;
    public NetworkFilterList(int x, int y, int width, int height, ConfigType.NetworkFilterConfig config) {
        super(x, y, width, height);
        scrollBar.setVisible(true);
        scrollBar.setHeight(height - 32);
        scrollBar.setAction(this::onScroll);
        this.config = config;
        children.add(addEntryButton = new Button(x + 4,y + height - 24, width - 8, 20, FiguraText.of("gui.network_filter.list.add_filter_entry"), null, this::onEntryAddClick));
        for (NetworkingAPI.Filter filter :
                config.getFilters()) {
            NetworkFilterEntry entry;
            entries.add(entry = new NetworkFilterEntry(x,y, width - 8 - scrollBar.getWidth(), 20, filter));
            children.add(entry);
        }
        repositionContents();
        updateContentsHeightDiff();
        updateScrollBar();
    }

    private void onScroll(ScrollBarWidget scrollBarWidget) {
        scroll = (int) (scrollBarWidget.getScrollProgress() * listElementsHeightDifference);
        repositionContents();
    }

    private void onEntryAddClick(net.minecraft.client.gui.components.Button button) {
        NetworkFilterEntry entry = new NetworkFilterEntry(getX() + 4, entries.size() * 24,  getWidth() - 22,20);
        entries.add(entry);
        children.add(entry);
        config.getFilters().add(entry.getSourceFilter());
        repositionContents();
        updateContentsHeightDiff();
        updateScrollBar();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        // background and scissors
        UIHelper.blitSliced(gui, x, y, width, height, UIHelper.OUTLINE_FILL);

        super.render(gui, mouseX, mouseY, delta);

        updateScissors(4,4, -18, -32);
        enableScissors(gui);
        for (NetworkFilterEntry entry :
                contents()) {
            entry.render(gui, mouseX, mouseY, delta);
        }
        gui.disableScissor();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        repositionChildren();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        repositionChildren();
        updateContentsHeightDiff();
        updateScrollBar();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        repositionChildren();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        repositionChildren();
    }

    private void updateScrollBar() {
        if (!entries.isEmpty() && listElementsHeightDifference > 0) {
            scrollBar.setVisible(true);
            scrollBar.setScrollProgress(Math.min(1, (double)scroll / listElementsHeightDifference));
            scrollBar.setScrollRatio(20, listElementsHeightDifference);
        }
        else {
            scrollBar.setVisible(false);
        }
    }

    private void repositionChildren() {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        scrollBar.setX(x + width - 14);
        scrollBar.setY(y + 4);
        scrollBar.setHeight(height - 32);

        addEntryButton.setX(x + 4);
        addEntryButton.setY(y + height - 24);
        addEntryButton.setWidth(width - 8);

        repositionContents();
    }

    private void updateContentsHeightDiff() {
        int listElementsTotalHeight = entries.size() * 24 - 4;
        listElementsHeightDifference = listElementsTotalHeight - (getHeight() - 36);
    }

    private void repositionContents() {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int yOffset = 4 - scroll;
        for (NetworkFilterEntry entry :
                contents()) {
            entry.setX(x + 4);
            entry.setY(y + yOffset);
            entry.setWidth(width - 22);
            yOffset += 20 + LIST_ELEMENT_Y_GAP;
        }
    }

    @Override
    public List<NetworkFilterEntry> contents() {
        return entries;
    }

    public static class NetworkFilterEntry extends AbstractContainerElement {
        private final NetworkingAPI.Filter sourceFilter;
        private final EnumButton enumButton;
        private final TextField filterTextField;
        public NetworkFilterEntry(int x, int y, int width, int height) {
            this(x, y, width, height, new NetworkingAPI.Filter("https://example.com", NetworkingAPI.Filter.FilterMode.EQUALS));
        }

        public NetworkFilterEntry(int x, int y, int width, int height, NetworkingAPI.Filter sourceFilter) {
            super(x, y, width, height);
            this.sourceFilter = sourceFilter;
            children.add(enumButton =
                    new EnumButton(x+width-90, y, 90, 20, "gui.network_filter.list.filter_mode"
                            ,sourceFilter.getMode().getId(), 4, this::onEnumSelect)
            );
            children.add(filterTextField = new TextField(x, y, width - 94, 20, TextField.HintType.IP, this::onSourceChange));
            filterTextField.getField().setValue(sourceFilter.getSource());
        }

        @Override
        public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
            int x = getX(), y = getY(), width = getWidth();
            filterTextField.setX(x);
            filterTextField.setY(y);
            filterTextField.setWidth(width - 94);
            enumButton.setX(x+width-90);
            enumButton.setY(y);
            super.render(gui, mouseX, mouseY, delta);
        }

        @Override
        public int getHeight() {
            return 20;
        }

        private void onSourceChange(String s) {
            sourceFilter.setSource(s);
        }

        private void onEnumSelect(int i) {
            sourceFilter.setMode(NetworkingAPI.Filter.FilterMode.getById(i));
        }

        public NetworkingAPI.Filter getSourceFilter() {
            return sourceFilter;
        }
    }
}
